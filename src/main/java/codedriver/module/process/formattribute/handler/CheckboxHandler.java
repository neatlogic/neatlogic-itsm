package codedriver.module.process.formattribute.handler;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.FormHandlerBase;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.dto.ApiVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CheckboxHandler extends FormHandlerBase {

    @Override
    public String getHandler() {
        return "formcheckbox";
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
        return "checkbox";
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        List<String> valueList = JSON.parseArray(JSON.toJSONString(attributeDataVo.getDataObj()), String.class);
        if (CollectionUtils.isNotEmpty(valueList)) {
            return getTextOrValue(configObj,valueList,ConversionType.TOTEXT.getValue());
        } else {
            return valueList;
        }
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        Object result = null;
        if (CollectionUtils.isNotEmpty(values)) {
            result = getTextOrValue(config,values,ConversionType.TOVALUE.getValue());
        }
        return result;
    }

    private Object getTextOrValue(JSONObject configObj,List<String> valueList,String conversionType){
        List<String> result = new ArrayList<>();
        String dataSource = configObj.getString("dataSource");
        if ("static".equals(dataSource)) {
            Map<Object, String> valueTextMap = new HashMap<>();
            List<ValueTextVo> dataList =
                    JSON.parseArray(JSON.toJSONString(configObj.getJSONArray("dataList")), ValueTextVo.class);
            if (CollectionUtils.isNotEmpty(dataList)) {
                for (ValueTextVo data : dataList) {
                    valueTextMap.put(data.getValue(), data.getText());
                }
            }
            if(ConversionType.TOTEXT.getValue().equals(conversionType)){
                for (String value : valueList) {
                    String text = valueTextMap.get(value);
                    if (text != null) {
                        result.add(text);
                    } else {
                        result.add(value);
                    }
                }
            }else if(ConversionType.TOVALUE.getValue().equals(conversionType)){
                for (String value : valueList) {
                    result.add(valueTextMap.get(value));
                }
            }
        } else if("matrix".equals(dataSource)){// 其他，如动态数据源
            if(ConversionType.TOTEXT.getValue().equals(conversionType)){
                for (String value : valueList) {
                    if (value.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
                        result.add(value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER)[1]);
                    } else {
                        result.add(value);
                    }
                }
            }else if(ConversionType.TOVALUE.getValue().equals(conversionType)){
                String matrixUuid = configObj.getString("matrixUuid");
                ValueTextVo mapping = JSON.toJavaObject(configObj.getJSONObject("mapping"), ValueTextVo.class);
                if (StringUtils.isNotBlank(matrixUuid) && CollectionUtils.isNotEmpty(valueList)
                        && mapping != null) {
                    ApiVo api = PrivateApiComponentFactory.getApiByToken("matrix/column/data/search/forselect/new");
                    if (api != null) {
                        IApiComponent restComponent = PrivateApiComponentFactory.getInstance(api.getHandler());
                        if (restComponent != null) {
                            for (String value : valueList) {
                                result.add(getValue(matrixUuid, mapping, value, restComponent, api));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getHandlerName() {
        return "复选框";
    }

    @Override
    public String getIcon() {
        return "ts-check-square-o";
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

    @Override
    protected List<String> myIndexFieldContentList(String data){
        if(data.startsWith("[")&&data.endsWith("]")){
            JSONArray jsonArray = JSONArray.parseArray(data);
            return JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        }
        return null;
    }
}
