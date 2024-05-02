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
import com.google.common.base.Objects;
import neatlogic.framework.common.constvalue.ParamType;
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
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.fulltextindex.utils.FullTextIndexUtil;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessWorkcenterField;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.util.FormUtil;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.TimeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ProcessTaskFormAttributeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

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
            //表单属性的uuid
            String attributeUuid = config.getString("attributeUuid");
            //对应表单版本的form_config字段
            JSONObject formConfig = config.getJSONObject("formConfig");
            FormVersionVo formVersionVo = new FormVersionVo();
            formVersionVo.setFormConfig(formConfig);
            List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                for (FormAttributeVo formAttribute : formAttributeList) {
                    if (Objects.equal(attributeUuid, formAttribute.getUuid())) {
                        config.put("name", formAttribute.getLabel());
                        if (value != null) {
                            IFormAttributeDataConversionHandler formAttributeHandler =
                                    FormAttributeDataConversionHandlerFactory.getHandler(formAttribute.getHandler());
                            if (formAttributeHandler != null) {
                                if (Objects.equal(formAttribute.getHandler(), FormHandler.FORMDATE.getHandler())
                                        || Objects.equal(formAttribute.getHandler(), FormHandler.FORMTIME.getHandler())) {
                                    if (value instanceof String) {
                                        return value;
                                    } else if (value instanceof JSONArray) {
                                        List<String> textList = ((JSONArray) value).toJavaList(String.class);
                                        return String.join("-", textList);
                                    }
                                } else {
                                    AttributeDataVo attributeDataVo = new AttributeDataVo();
                                    attributeDataVo.setAttributeUuid(attributeUuid);
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
                }
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        ConditionVo conditionVo = conditionList.get(index);
        if (StringUtils.isBlank(conditionVo.getHandler())) {
            throw new ParamIrregularException("ConditionGroup", " lost handler");
        }
        IFormAttributeHandler formAttributeHandler = FormAttributeHandlerFactory.getHandler(conditionVo.getHandler());
        if (formAttributeHandler == null) {
            throw new ParamIrregularException("ConditionGroup", " handler illegal");
        }
        JSONArray valueArray = JSONArray.parseArray(conditionVo.getValueList().toString());
        if (CollectionUtils.isEmpty(valueArray)) {
            return;
        }
        if (FormHandler.FORMDATE.getHandler().equals(conditionVo.getHandler())) {
            JSONObject value = valueArray.getJSONObject(0);
            dateSqlBuild(value, conditionVo, sqlSb);
        } else {
            defaultSqlBuild(valueArray, conditionVo, formAttributeHandler, sqlSb);
        }
    }

    /**
     * date的sql拼凑
     *
     * @param value       条件的值
     * @param conditionVo 条件
     * @param sqlSb       拼凑的sql
     */
    private void dateSqlBuild(JSONObject value, ConditionVo conditionVo, StringBuilder sqlSb) {
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

    /**
     * 除date外的sql拼凑
     *
     * @param valueArray           条件的值
     * @param conditionVo          条件
     * @param formAttributeHandler 表单处理器
     * @param sqlSb                拼凑的sql
     */
    private void defaultSqlBuild(JSONArray valueArray, ConditionVo conditionVo, IFormAttributeHandler formAttributeHandler, StringBuilder sqlSb) {
        List<String> valueList = new ArrayList<>();
        for (Object valueObj : valueArray) {
            if (valueObj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) valueObj;
                String value = jsonObj.getString("value");
                String text = jsonObj.getString("text");
                String[] valueTmpList = {value, text};
                for (String valueTmp : valueTmpList) {
                    //如果需要分词，则搜索的时候关键字也需分词搜索
                    if (formAttributeHandler.isNeedSliceWord()) {
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
                if (formAttributeHandler.isNeedSliceWord()) {
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

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        JSONObject resultObj = new JSONObject();
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                resultObj.put("formConfig", formContent);
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(processTaskStepVo.getProcessTaskId());
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    if (java.util.Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMRADIO.getHandler())
                            || java.util.Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMCHECKBOX.getHandler())
                            || java.util.Objects.equals(processTaskFormAttributeDataVo.getHandler(), FormHandler.FORMSELECT.getHandler())) {
                        Object value = FormUtil.getFormSelectAttributeValueByOriginalValue(processTaskFormAttributeDataVo.getDataObj());
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), value);
                        //另存一份label为key的数据，给条件路由的自定义脚本消费
                        //resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), value);
                    } else {
                        resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                        //另存一份label为key的数据，给条件路由的自定义脚本消费
                        //resultObj.put(processTaskFormAttributeDataVo.getAttributeLabel(), processTaskFormAttributeDataVo.getDataObj());
                    }
                }
            }
        }
        return resultObj;
    }
}
