package neatlogic.module.process.service;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.FormHandlerBase;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormAttributeNotFoundException;
import neatlogic.framework.matrix.core.IMatrixDataSourceHandler;
import neatlogic.framework.matrix.core.MatrixDataSourceHandlerFactory;
import neatlogic.framework.matrix.dao.mapper.MatrixMapper;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixVo;
import neatlogic.framework.matrix.exception.MatrixDataSourceHandlerNotFoundException;
import neatlogic.framework.matrix.exception.MatrixNotFoundException;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.dto.ProcessFormVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNextStepIllegalException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNextStepOverOneException;
import neatlogic.module.framework.form.attribute.handler.SelectHandler;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
        if (StringUtils.isNotBlank(priority)) {
            PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priority);
            if (priorityVo == null) {
                priorityVo = priorityMapper.getPriorityByName(priority);
                if (priorityVo == null) {
                    throw new PriorityNotFoundException(priority);
                }
            }
            paramObj.put("priorityUuid", priorityVo.getUuid());
        }
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
                                    FormHandlerBase formAttributeHandler = (FormHandlerBase) FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                                    if (formAttributeHandler != null) {
                                        JSONObject config = JSONObject.parseObject(formAttributeVo.getConfig());
                                        Object dataList = formAttributeHandler.textConversionValue(formAttributeData.get("dataList"), config);
                                        formAttributeData.put("dataList", dataList);
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
        Long processTaskId = saveResultObj.getLong("processTaskId");
        List<Long> nextStepIdList = processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(saveResultObj.getLong("processTaskStepId"), ProcessFlowDirection.FORWARD.getValue());
        if (nextStepIdList.isEmpty()) {
            throw new ProcessTaskNextStepIllegalException(processTaskId);
        }
        if (nextStepIdList.size() != 1) {
            throw new ProcessTaskNextStepOverOneException(processTaskId);
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
