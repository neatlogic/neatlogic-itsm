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
public class TextareaHandler extends FormHandlerBase {

    @Override
    public String getHandler() {
        return "formtextarea";
    }

    @Override
    public String getHandlerName() {
        return "文本域";
    }

    @Override
    public String getIcon() {
        return "ts-text";
    }

    @Override
    public String getHandlerType(String model) {
       /* if (model != null && model.equals(ProcessConditionModel.CUSTOM.getValue())) {
            return "input";
        }*/
        return "input";//富文本不管是哪种模式下过滤都是input
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

}
