package codedriver.module.process.formattribute.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            String dataSource = configObj.getString("dataSource");
            if ("static".equals(dataSource)) {
                List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
                if (CollectionUtils.isNotEmpty(dataList)) {
                    for (ValueTextVo data : dataList) {
                        if (value.equals(data.getValue())) {
                            return data.getText();
                        }
                    }
                }
            } else {// 其他，如动态数据源
                if (value.contains(IFormAttributeHandler.SELECT_COMPOSE_JOINER)) {
                    return value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER)[1];
                }
            }
        }

        return value;
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        Object result = null;
        if(CollectionUtils.isNotEmpty(values)){
            String dataSource = config.getString("dataSource");
            if ("static".equals(dataSource)) {
                List<ValueTextVo> dataList =
                        JSON.parseArray(JSON.toJSONString(config.getJSONArray("dataList")), ValueTextVo.class);
                if (CollectionUtils.isNotEmpty(dataList)) {
                    Map<String, Object> valueTextMap = new HashMap<>();
                    for (ValueTextVo data : dataList) {
                        valueTextMap.put(data.getText(), data.getValue());
                    }
                    result = valueTextMap.get(values.get(0));
                }
            }else if("matrix".equals(dataSource)){
                String matrixUuid = config.getString("matrixUuid");
                ValueTextVo mapping = JSON.toJavaObject(config.getJSONObject("mapping"), ValueTextVo.class);
                if (StringUtils.isNotBlank(matrixUuid) && CollectionUtils.isNotEmpty(values)
                        && mapping != null) {
                    ApiVo api = PrivateApiComponentFactory.getApiByToken("matrix/column/data/search/forselect/new");
                    if(api != null){
                        IApiComponent restComponent = PrivateApiComponentFactory.getInstance(api.getHandler());
                        if (restComponent != null) {
                            result = getValue(matrixUuid, mapping, values.get(0), restComponent, api);
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
