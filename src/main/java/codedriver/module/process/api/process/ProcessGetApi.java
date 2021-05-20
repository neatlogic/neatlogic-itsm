package codedriver.module.process.api.process;

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
                    IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(ProcessStepHandlerType.END.getHandler());
                    if (processStepInternalHandler == null) {
                        throw new ProcessStepUtilHandlerNotFoundException(ProcessStepHandlerType.END.getHandler());
                    }
                    JSONObject processObj = processStepInternalHandler.makeupProcessStepConfig(process);
                    JSONArray connectionList = process.getJSONArray("connectionList");
                    if (CollectionUtils.isNotEmpty(connectionList)) {
                        connectionList.removeIf(Objects::isNull);
                    } else {
                        connectionList = new JSONArray();
                    }
                    processObj.put("connectionList", connectionList);
                    JSONArray stepList = process.getJSONArray("stepList");
                    if (CollectionUtils.isNotEmpty(stepList)) {
                        stepList.removeIf(Objects::isNull);
                        for (int i = 0; i < stepList.size(); i++) {
                            JSONObject step = stepList.getJSONObject(i);
                            String handler = step.getString("handler");
                            processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
                            if (processStepInternalHandler == null) {
                                throw new ProcessStepUtilHandlerNotFoundException(handler);
                            }
                            JSONObject stepConfig = step.getJSONObject("stepConfig");
                            JSONObject stepConfigObj = processStepInternalHandler.makeupProcessStepConfig(stepConfig);
                            step.put("stepConfig", stepConfigObj);
                        }
                    }
                    processObj.put("stepList", stepList);
                    configObj.put("process", processObj);
					String newConfig = JSONObject.toJSONString(configObj, SerializerFeature.MapSortField);
					System.out.println("-------------------------");
					System.out.println(oldConfig);
					System.out.println(newConfig);
					System.out.println("=========================");
                    processVo.setConfig(newConfig);
                }
            }
        }
        int count = processMapper.getProcessReferenceCount(uuid);
        processVo.setReferenceCount(count);
        return processVo;
    }
}
