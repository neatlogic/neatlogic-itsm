package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.MyApiComponent;
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

    @Resource
    private UserMapper userMapper;

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
            @Param(name = "channel", type = ApiParamType.STRING, isRequired = true, desc = "支持channelUuid和channelName入参"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, maxLength = 80, desc = "标题"),
            @Param(name = "owner", type = ApiParamType.STRING, isRequired = true, desc = "上报人uuid和上报人id入参"),
            @Param(name = "reporter", type = ApiParamType.STRING, desc = "代报人"),
            @Param(name = "priority", type = ApiParamType.STRING, isRequired = true, desc = "优先级"),
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
        //上报人，支持上报人uuid和上报人id入参
        String owner = jsonObj.getString("owner");
        UserVo userVo = userMapper.getUserByUuid(owner);
        if (userVo == null) {
            userVo = userMapper.getUserByUserId(owner);
            if (userVo == null) {
                throw new UserNotFoundException(owner);
            }
        }
        //处理channel，支持channelUuid和channelName入参
        String channel = jsonObj.getString("channel");
        ChannelVo channelVo = channelMapper.getChannelByUuid(channel);
        if (channelVo == null) {
            channelVo = channelMapper.getChannelByName(channel);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channel);
            }
            jsonObj.put("channelUuid", channelVo.getUuid());
        }
        //优先级
        String priority = jsonObj.getString("priority");
        PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priority);
        if(priorityVo == null){
            priorityVo = priorityMapper.getPriorityByName(priority);
            if(priorityVo == null){
                throw new PriorityNotFoundException(priority);
            }
            jsonObj.put("priorityUuid", priorityVo.getUuid());
        }
        //流程
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelVo.getUuid());
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        //如果表单属性数据列表，使用的唯一标识是label时，需要转换成attributeUuid
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

        //暂存
        jsonObj.put("isNeedValid", 1);
        MyApiComponent draftSaveApi = (MyApiComponent) PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
        JSONObject saveResultObj = JSONObject.parseObject(draftSaveApi.myDoService(jsonObj).toString());
        saveResultObj.put("action", "start");

        //查询可执行下一 步骤
        MyApiComponent stepListApi = (MyApiComponent) PrivateApiComponentFactory.getInstance(ProcessTaskProcessableStepList.class.getName());
        Object nextStepListObj = stepListApi.myDoService(saveResultObj);
        List<ProcessTaskStepVo> nextStepList = (List<ProcessTaskStepVo>) nextStepListObj;
        if (CollectionUtils.isEmpty(nextStepList) && nextStepList.size() != 1) {
            throw new RuntimeException("抱歉！暂不支持开始节点连接多个后续节点。");
        }
        saveResultObj.put("nextStepId", nextStepList.get(0).getId());

        //流转
        MyApiComponent startProcessApi = (MyApiComponent) PrivateApiComponentFactory.getInstance(ProcessTaskStartProcessApi.class.getName());
        startProcessApi.myDoService(saveResultObj);

        result.put("processTaskId", saveResultObj.getString("processTaskId"));
        return result;
    }

}
