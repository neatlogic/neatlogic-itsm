package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.notify.core.NotifyPolicyHandlerFactory;
import neatlogic.framework.notify.exception.NotifyPolicyHandlerNotFoundException;
import neatlogic.framework.process.auth.PROCESS;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS.class)
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
            @Param(name = "tbodyList", explode = ConditionParamVo[].class, desc = "流程通知策略参数列表")
    })
    @Description(desc = "流程通知策略参数列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String notifyPolicyHandler = jsonObj.getString("notifyPolicyHandler");
        INotifyPolicyHandler handler = NotifyPolicyHandlerFactory.getHandler(notifyPolicyHandler);
        if (handler == null) {
            throw new NotifyPolicyHandlerNotFoundException(notifyPolicyHandler);
        }
        List<ConditionParamVo> systemParamList = handler.getSystemParamList();
        systemParamList.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
        List<ConditionParamVo> paramList = new ArrayList<>(systemParamList);
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
                    if (formHandler.getParamType() != null) {
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
