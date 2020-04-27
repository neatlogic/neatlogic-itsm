package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MatrixAttributeTypeListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "matrix/attribute/type";
	}

	@Override
	public String getName() {
		return "矩阵属性类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		 List<ValueTextVo> typeList = new ArrayList<>();
		for(ProcessMatrixAttributeType type : ProcessMatrixAttributeType.values()) {
			typeList.add(new ValueTextVo(type.getValue(), type.getText()));
		}
		return typeList;
	}

}
