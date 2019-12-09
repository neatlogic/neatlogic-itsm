package codedriver.framework.process.datacube;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.process.dto.ProcessDataCubeVo;

public interface IDataCubeHandler {
	public String getType();
	public JSONArray getData(ProcessDataCubeVo dataCubeVo);
}
