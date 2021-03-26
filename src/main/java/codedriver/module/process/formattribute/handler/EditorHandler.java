package codedriver.module.process.formattribute.handler;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.FormHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EditorHandler extends FormHandlerBase {

    @Override
    public String getHandler() {
        return "formeditor";
    }

    @Override
    public String getHandlerType(String model) {
        return "input";
    }

    @Override
    public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
        return false;
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        return attributeDataVo.getData();
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        if(CollectionUtils.isNotEmpty(values)){
            return values.get(0);
        }
        return null;
    }

    @Override
    public String getHandlerName() {
        return "富文本框";
    }

    @Override
    public String getIcon() {
        return "ts-viewmodule";
    }

    @Override
    public ParamType getParamType() {
        return ParamType.STRING;
    }

    @Override
    public String getDataType() {
        return "string";
    }

    @Override
    public boolean isConditionable() {
        return true;
    }

    @Override
    public boolean isShowable() {
        return true;
    }

    @Override
    public boolean isValueable() {
        return true;
    }

    @Override
    public boolean isFilterable() {
        return true;
    }

    @Override
    public boolean isExtendable() {
        return false;
    }

    @Override
    public String getModule() {
        return "process";
    }

    @Override
    public boolean isForTemplate() {
        return true;
    }

    @Override
    public boolean isAudit() {
        return true;
    }

    @Override
    public Boolean isUseFormConfig() {
        return false;
    }
}
