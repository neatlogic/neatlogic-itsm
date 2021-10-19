/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class FormParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.FORM.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskVo.getId());
            if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
                String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                if (StringUtils.isNotBlank(formContent)) {
                    List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskVo.getId());
                    if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
                        Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
                        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataVoList = new ArrayList<>();
                        FormVersionVo formVersionVo = new FormVersionVo();
                        formVersionVo.setFormConfig(formContent);
                        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                        for (FormAttributeVo formAttribute : formAttributeList) {
                            ProcessTaskFormAttributeDataVo attributeDataVo = processTaskFormAttributeDataMap.get(formAttribute.getUuid());
                            if (attributeDataVo != null) {
                                attributeDataVo.setLabel(formAttribute.getLabel());
                                if (attributeDataVo.getData() != null) {
                                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttribute.getHandler());
                                    if (handler != null) {
                                        Object value = handler.dataTransformationForEmail(attributeDataVo, JSONObject.parseObject(formAttribute.getConfig()));
                                        attributeDataVo.setDataObj(value);
                                        processTaskFormAttributeDataVoList.add(attributeDataVo);
                                    }
                                }
                            }
                        }
                        return processTaskFormAttributeDataVoList;
                    }
                }
            }
        }
        return null;
    }
}
