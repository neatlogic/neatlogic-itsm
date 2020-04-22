package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalRequestHandlerNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.process.matrixrexternal.core.IMatrixExternalRequestHandler;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
@Service
public class MatrixExternalDataSearchApi extends ApiComponentBase {

//    @Autowired
//    private MatrixService matrixService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;

	@Override
	public String getToken() {
		return "matrix/external/data/search";
	}

	@Override
	public String getName() {
		return "外部数据源数据检索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({ 
		@Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING),
        @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
        @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
        @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
	})
	@Output({ @Param( name = "tbodyList", desc = "矩阵数据集合"),
	         @Param( name = "theadList", desc = "矩阵属性集合"),
	         @Param( explode = BasePageVo.class)})
	@Description( desc = "矩阵数据检索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }
//        JSONObject dataObj = matrixService.getMatrixExternalData(matrixUuid);
//        List<String> headerList = dataObj.getJSONArray("headerList").toJavaList(String.class);
//        List<String> columnList = dataObj.getJSONArray("columnList").toJavaList(String.class);
//        List<Map<String, String>> dataMapList= (List<Map<String,String>>) dataObj.get("dataMapList");
        ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        if(externalVo == null) {
        	throw new MatrixExternalNotFoundException(matrixUuid);
        }
        String plugin = externalVo.getPlugin();
        IMatrixExternalRequestHandler requestHandler = MatrixExternalRequestFactory.getHandler(plugin);
        if(requestHandler == null) {
        	throw new MatrixExternalRequestHandlerNotFoundException(plugin);
        }
        JSONObject externalObj = JSONObject.parseObject(externalVo.getConfig());
        if(MapUtils.isNotEmpty(externalObj)) {
            String url = externalObj.getString("url");
            if(StringUtils.isNotBlank(url)) {
            	String rootName = externalObj.getString("rootName");
            	//TODO url拼接分页参数
                JSONArray dataArray = requestHandler.dataHandler(url, rootName, externalObj);
                if (CollectionUtils.isNotEmpty(dataArray)){
                	JSONArray columnList = externalObj.getJSONArray("columnList");
                    List<String> headerList = new ArrayList<>();
                    List<String> attributeList = new ArrayList<>();
                    for (int i = 0; i < columnList.size(); i++){
                        JSONObject obj = columnList.getJSONObject(i);
                        headerList.add(obj.getString("text"));
                        attributeList.add(obj.getString("value"));
                    }
                    List<Map<String, String>> dataMapList = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++){
                        JSONObject obj = dataArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        for (String attribute : attributeList){
                            map.put(attribute, obj.getString(attribute));
                        }
                        dataMapList.add(map);
                    }
//                    returnObj.put("headerList", headerList);
//                    returnObj.put("columnList", attributeList);
//                    returnObj.put("dataMapList", dataMapList);
                }
            }
        }
		return null;
	}

}
