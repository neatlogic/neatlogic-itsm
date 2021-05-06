package codedriver.module.process.api.process;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.form.constvalue.FormHandlerTypeBak;
import codedriver.framework.process.auth.PROCESS_BASE;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.process.constvalue.ProcessTaskParams;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessParamList extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/param/list";
    }

    @Override
    public String getName() {
        return "流程参数列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "formUuid", type = ApiParamType.STRING, desc = "流程绑定表单的uuid")})
    @Output({@Param(explode = ConditionParamVo[].class, desc = "流程参数列表")})
    @Description(desc = "流程参数列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray resultArray = new JSONArray();
        // 固定字段条件
        for (ProcessTaskParams processTaskParams : ProcessTaskParams.values()) {
            ConditionParamVo param = new ConditionParamVo();
            param.setName(processTaskParams.getValue());
            param.setLabel(processTaskParams.getText());
            param.setParamType(processTaskParams.getParamType().getName());
            param.setParamTypeName(processTaskParams.getParamType().getText());
            param.setFreemarkerTemplate(processTaskParams.getFreemarkerTemplate());
            param.setIsEditable(0);
            resultArray.add(param);
        }
        /** homeUrl参数 **/
        ConditionParamVo param = new ConditionParamVo();
        param.setName("homeUrl");
        param.setLabel("域名");
        param.setParamType(ParamType.STRING.getName());
        param.setParamTypeName(ParamType.STRING.getText());
        param.setIsEditable(0);
        resultArray.add(param);

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
                    if (formHandler != null && formHandler.getParamType() != null) {
                        conditionParamVo.setParamType(formHandler.getParamType().getName());
                        conditionParamVo.setParamTypeName(formHandler.getParamType().getText());
                    }
                    conditionParamVo.setIsEditable(0);
                    resultArray.add(conditionParamVo);
                }
            }
        }
        return resultArray;
    }

}
