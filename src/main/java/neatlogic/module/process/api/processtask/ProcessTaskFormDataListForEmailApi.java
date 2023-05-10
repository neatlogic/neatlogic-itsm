/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/8/19 7:20
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormDataListForEmailApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/form/data/list/foremail";
    }

    @Override
    public String getName() {
        return "查询工单表单数据列表（邮件）";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Output({
            @Param(explode = ProcessTaskFormAttributeDataVo[].class, desc = "数据")
    })
    @Description(desc = "查询工单步骤表单数据")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        processTaskService.setProcessTaskFormInfo(processTaskVo);
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskVo.getProcessTaskFormAttributeDataList();
        if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
            Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
            if (MapUtils.isNotEmpty(processTaskVo.getFormConfig())) {
                FormVersionVo formVersionVo = new FormVersionVo();
                formVersionVo.setFormConfig(processTaskVo.getFormConfig());
                List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                for (FormAttributeVo formAttribute : formAttributeList) {
                    ProcessTaskFormAttributeDataVo attributeDataVo = processTaskFormAttributeDataMap.get(formAttribute.getUuid());
                    if (attributeDataVo != null) {
                        attributeDataVo.setAttributeLabel(formAttribute.getLabel());
                        if (attributeDataVo.getData() != null) {
                            IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttribute.getHandler());
                            if (handler != null) {
//                                long startTime = System.currentTimeMillis();
//                                Object value = handler.dataTransformationForEmail(attributeDataVo, JSONObject.parseObject(formAttribute.getConfig()));
//                                System.out.println(formAttribute.getHandler() + "-" + (System.currentTimeMillis() - startTime));
//                                attributeDataVo.setDataObj(value);
                                long startTime2 = System.currentTimeMillis();
                                JSONObject valueObj = handler.getDetailedData(attributeDataVo, JSONObject.parseObject(formAttribute.getConfig()));
                                System.out.println(formAttribute.getHandler() + "-" + (System.currentTimeMillis() - startTime2));
                                attributeDataVo.setDataObj(valueObj);
                            }
                        }
                    }
                }
            }
        }
        return processTaskFormAttributeDataList;
    }
}
