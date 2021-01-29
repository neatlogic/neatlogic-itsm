package codedriver.module.process.formattribute.handler;

import java.util.List;

import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.dto.ApiVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.FormHandlerBase;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;

@Component
public class RadioHandler extends FormHandlerBase {

    @Override
    public String getHandler() {
        return "formradio";
    }

    @Override
    public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
        return false;
    }

    @Override
    public String getHandlerType(String model) {
        if (model != null && model.equals(ProcessConditionModel.CUSTOM.getValue())) {
            return "select";
        }
        return "radio";
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        String value = attributeDataVo.getData();
        if (StringUtils.isNotBlank(value)) {
            return getTextOrValue(value,configObj,ConversionType.TOTEXT.getValue());
        }
        return value;
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        Object result = null;
        if(CollectionUtils.isNotEmpty(values)){
            result = getTextOrValue(values.get(0),config,ConversionType.TOVALUE.getValue());
        }
        return result;
    }

    private Object getTextOrValue(String value,JSONObject configObj,String conversionType){
        Object result = null;
        String dataSource = configObj.getString("dataSource");
        if ("static".equals(dataSource)) {
            List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
            if (CollectionUtils.isNotEmpty(dataList)) {
                for (ValueTextVo data : dataList) {
                    if (ConversionType.TOTEXT.getValue().equals(conversionType) && value.equals(data.getValue())) {
                        result = data.getText();
                        break;
                    }else if(ConversionType.TOVALUE.getValue().equals(conversionType) && value.equals(data.getText())){
                        result = data.getValue();
                        break;
                    }
                }
            }
        } else if("matrix".equals(dataSource)) {// 其他，如动态数据源
            if (ConversionType.TOTEXT.getValue().equals(conversionType) && value.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
                result = value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER)[1];
            }else if(ConversionType.TOVALUE.getValue().equals(conversionType)){
                String matrixUuid = configObj.getString("matrixUuid");
                ValueTextVo mapping = JSON.toJavaObject(configObj.getJSONObject("mapping"), ValueTextVo.class);
                if (StringUtils.isNotBlank(matrixUuid) && StringUtils.isNotBlank(value)
                        && mapping != null) {
                    ApiVo api = PrivateApiComponentFactory.getApiByToken("matrix/column/data/search/forselect/new");
                    if(api != null){
                        IApiComponent restComponent = PrivateApiComponentFactory.getInstance(api.getHandler());
                        if (restComponent != null) {
                            result = getValue(matrixUuid, mapping, value, restComponent, api);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getHandlerName() {
        return "单选框";
    }

    @Override
    public String getIcon() {
        return "ts-complete";
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
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
