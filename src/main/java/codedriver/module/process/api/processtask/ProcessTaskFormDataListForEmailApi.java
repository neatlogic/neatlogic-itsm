/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                formVersionVo.setFormConfig(processTaskVo.getFormConfig().toJSONString());
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
                            }
                        }
                    }
                }
            }
        }
        return processTaskFormAttributeDataList;
    }
}
