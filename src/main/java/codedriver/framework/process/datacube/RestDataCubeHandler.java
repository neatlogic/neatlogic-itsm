package codedriver.framework.process.datacube;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.process.dto.ProcessDataCubeVo;
import codedriver.framework.process.dto.ProcessDataCubeVo.DataCubeType;

@Component
public class RestDataCubeHandler implements IDataCubeHandler {

	@Override
	public String getType() {
		return DataCubeType.REST.getValue();
	}

	@Override
	public JSONArray getData(ProcessDataCubeVo dataCubeVo) {
		return new JSONArray();
	}

}
