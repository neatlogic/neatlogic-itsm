package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.matrix.constvalue.MatrixType;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
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
import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.MyApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    @Resource
    private MatrixMapper matrixMapper;

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
            jsonObj.put("owner", userVo.getUuid());
        }
        //处理channel，支持channelUuid和channelName入参
        String channel = jsonObj.getString("channel");
        ChannelVo channelVo = channelMapper.getChannelByUuid(channel);
        if (channelVo == null) {
            channelVo = channelMapper.getChannelByName(channel);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channel);
            }
        }
        jsonObj.put("channelUuid", channelVo.getUuid());
        //优先级
        String priority = jsonObj.getString("priority");
        PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priority);
        if(priorityVo == null){
            priorityVo = priorityMapper.getPriorityByName(priority);
            if(priorityVo == null){
                throw new PriorityNotFoundException(priority);
            }
        }
        jsonObj.put("priorityUuid", priorityVo.getUuid());
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
                        Map<String, FormAttributeVo> labelAttributeMap = new HashMap<>();
                        for (FormAttributeVo formAttributeVo : formAttributeVoList) {
                            labelAttributeMap.put(formAttributeVo.getLabel(), formAttributeVo);
                        }
                        for (int i = 0; i < formAttributeDataList.size(); i++) {
                            JSONObject formAttributeData = formAttributeDataList.getJSONObject(i);
                            if (MapUtils.isNotEmpty(formAttributeData)) {
                                String attributeUuid = formAttributeData.getString("attributeUuid");
                                String label = formAttributeData.getString("label");
                                if (StringUtils.isBlank(attributeUuid) && StringUtils.isNotBlank(label)) {
                                    FormAttributeVo formAttributeVo = labelAttributeMap.get(label);
                                    if (formAttributeVo == null) {

                                    }
                                    formAttributeData.put("attributeUuid", formAttributeVo.getUuid());
                                    JSONArray dataArray = formAttributeData.getJSONArray("dataList");
                                    if (CollectionUtils.isEmpty(dataArray)) {
                                        continue;
                                    }
                                    List<String> dataList = dataArray.toJavaList(String.class);
                                    String configStr = formAttributeVo.getConfig();
                                    JSONObject config = JSONObject.parseObject(configStr);
                                    Object data = textConversionValue(dataList, config);
                                    if (data != null) {
                                        formAttributeData.put("dataList", data);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String reporter = jsonObj.getString("reporter");
        if (StringUtils.isNotBlank(reporter)) {
            UserVo reporterUserVo = userMapper.getUserByUuid(reporter);
            if (reporterUserVo == null) {
                reporterUserVo = userMapper.getUserByUserId(reporter);
                if (reporterUserVo == null) {
                    throw new UserNotFoundException(reporter);
                }
                reporterUserVo = userMapper.getUserByUuid(reporterUserVo.getUuid());
            }
            UserContext.init(reporterUserVo, SystemUser.SYSTEM.getTimezone());
        }
        //暂存
        //TODO isNeedValid 参数是否需要？？？
        jsonObj.put("isNeedValid", 1);
        MyApiComponent draftSaveApi = (MyApiComponent) PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
        JSONObject saveResultObj = (JSONObject) draftSaveApi.myDoService(jsonObj);
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

    private Object textConversionValue(List<String> values, JSONObject config) {
        Object result = null;
        if (CollectionUtils.isNotEmpty(values)) {
            boolean isMultiple = config.getBooleanValue("isMultiple");
            String dataSource = config.getString("dataSource");
            if ("static".equals(dataSource)) {
                List<ValueTextVo> dataList =
                        JSON.parseArray(JSON.toJSONString(config.getJSONArray("dataList")), ValueTextVo.class);
                if (CollectionUtils.isNotEmpty(dataList)) {
                    Map<String, Object> valueTextMap = new HashMap<>();
                    for (ValueTextVo data : dataList) {
                        valueTextMap.put(data.getText(), data.getValue());
                    }
                    if (isMultiple) {
                        JSONArray jsonArray = new JSONArray();
                        for (String value : values) {
                            jsonArray.add(valueTextMap.get(value));
                        }
                        result = jsonArray;
                    } else {
                        result = valueTextMap.get(values.get(0));
                    }
                }

            } else if ("matrix".equals(dataSource)) {
                ValueTextVo mapping = JSON.toJavaObject(config.getJSONObject("mapping"), ValueTextVo.class);
                if (Objects.equals(mapping.getText(), mapping.getValue())) {
                    List<String> dataList = new ArrayList<>();
                    for (String value : values) {
                        dataList.add(value + "&=&" + value);
                    }
                    return dataList;
                }
                String matrixUuid = config.getString("matrixUuid");
                if (StringUtils.isNotBlank(matrixUuid)) {
                    MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
                    if (matrixVo == null) {
                        throw new MatrixNotFoundException(matrixUuid);
                    }
                    if ("cmdbci".equals(matrixVo.getType())) {
                        ApiVo api = PrivateApiComponentFactory.getApiByToken("matrix/column/data/search/forselect/new");
                        if (api != null) {
                            MyApiComponent myApiComponent = (MyApiComponent) PrivateApiComponentFactory.getInstance(api.getHandler());
                            if (myApiComponent != null) {
                                List<String> dataLsit = new ArrayList<>();
                                for (String value : values) {
                                    String compose = getValue(matrixUuid, mapping, value, myApiComponent);
                                    if (StringUtils.isNotBlank(compose)) {
                                        dataLsit.add(compose);
                                    }
                                }
                                return dataLsit;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    private String getValue(String matrixUuid, ValueTextVo mapping, String value, MyApiComponent myApiComponent) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("matrixUuid", matrixUuid);
            JSONArray columnList = new JSONArray();
            columnList.add(mapping.getValue());
            columnList.add(mapping.getText());
            paramObj.put("columnList", columnList);
            paramObj.put("keyword", value);
            paramObj.put("keywordColumn", mapping.getText());
            JSONObject resultObj = (JSONObject) myApiComponent.myDoService(paramObj);
            JSONArray tbodyList = resultObj.getJSONArray("tbodyList");
            for (int i = 0; i < tbodyList.size(); i++) {
                JSONObject firstObj = tbodyList.getJSONObject(i);
                JSONObject valueObj = firstObj.getJSONObject((String) mapping.getValue());
                return valueObj.getString("compose");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
