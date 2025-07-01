/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.notify.handler.param;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FormJsonParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.FORM_JSON.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskVo.getId());
        if (processTaskFormVo == null) {
            return null;
        }
        if (StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            return null;
        }
        Map<String, AttributeDataVo> attributeDataMap = new HashMap<>();
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(processTaskVo.getId());
        if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
            attributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
        }
        JSONArray attributeExtendedDataList = new JSONArray();
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormConfig(JSONObject.parseObject(processTaskFormVo.getFormContent()));
        String mainSceneUuid = formVersionVo.getFormConfig().getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
        for (FormAttributeVo formAttribute : formAttributeList) {
            AttributeDataVo attributeDataVo = attributeDataMap.get(formAttribute.getUuid());
            if (attributeDataVo == null) {
                attributeDataVo = new ProcessTaskFormAttributeDataVo();
                attributeDataVo.setAttributeUuid(formAttribute.getUuid());
                attributeDataVo.setHandler(formAttribute.getHandler());
            }
            attributeDataVo.setAttributeKey(formAttribute.getKey());
            attributeDataVo.setAttributeLabel(formAttribute.getLabel());
            IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttribute.getHandler());
            if (handler != null) {
                Object value = handler.dataTransformationForEmail(attributeDataVo, formAttribute.getConfig());
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("attributeKey", attributeDataVo.getAttributeKey());
                jsonObj.put("attributeLabel", attributeDataVo.getAttributeLabel());
                jsonObj.put("extendedData", value);
                attributeExtendedDataList.add(jsonObj);
            }
        }
        return attributeExtendedDataList;
    }
}
