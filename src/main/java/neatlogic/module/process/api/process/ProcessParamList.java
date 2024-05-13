package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskParams;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessParamList extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/param/list";
    }

    @Override
    public String getName() {
        return "nmpap.processparamlist.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")})
    @Output({@Param(explode = ConditionParamVo[].class, desc = "流程参数列表")})
    @Description(desc = "nmpap.processparamlist.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        // 固定字段条件
        for (ProcessTaskParams processTaskParams : ProcessTaskParams.values()) {
            IConditionHandler condition = ConditionHandlerFactory.getHandler(processTaskParams.getValue());
            if (condition != null) {
                ConditionParamVo param = new ConditionParamVo();
                param.setName(processTaskParams.getValue());
                param.setLabel(processTaskParams.getText());
                ParamType paramType = condition.getParamType();
                if (paramType != null) {
                    param.setParamType(paramType.getName());
                    param.setParamTypeName(paramType.getText());
                }
//            param.setFreemarkerTemplate(processTaskParams.getFreemarkerTemplate());
                param.setIsEditable(0);
                param.setType(condition.getType());
                resultArray.add(param);
            }
        }
        /** homeUrl参数 **/
//        ConditionParamVo param = new ConditionParamVo();
//        param.setName("homeUrl");
//        param.setLabel("域名");
//        param.setParamType(ParamType.STRING.getName());
//        param.setParamTypeName(ParamType.STRING.getText());
//        param.setIsEditable(0);
//        param.setType("common");
//        resultArray.add(param);

        // 表单条件
        String formUuid = jsonObj.getString("formUuid");
        if (StringUtils.isNotBlank(formUuid)) {
            List<FormAttributeVo> formAttrList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            for (FormAttributeVo formAttributeVo : formAttrList) {
                IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if(formHandler == null){
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
                    conditionParamVo.setType(ProcessFieldType.FORM.getValue());
                    resultArray.add(conditionParamVo);
                }
            }
        }
        return resultArray;
    }

}
