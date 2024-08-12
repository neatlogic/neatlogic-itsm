/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.fulltextindex.utils.FullTextIndexUtil;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.util.FormUtil;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.stephandler.component.ConditionProcessComponent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProcessTaskFormAttributeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getName() {
        return ProcessFieldType.FORM.getValue();
    }

    @Override
    public String getDisplayName() {
        return ProcessFieldType.FORM.getName();
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return null;
    }

    @Override
    public String getType() {
        return ProcessFieldType.FORM.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        return null;
    }

    @Override
    public Integer getSort() {
        return null;
    }

    @Override
    public ParamType getParamType() {
        return null;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (MapUtils.isNotEmpty(config)) {
            FormAttributeVo formAttribute = config.getObject("formAttribute", FormAttributeVo.class);
            if (value != null) {
                IFormAttributeDataConversionHandler formAttributeHandler =
                        FormAttributeDataConversionHandlerFactory.getHandler(formAttribute.getHandler());
                if (formAttributeHandler != null) {
                    if (Objects.equals(formAttribute.getHandler(), FormHandler.FORMDATE.getHandler())
                            || Objects.equals(formAttribute.getHandler(), FormHandler.FORMTIME.getHandler())) {
                        if (value instanceof String) {
                            return value;
                        } else if (value instanceof JSONArray) {
                            List<String> textList = ((JSONArray) value).toJavaList(String.class);
                            return String.join("-", textList);
                        }
                    } else {
                        AttributeDataVo attributeDataVo = new AttributeDataVo();
                        attributeDataVo.setAttributeUuid(formAttribute.getUuid());
                        attributeDataVo.setHandler(formAttribute.getHandler());
                        if (value instanceof String) {
                            attributeDataVo.setData((String) value);
                        } else if (value instanceof JSONArray) {
                            attributeDataVo.setData(JSON.toJSONString(value));
                        }

                        Object text = formAttributeHandler.valueConversionText(attributeDataVo, formAttribute.getConfig());
                        if (text instanceof String) {
                            return text;
                        } else if (text instanceof List) {
                            List<String> textList = JSON.parseArray(JSON.toJSONString(text), String.class);
                            if (FormHandler.FORMCASCADER.getHandler().equals(formAttribute.getHandler())) {
                                return String.join("/", textList);
                            } else {
                                return String.join("、", textList);
                            }
                        }
                        return text;
                    }
                }
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        ConditionVo conditionVo = groupVo.getConditionList().get(index);
        if (StringUtils.isBlank(conditionVo.getHandler())) {
            throw new ParamIrregularException("Condition", " lost handler");
        }
        IFormAttributeHandler formAttributeHandler = FormAttributeHandlerFactory.getHandler(conditionVo.getHandler());
        if (formAttributeHandler == null) {
            throw new ParamIrregularException("Condition", " handler illegal");
        }
        //补充服务目录条件
        List<String> channelUuidList = groupVo.getChannelUuidList();
        if (CollectionUtils.isEmpty(channelUuidList)) {
            throw new ParamIrregularException("ConditionGroup", " lost channelUuidList");
        }
        ConditionVo channelCondition = new ConditionVo();
        channelCondition.setName("channel");
        channelCondition.setLabel("服务");
        channelCondition.setType("common");
        channelCondition.setExpression(Expression.INCLUDE.getExpression());
        channelCondition.setValueList(channelUuidList);
        getSimpleSqlConditionWhere(channelCondition, sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue());
        sqlSb.append(" and ");
        JSONArray valueArray = JSON.parseArray(conditionVo.getValueList().toString());
        if (FormHandler.FORMDATE.getHandler().equals(conditionVo.getHandler())) {
            dateSqlBuild(valueArray, conditionVo, sqlSb);
        } else {
            defaultSqlBuild(valueArray, conditionVo, formAttributeHandler, sqlSb);
        }
    }

    /**
     * date的sql拼凑
     *
     * @param valueArray  条件的值
     * @param conditionVo 条件
     * @param sqlSb       拼凑的sql
     */
    private void dateSqlBuild(JSONArray valueArray, ConditionVo conditionVo, StringBuilder sqlSb) {
        if (CollectionUtils.isEmpty(valueArray)) {
            sqlSb.append(String.format(" EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s') ",
                    conditionVo.getName()));
        } else {
            JSONObject value = valueArray.getJSONObject(0);
            SimpleDateFormat format = new SimpleDateFormat(TimeUtil.YYYY_MM_DD_HH_MM_SS);
            String startTime;
            String endTime;
            if (value.containsKey(ProcessWorkcenterField.STARTTIME.getValuePro())) {
                startTime = format.format(new Date(value.getLong(ProcessWorkcenterField.STARTTIME.getValuePro())));
                endTime = format.format(new Date(value.getLong(ProcessWorkcenterField.ENDTIME.getValuePro())));
            } else {
                startTime = TimeUtil.timeTransfer(value.getInteger("timeRange"), value.getString("timeUnit"));
                endTime = TimeUtil.timeNow();
            }
            sqlSb.append(String.format(" EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s' AND fw.word between '%s' and '%s' ) ",
                    conditionVo.getName(), startTime, endTime));

        }
    }

    /**
     * 除date外的sql拼凑
     *
     * @param valueArray           条件的值
     * @param conditionVo          条件
     * @param formAttributeHandler 表单处理器
     * @param sqlSb                拼凑的sql
     */
    private void defaultSqlBuild(JSONArray valueArray, ConditionVo conditionVo, IFormAttributeHandler formAttributeHandler, StringBuilder sqlSb) {
        if (Arrays.asList(Expression.EXCLUDE.getExpression(), Expression.ISNULL.getExpression()).contains(conditionVo.getExpression())) {
            sqlSb.append(" not ");
        }
        if (CollectionUtils.isEmpty(valueArray)) {
            sqlSb.append(String.format(" EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s') ",
                    conditionVo.getName()));
        } else {
            List<String> valueList = new ArrayList<>();
            for (Object valueObj : valueArray) {
                if (valueObj instanceof JSONObject) {
                    JSONObject jsonObj = (JSONObject) valueObj;
                    String value = jsonObj.getString("value");
                    String text = jsonObj.getString("text");
                    String[] valueTmpList = {value, text};
                    for (String valueTmp : valueTmpList) {
                        //如果需要分词，则搜索的时候关键字也需分词搜索
                        if (Boolean.TRUE.equals(formAttributeHandler.isNeedSliceWord())) {
                            Set<String> sliceKeySet = null;
                            sliceKeySet = FullTextIndexUtil.sliceKeyword(valueTmp);
                            if (CollectionUtils.isNotEmpty(sliceKeySet)) {
                                valueList.addAll(new ArrayList<>(sliceKeySet));
                            }
                        } else {//否则直接md5作为整体搜索
                            valueList.add(Md5Util.encryptMD5(valueTmp).toLowerCase(Locale.ROOT));
                        }
                    }
                } else {
                    String valueTmp = valueObj.toString();
                    //如果需要分词，则搜索的时候关键字也需分词搜索
                    if (Boolean.TRUE.equals(formAttributeHandler.isNeedSliceWord())) {
                        Set<String> sliceKeySet = null;
                        sliceKeySet = FullTextIndexUtil.sliceKeyword(valueTmp);
                        if (CollectionUtils.isNotEmpty(sliceKeySet)) {
                            valueList.addAll(new ArrayList<>(sliceKeySet));
                        }
                    } else {//否则直接md5作为整体搜索
                        valueList.add(Md5Util.encryptMD5(valueTmp).toLowerCase(Locale.ROOT));
                    }
                }
            }
            sqlSb.append(String.format(" EXISTS (SELECT 1 FROM `fulltextindex_word` fw JOIN fulltextindex_field_process ff ON fw.id = ff.`word_id` JOIN `fulltextindex_target_process` ft ON ff.`target_id` = ft.`target_id` WHERE ff.`target_id` = pt.id  AND ft.`target_type` = 'processtask' AND ff.`target_field` = '%s' AND fw.word IN ('%s')) ",
                    conditionVo.getName(), String.join("','", valueList)));
        }
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        JSONObject resultObj = new JSONObject();
        List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTag(processTaskStepVo.getProcessTaskId(), ConditionProcessComponent.FORM_EXTEND_ATTRIBUTE_TAG);
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskIdAndTag(processTaskStepVo.getProcessTaskId(), ConditionProcessComponent.FORM_EXTEND_ATTRIBUTE_TAG);
            for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                FormAttributeVo formAttributeVo = formAttributeMap.get(processTaskFormAttributeDataVo.getAttributeUuid());
                if (formAttributeVo == null) {
                    continue;
                }
                if (Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMRADIO.getHandler())
                        || Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMCHECKBOX.getHandler())
                        || Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMSELECT.getHandler())) {
                    Object value = FormUtil.getFormSelectAttributeValueByOriginalValue(processTaskFormAttributeDataVo.getDataObj());
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), value);
                } else {
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        return resultObj;
    }

    @Override
    public Object getConditionParamDataNew(ProcessTaskStepVo processTaskStepVo, String formTag) {
        JSONObject resultObj = new JSONObject();
        List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTagNew(processTaskStepVo.getProcessTaskId(), formTag);
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskIdAndTagNew(processTaskStepVo.getProcessTaskId(), formTag);
            for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                FormAttributeVo formAttributeVo = formAttributeMap.get(processTaskFormAttributeDataVo.getAttributeUuid());
                if (formAttributeVo == null) {
                    continue;
                }
                Object dataObj = processTaskFormAttributeDataVo.getDataObj();
                JSONObject componentObj = new JSONObject();
                componentObj.put("handler", formAttributeVo.getHandler());
                componentObj.put("uuid", formAttributeVo.getUuid());
                componentObj.put("label", formAttributeVo.getLabel());
                componentObj.put("config", formAttributeVo.getConfig());
                componentObj.put("type", formAttributeVo.getType());
                List<FormAttributeVo> downwardFormAttributeList = FormUtil.getFormAttributeList(componentObj, null);
                if (downwardFormAttributeList.size() > 1 && dataObj instanceof JSONArray) {
                    // 表格组件
                    JSONArray dataList = new JSONArray();
                    JSONArray dataArray = JSONArray.parseArray(((JSONArray) dataObj).toJSONString());
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject newRowObj = new JSONObject();
                        JSONObject rowObj = dataArray.getJSONObject(i);
                        for (FormAttributeVo downwardFormAttribute : downwardFormAttributeList) {
                            Object value = rowObj.get(downwardFormAttribute.getUuid());
                            if (value != null) {
                                IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(downwardFormAttribute.getHandler());
                                if (handler != null) {
                                    Object simpleValue = handler.getSimpleValue(value);
                                    newRowObj.put(downwardFormAttribute.getUuid(), simpleValue);
                                } else {
                                    newRowObj.put(downwardFormAttribute.getUuid(), value);
                                }
                            }
                        }
                        dataList.add(newRowObj);
                    }
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataList);
                } else {
                    // 非表格组件
                    IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                    if (handler != null) {
                        Object simpleValue = handler.getSimpleValue(dataObj);
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), simpleValue);
                    } else {
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataObj);
                    }
                }
