package codedriver.module.process.api.process;

import codedriver.framework.lcs.LCSUtil;
import codedriver.framework.lcs.PrintSingeColorFormatUtil;
import codedriver.framework.lcs.SegmentPair;
import codedriver.framework.lcs.SegmentRange;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.PROCESS_MODIFY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessMapper processMapper;

    @Override
    public String getToken() {
        return "process/get";
    }

    @Override
    public String getName() {
        return "获取单个流程图数据接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
			@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
    })
    @Output({
			@Param(explode = ProcessVo.class)
    })
    @Description(desc = "获取单个流程图数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ProcessVo processVo = processMapper.getProcessByUuid(uuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(uuid);
        }
        String config = processVo.getConfig();
        if (StringUtils.isNotBlank(config)) {
            JSONObject configObj = JSONObject.parseObject(config);
            if (MapUtils.isNotEmpty(configObj)) {
                String oldConfig = JSONObject.toJSONString(configObj, SerializerFeature.MapSortField);
                JSONObject process = configObj.getJSONObject("process");
                if (MapUtils.isNotEmpty(process)) {
                    JSONArray connectionList = process.getJSONArray("connectionList");
                    process.remove("connectionList");
                    String source = JSONObject.toJSONString(process, SerializerFeature.MapSortField);
                    IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(ProcessStepHandlerType.END.getHandler());
                    if (processStepInternalHandler == null) {
                        throw new ProcessStepUtilHandlerNotFoundException(ProcessStepHandlerType.END.getHandler());
                    }
                    JSONObject processObj = processStepInternalHandler.makeupProcessStepConfig(process);
                    JSONArray stepList = process.getJSONArray("stepList");
                    if (CollectionUtils.isNotEmpty(stepList)) {
                        stepList.removeIf(Objects::isNull);
                        for (int i = 0; i < stepList.size(); i++) {
                            JSONObject step = stepList.getJSONObject(i);
                            String handler = step.getString("handler");
                            if(!Objects.equals(handler, ProcessStepHandlerType.END.getHandler())){
                                processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
                                if (processStepInternalHandler == null) {
                                    throw new ProcessStepUtilHandlerNotFoundException(handler);
                                }
                                JSONObject stepConfig = step.getJSONObject("stepConfig");
                                JSONObject stepConfigObj = processStepInternalHandler.makeupProcessStepConfig(stepConfig);
                                step.put("stepConfig", stepConfigObj);
                            }
                        }
                    }
                    processObj.put("stepList", stepList);
                    String target = JSONObject.toJSONString(processObj, SerializerFeature.MapSortField);
                    if (CollectionUtils.isNotEmpty(connectionList)) {
                        connectionList.removeIf(Objects::isNull);
                    } else {
                        connectionList = new JSONArray();
                    }
                    processObj.put("connectionList", connectionList);
                    configObj.put("process", processObj);
					String newConfig = JSONObject.toJSONString(configObj, SerializerFeature.MapSortField);
					System.out.println("-------------------------");
//					System.out.println(config);
//					System.out.println(oldConfig);
//					System.out.println(newConfig);
                    List<SegmentPair> segmentPairList = LCSUtil.LCSCompare(source, target);
                    List<SegmentRange> oldSegmentRangeList = new ArrayList<>();
                    List<SegmentRange> newSegmentRangeList = new ArrayList<>();
                    for(SegmentPair segmentpair : segmentPairList) {
//                    System.out.println(segmentpair);
                        oldSegmentRangeList.add(new SegmentRange(segmentpair.getOldBeginIndex(), segmentpair.getOldEndIndex(), segmentpair.isMatch()));
                        newSegmentRangeList.add(new SegmentRange(segmentpair.getNewBeginIndex(), segmentpair.getNewEndIndex(), segmentpair.isMatch()));
                    }
                    String oldResult = LCSUtil.wrapChangePlace(source, oldSegmentRangeList, LCSUtil.SPAN_CLASS_DELETE, LCSUtil.SPAN_END);
                    PrintSingeColorFormatUtil.println();
                    String newResult = LCSUtil.wrapChangePlace(target, newSegmentRangeList, LCSUtil.SPAN_CLASS_INSERT, LCSUtil.SPAN_END);
                    PrintSingeColorFormatUtil.println();
					System.out.println("=========================");
//                    processVo.setConfig(newConfig);
                }
            }
        }
        int count = processMapper.getProcessReferenceCount(uuid);
        processVo.setReferenceCount(count);
        return processVo;
    }
}
