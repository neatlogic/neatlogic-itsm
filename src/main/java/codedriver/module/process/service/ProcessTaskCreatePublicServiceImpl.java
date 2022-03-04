package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.exception.FormAttributeNotFoundException;
import codedriver.framework.matrix.core.IMatrixDataSourceHandler;
import codedriver.framework.matrix.core.MatrixDataSourceHandlerFactory;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixVo;
import codedriver.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import codedriver.framework.matrix.exception.MatrixNotFoundException;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNextStepIllegalException;
import codedriver.framework.process.exception.processtask.ProcessTaskNextStepOverOneException;
import codedriver.module.framework.form.attribute.handler.SelectHandler;
import codedriver.module.process.dao.mapper.ProcessMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
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
public class ProcessTaskCreatePublicServiceImpl implements ProcessTaskCreatePublicService {

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

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    /**
     * 创建工单
     *
     * @param paramObj
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject createProcessTask(JSONObject paramObj) throws Exception {
        JSONObject result = new JSONObject();
        //上报人，支持上报人uuid和上报人id入参
        String owner = paramObj.getString("owner");
        UserVo userVo = userMapper.getUserByUuid(owner);
        if (userVo == null) {
            userVo = userMapper.getUserByUserId(owner);
            if (userVo == null) {
                throw new UserNotFoundException(owner);
            }
            paramObj.put("owner", userVo.getUuid());
        }
        //处理channel，支持channelUuid和channelName入参
        String channel = paramObj.getString("channel");
        ChannelVo channelVo = channelMapper.getChannelByUuid(channel);
        if (channelVo == null) {
            channelVo = channelMapper.getChannelByName(channel);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channel);
            }
        }
        paramObj.put("channelUuid", channelVo.getUuid());
        //优先级
        String priority = paramObj.getString("priority");
        PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priority);
        if (priorityVo == null) {
            priorityVo = priorityMapper.getPriorityByName(priority);
            if (priorityVo == null) {
                throw new PriorityNotFoundException(priority);
            }
        }
        paramObj.put("priorityUuid", priorityVo.getUuid());
        //流程
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelVo.getUuid());
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        //如果表单属性数据列表，使用的唯一标识是label时，需要转换成attributeUuid
        JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
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
                                        throw new FormAttributeNotFoundException(label);
                                    }
                                    formAttributeData.put("attributeUuid", formAttributeVo.getUuid());
                                    formAttributeData.put("handler", formAttributeVo.getHandler());
                                    //目前仅需要转换formselect
                                    if (!Objects.equals((new SelectHandler()).getHandler(), formAttributeVo.getHandler())) {
                                        continue;
                                    }
                                    JSONObject config = JSONObject.parseObject(formAttributeVo.getConfig());
                                    JSONArray dataArray = new JSONArray();
                                    if (config.getBooleanValue("isMultiple")) {
                                        try {
                                            dataArray = formAttributeData.getJSONArray("dataList");
                                        } catch (JSONException ex) {
                                            throw new ParamIrregularException(label + ":dataList");
                                        }
                                    } else {
                                        String dataListStr = formAttributeData.getString("dataList");
                                        dataArray.add(dataListStr);
                                    }
                                    if (CollectionUtils.isEmpty(dataArray)) {
                                        continue;
                                    }
                                    List<String> dataList = dataArray.toJavaList(String.class);
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

        String reporter = paramObj.getString("reporter");
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
        paramObj.put("isNeedValid", 1);
        JSONObject saveResultObj = processTaskService.saveProcessTaskDraft(paramObj);

        //查询可执行下一 步骤
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setId(saveResultObj.getLong("processTaskId"));
        processTaskVo.setChannelUuid(channelVo.getUuid());
        List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(saveResultObj.getLong("processTaskStepId"), ProcessFlowDirection.FORWARD.getValue());
        if (nextStepIdList.isEmpty()) {
            throw new ProcessTaskNextStepIllegalException(processTaskVo.getId());
        }
        if (nextStepIdList.size() != 1) {
            throw new ProcessTaskNextStepOverOneException(processTaskVo.getId());
        }
        saveResultObj.put("nextStepId", nextStepIdList.get(0));

        //流转
        processTaskService.startProcessProcessTask(saveResultObj);

        result.put("processTaskId", saveResultObj.getString("processTaskId"));
        return result;
    }

    /**
     * 通过text 转换为实际的value
     *
     * @param values text
     * @param config form config
     * @return 转换后的值
     */
    private Object textConversionValue(List<String> values, JSONObject config) {
        Object result = null;
        if (CollectionUtils.isNotEmpty(values)) {
            boolean isMultiple = config.getBooleanValue("isMultiple");
            String dataSource = config.getString("dataSource");
            if ("static".equals(dataSource)) {
                List<ValueTextVo> dataList = JSON.parseArray(JSON.toJSONString(config.getJSONArray("dataList")), ValueTextVo.class);
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
                    if (isMultiple) {
                        List<String> dataList = new ArrayList<>();
                        for (String value : values) {
                            dataList.add(value + "&=&" + value);
                        }
                        return dataList;
                    } else {
                        String value = values.get(0);
                        return value + "&=&" + value;
                    }
                }
                String matrixUuid = config.getString("matrixUuid");
                if (StringUtils.isNotBlank(matrixUuid)) {
                    MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
                    if (matrixVo == null) {
                        throw new MatrixNotFoundException(matrixUuid);
                    }
                    if (isMultiple) {
                        List<String> dataLsit = new ArrayList<>();
                        for (String value : values) {
                            String compose = getValue(matrixUuid, mapping, value);
                            if (StringUtils.isNotBlank(compose)) {
                                dataLsit.add(compose);
                            }
                        }
                        return dataLsit;
                    } else {
                        return getValue(matrixUuid, mapping, values.get(0));
                    }
                }
            }
        }
        return result;
    }

    private String getValue(String matrixUuid, ValueTextVo mapping, String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            MatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
            if (matrixVo == null) {
                throw new MatrixNotFoundException(matrixUuid);
            }
            IMatrixDataSourceHandler matrixDataSourceHandler = MatrixDataSourceHandlerFactory.getHandler(matrixVo.getType());
            if (matrixDataSourceHandler == null) {
                throw new MatrixDataSourceHandlerNotFoundException(matrixVo.getType());
            }
            MatrixDataVo dataVo = new MatrixDataVo();
            dataVo.setMatrixUuid(matrixUuid);
            List<String> columnList = new ArrayList<>();
            columnList.add((String) mapping.getValue());
            columnList.add(mapping.getText());
            dataVo.setColumnList(columnList);
            dataVo.setKeyword(value);
            dataVo.setKeywordColumn(mapping.getText());
            List<Map<String, JSONObject>> tbodyList = matrixDataSourceHandler.searchTableColumnData(dataVo);
            for (Map<String, JSONObject> firstObj : tbodyList) {
                JSONObject valueObj = firstObj.get(mapping.getValue());
                return valueObj.getString("compose");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