//                if (Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMRADIO.getHandler())
//                        || Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMCHECKBOX.getHandler())
//                        || Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMSELECT.getHandler())) {
//                    Object value = FormUtil.getFormSelectAttributeValueByOriginalValue(processTaskFormAttributeDataVo.getDataObj());
//                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), value);
//                } else {
//                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
//                }
            }
        }
        return resultObj;
    }

    @Override
    public Object getConditionParamDataForHumanization(ProcessTaskStepVo processTaskStepVo) {
        JSONObject resultObj = new JSONObject();
        List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTag(processTaskStepVo.getProcessTaskId(), ConditionProcessComponent.FORM_EXTEND_ATTRIBUTE_TAG);
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskIdAndTag(processTaskStepVo.getProcessTaskId(), ConditionProcessComponent.FORM_EXTEND_ATTRIBUTE_TAG);
            for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                FormAttributeVo formAttributeVo = formAttributeMap.get(processTaskFormAttributeDataVo.getAttributeUuid());
                if (formAttributeVo == null) {
                    continue;
                }
                if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMRADIO.getHandler())
                        || Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMCHECKBOX.getHandler())
                        || Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMSELECT.getHandler())) {
                    Object value = FormUtil.getFormSelectAttributeValueByOriginalValue(processTaskFormAttributeDataVo.getDataObj());
                    //另存一份label为key的数据，给条件路由的自定义脚本消费
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), value);
                } else {
                    String data = processTaskFormAttributeDataVo.getData();
                    if (StringUtils.isNotBlank(data)) {
                        JSONObject componentObj = new JSONObject();
                        componentObj.put("handler", formAttributeVo.getHandler());
                        componentObj.put("uuid", formAttributeVo.getUuid());
                        componentObj.put("label", formAttributeVo.getLabel());
                        componentObj.put("config", formAttributeVo.getConfig());
                        componentObj.put("type", formAttributeVo.getType());
                        List<FormAttributeVo> downwardFormAttributeList = FormUtil.getFormAttributeList(componentObj, null);
                        for (FormAttributeVo downwardFormAttribute : downwardFormAttributeList) {
                            if (data.contains(downwardFormAttribute.getUuid())) {
                                data = data.replace(downwardFormAttribute.getUuid(), downwardFormAttribute.getLabel());
                            }
                        }
                        processTaskFormAttributeDataVo.setData(data);
                    }
                    //另存一份label为key的数据，给条件路由的自定义脚本消费
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        return resultObj;
    }

    @Override
    public Object getConditionParamDataForHumanizationNew(ProcessTaskStepVo processTaskStepVo, String formTag) {
        JSONObject resultObj = new JSONObject();
        List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTagNew(processTaskStepVo.getProcessTaskId(), formTag);
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskIdAndTagNew(processTaskStepVo.getProcessTaskId(), formTag);
            for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                FormAttributeVo formAttributeVo = formAttributeMap.get(processTaskFormAttributeDataVo.getAttributeUuid());
                if (formAttributeVo == null) {
                    continue;
                }

                Object dataObj = processTaskFormAttributeDataVo.getDataObj();
                JSONObject componentObj = new JSONObject();
                componentObj.put("handler", formAttributeVo.getHandler());
                componentObj.put("uuid", formAttributeVo.getUuid());
                componentObj.put("label", formAttributeVo.getLabel());
                componentObj.put("config", formAttributeVo.getConfig());
                componentObj.put("type", formAttributeVo.getType());
                List<FormAttributeVo> downwardFormAttributeList = FormUtil.getFormAttributeList(componentObj, null);
                if (downwardFormAttributeList.size() > 1 && dataObj instanceof JSONArray) {
                    // 表格组件
                    JSONArray dataList = new JSONArray();
                    JSONArray dataArray = JSONArray.parseArray(((JSONArray) dataObj).toJSONString());
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject newRowObj = new JSONObject();
                        JSONObject rowObj = dataArray.getJSONObject(i);
                        for (FormAttributeVo downwardFormAttribute : downwardFormAttributeList) {
                            Object value = rowObj.get(downwardFormAttribute.getUuid());
                            if (value != null) {
                                IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(downwardFormAttribute.getHandler());
                                if (handler != null) {
                                    Object simpleValue = handler.getSimpleValue(value);
                                    newRowObj.put(downwardFormAttribute.getLabel(), simpleValue);
                                } else {
                                    newRowObj.put(downwardFormAttribute.getLabel(), value);
                                }
                            }
                        }
                        dataList.add(newRowObj);
                    }
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), dataList);
                } else {
                    // 非表格组件
                    IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                    if (handler != null) {
                        Object simpleValue = handler.getSimpleValue(dataObj);
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), simpleValue);
                    } else {
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), dataObj);
                    }
                }
