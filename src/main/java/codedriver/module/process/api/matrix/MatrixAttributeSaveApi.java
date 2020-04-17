package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixAttributeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:07
 **/
@Service
public class MatrixAttributeSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixAttributeService attributeService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/save";
    }

    @Override
    public String getName() {
        return "矩阵属性保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "attributeArray", desc = "属性数据", type = ApiParamType.JSONARRAY, isRequired = true)})
    @Description( desc = "矩阵属性保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ProcessMatrixAttributeVo> attributeVoList = new ArrayList<>();
        String matrixUuid = jsonObj.getString("matrixUuid");
    	if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
        JSONArray attributeArray = jsonObj.getJSONArray("attributeArray");
        for (int i = 0;i < attributeArray.size(); i++){
            JSONObject attributeObj = attributeArray.getJSONObject(i);
            ProcessMatrixAttributeVo attributeVo = new ProcessMatrixAttributeVo();
            attributeVo.setMatrixUuid(matrixUuid);
            attributeVo.setName(attributeObj.getString("name"));
            if (attributeObj.containsKey("uuid")){
                attributeVo.setUuid(attributeObj.getString("uuid"));
            }
            attributeVo.setConfig(attributeObj.toString());
            attributeVoList.add(attributeVo);
        }
        attributeService.saveMatrixAttribute(attributeVoList, matrixUuid);
        return "";
    }
}
