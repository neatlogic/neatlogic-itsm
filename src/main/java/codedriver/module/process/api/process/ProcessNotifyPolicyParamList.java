package codedriver.module.process.api.process;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.notify.core.INotifyPolicyHandler;
import codedriver.framework.notify.core.NotifyPolicyHandlerFactory;
import codedriver.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskParams;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessNotifyPolicyParamList extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/notify/policy/param/list";
    }

    @Override
    public String getName() {
        return "流程通知策略参数列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "notifyPolicyHandler", type = ApiParamType.STRING, isRequired = true, desc = "通知策略处理器"),
            @Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")
    })
    @Output({
            @Param(explode = ConditionParamVo[].class, desc = "流程参数列表")
    })
    @Description(desc = "流程通知策略参数列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String notifyPolicyHandler = jsonObj.getString("notifyPolicyHandler");
        INotifyPolicyHandler handler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyHandler);
        if (notifyPolicyHandler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyHandler);
        }
        List<ConditionParamVo> systemParamList = handler.getSystemParamList();
        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        List<ConditionParamVo> paramList = new ArrayList<>();
        paramList.addAll(systemParamList);
        // 表单条件
        String formUuid = jsonObj.getString("formUuid");
        if (StringUtils.isNotBlank(formUuid)) {
            List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            for (FormAttributeVo formAttributeVo : formAttrList) {
                IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (formHandler == null) {
                    continue;
                }
                if (formHandler.isConditionable()) {
                    ConditionParamVo conditionParamVo = new ConditionParamVo();
                    conditionParamVo.setName(formAttributeVo.getUuid());
                    conditionParamVo.setLabel(formAttributeVo.getLabel());
                    if (formHandler != null && formHandler.getParamType() != null) {
                        conditionParamVo.setParamType(formHandler.getParamType().getName());
                        conditionParamVo.setParamTypeName(formHandler.getParamType().getText());
                    }
                    conditionParamVo.setIsEditable(0);
                    conditionParamVo.setType("form");
                    paramList.add(conditionParamVo);
                }
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", paramList);
        return resultObj;
    }

}