//                if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMRADIO.getHandler())
//                        || Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMCHECKBOX.getHandler())
//                        || Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMSELECT.getHandler())) {
//                    Object value = FormUtil.getFormSelectAttributeValueByOriginalValue(processTaskFormAttributeDataVo.getDataObj());
//                    //另存一份label为key的数据，给条件路由的自定义脚本消费
//                    resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), value);
//                } else {
//                    String data = processTaskFormAttributeDataVo.getData();
//                    if (StringUtils.isNotBlank(data)) {
//                        JSONObject componentObj = new JSONObject();
//                        componentObj.put("handler", formAttributeVo.getHandler());
//                        componentObj.put("uuid", formAttributeVo.getUuid());
//                        componentObj.put("label", formAttributeVo.getLabel());
//                        componentObj.put("config", formAttributeVo.getConfig());
//                        componentObj.put("type", formAttributeVo.getType());
//                        List<FormAttributeVo> downwardFormAttributeList = FormUtil.getFormAttributeList(componentObj, null);
//                        for (FormAttributeVo downwardFormAttribute : downwardFormAttributeList) {
//                            if (data.contains(downwardFormAttribute.getUuid())) {
//                                data = data.replace(downwardFormAttribute.getUuid(), downwardFormAttribute.getLabel());
//                            }
//                        }
//                        processTaskFormAttributeDataVo.setData(data);
//                    }
//                    //另存一份label为key的数据，给条件路由的自定义脚本消费
//                    resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), processTaskFormAttributeDataVo.getDataObj());
//                }
            }
        }
        return resultObj;
    }
}
