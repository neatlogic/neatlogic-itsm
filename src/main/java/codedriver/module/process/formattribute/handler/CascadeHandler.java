package codedriver.module.process.formattribute.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.matrix.dto.MatrixColumnVo;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.FormHandlerBase;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
import codedriver.framework.restful.core.IApiComponent;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.dto.ApiVo;

@Component
public class CascadeHandler extends FormHandlerBase {

    @Override
    public String getHandler() {
        return "formcascadelist";
    }

    @Override
    public String getHandlerName() {
        return "级联下拉";
    }

    @Override
    public String getHandlerType(String model) {
        return "cascadelist";
    }

    @Override
    public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
        return false;
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        Object dataObj = attributeDataVo.getDataObj();
        if (dataObj != null) {
            List<String> textList = new ArrayList<>();
            List<String> valueList = JSON.parseArray(JSON.toJSONString(dataObj), String.class);
            String dataSource = configObj.getString("dataSource");
            if ("static".equals(dataSource)) {
                if(CollectionUtils.isNotEmpty(valueList)) {
                    JSONArray dataList = configObj.getJSONArray("dataList");
                    for(String value : valueList) {
                        for(int i = 0; i < dataList.size(); i++) {
                            JSONObject dataObject = dataList.getJSONObject(i);
                            if(Objects.equals(dataObject.getString("value"), value)) {
                                textList.add(dataObject.getString("text"));
                                dataList = dataObject.getJSONArray("children");
                                break;
                            }
                        }
                    }
                }
            } else {// 其他，如动态数据源
                String matrixUuid = configObj.getString("matrixUuid");
                List<ValueTextVo> mappingList =
                    JSON.parseArray(JSON.toJSONString(configObj.getJSONArray("mapping")), ValueTextVo.class);
                if (StringUtils.isNotBlank(matrixUuid) && CollectionUtils.isNotEmpty(valueList)
                    && CollectionUtils.isNotEmpty(mappingList)) {
                    ApiVo api = PrivateApiComponentFactory.getApiByToken("matrix/column/data/search/forselect/new");
                    if (api != null) {
                        IApiComponent restComponent = PrivateApiComponentFactory.getInstance(api.getHandler());
                        if (restComponent != null) {
                            if (valueList.size() > 0 && mappingList.size() > 0) {
                                List<MatrixColumnVo> sourceColumnList = new ArrayList<>();
                                textList.add(getText(matrixUuid, mappingList.get(0), valueList.get(0), sourceColumnList,
                                    restComponent, api));
                                if (valueList.size() > 1 && mappingList.size() > 1) {
                                    textList.add(getText(matrixUuid, mappingList.get(1), valueList.get(1),
                                        sourceColumnList, restComponent, api));
                                    if (valueList.size() > 2 && mappingList.size() > 2) {
                                        textList.add(getText(matrixUuid, mappingList.get(2), valueList.get(2),
                                            sourceColumnList, restComponent, api));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return textList;
        }
        return dataObj;
    }

    private String getText(String matrixUuid, ValueTextVo mapping, String value, List<MatrixColumnVo> sourceColumnList,
        IApiComponent restComponent, ApiVo api) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String[] split = value.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER);
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("matrixUuid", matrixUuid);
            List<String> columnList = new ArrayList<>();
            columnList.add((String)mapping.getValue());
            columnList.add(mapping.getText());
            paramObj.put("columnList", columnList);
            sourceColumnList.add(new MatrixColumnVo((String)mapping.getValue(), split[0]));
            sourceColumnList.add(new MatrixColumnVo(mapping.getText(), split[1]));
            paramObj.put("sourceColumnList", sourceColumnList);
            JSONObject resultObj = (JSONObject)restComponent.doService(api, paramObj);
            JSONArray columnDataList = resultObj.getJSONArray("columnDataList");
            for (int i = 0; i < columnDataList.size(); i++) {
                JSONObject firstObj = columnDataList.getJSONObject(i);
                JSONObject textObj = firstObj.getJSONObject(mapping.getText());
                if (Objects.equals(textObj.getString("value"), split[1])) {
                    return textObj.getString("text");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return split[1];
    }

    @Override
    public String getIcon() {
        return "ts-formlist";
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
