package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskCreatePublicApi extends PublicApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private PriorityMapper priorityMapper;

    @Override
    public String getName() {
        return "上报工单(供第三方使用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Input({
            @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, maxLength = 80, desc = "标题"),
            @Param(name = "owner", type = ApiParamType.STRING, isRequired = true, desc = "请求人"),
            @Param(name = "reporter", type = ApiParamType.STRING, desc = "代报人"),
            @Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
            @Param(name = "priorityName", type = ApiParamType.STRING, desc = "优先级名"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "隐藏表单属性列表"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "只读表单属性列表"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "附件id列表"),
            @Param(name = "handlerStepInfo", type = ApiParamType.JSONOBJECT, desc = "处理器特有的步骤信息"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "来源")
    })
    @Output({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id")
    })
    @Description(desc = "上报工单(供第三方使用)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        String channelUuid = jsonObj.getString("channelUuid");
        if (channelMapper.checkChannelIsExists(channelUuid) == 0) {
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        /** 如果表单属性数据列表，使用的唯一标识是label时，需要转换成attributeUuid **/
        JSONArray formAttributeDataList = jsonObj.getJSONArray("formAttributeDataList");
        if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
            int count = 0;
            for (int i = 0; i < formAttributeDataList.size(); i++) {
                JSONObject formAttributeData = formAttributeDataList.getJSONObject(i);
                if (MapUtils.isNotEmpty(formAttributeData)) {
                    String attributeUuid = formAttributeData.getString("attributeUuid");
                    String label = formAttributeData.getString("label");
                    if (StringUtils.isBlank(attributeUuid) && StringUtils.isNotBlank(label)) {
                        count++;
                    }
                }
            }
            if (count > 0) {
                ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
                if (processFormVo != null) {
                    FormVersionVo actionFormVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
                    List<FormAttributeVo> formAttributeVoList = actionFormVersionVo.getFormAttributeList();
                    if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
                        Map<String, String> labelAttributeUuidMap = new HashMap<>();
                        for (FormAttributeVo formAttributeVo : formAttributeVoList) {
                            labelAttributeUuidMap.put(formAttributeVo.getLabel(), formAttributeVo.getUuid());
                        }
                        for (int i = 0; i < formAttributeDataList.size(); i++) {
                            JSONObject formAttributeData = formAttributeDataList.getJSONObject(i);
                            if (MapUtils.isNotEmpty(formAttributeData)) {
                                String attributeUuid = formAttributeData.getString("attributeUuid");
                                String label = formAttributeData.getString("label");
                                if (StringUtils.isBlank(attributeUuid) && StringUtils.isNotBlank(label)) {
                                    formAttributeData.put("attributeUuid", labelAttributeUuidMap.get(label));
                                }
                            }
                        }
                    }
                }
            }
        }
        /** 如果优先级使用名称时，需要转换成uuid **/
        String priorityUuid = jsonObj.getString("priorityUuid");
        if (StringUtils.isBlank(priorityUuid)) {
            String priorityName = jsonObj.getString("priorityName");
            if (StringUtils.isBlank(priorityName)) {
                throw new ParamNotExistsException("priorityUuid", "priorityName");
            }
            PriorityVo priorityVo = priorityMapper.getPriorityByName(priorityName);
            if (priorityVo == null) {
                throw new PriorityNotFoundException(priorityName);
            }
            jsonObj.put("priorityUuid", priorityVo.getUuid());
        }
        //暂存
        jsonObj.put("isNeedValid", 1);
        ProcessTaskDraftSaveApi drafSaveApi = (ProcessTaskDraftSaveApi) PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
        JSONObject saveResultObj = JSONObject.parseObject(drafSaveApi.myDoService(jsonObj).toString());
        saveResultObj.put("action", "start");

        //查询可执行下一步骤
        ProcessTaskProcessableStepList stepListApi = (ProcessTaskProcessableStepList) PrivateApiComponentFactory.getInstance(ProcessTaskProcessableStepList.class.getName());
        Object nextStepListObj = stepListApi.myDoService(saveResultObj);
        List<ProcessTaskStepVo> nextStepList = (List<ProcessTaskStepVo>) nextStepListObj;
        if (CollectionUtils.isEmpty(nextStepList) && nextStepList.size() != 1) {
            throw new RuntimeException("抱歉！暂不支持开始节点连接多个后续节点。");
        }
        saveResultObj.put("nextStepId", nextStepList.get(0).getId());

        //流转
        ProcessTaskStartProcessApi startProcessApi = (ProcessTaskStartProcessApi) PrivateApiComponentFactory.getInstance(ProcessTaskStartProcessApi.class.getName());
        startProcessApi.myDoService(saveResultObj);

        result.put("processTaskId", saveResultObj.getString("processTaskId"));
        return result;
    }

}
