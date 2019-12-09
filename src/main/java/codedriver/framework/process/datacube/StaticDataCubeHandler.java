package codedriver.framework.process.datacube;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;

import codedriver.module.process.dto.ProcessDataCubeVo;
import codedriver.module.process.dto.ProcessDataCubeVo.DataCubeType;

@Component
public class StaticDataCubeHandler implements IDataCubeHandler {

	@Override
	public String getType() {
		return DataCubeType.STATIC.getValue();
	}

	@Override
	public JSONArray getData(ProcessDataCubeVo dataCubeVo) {
		if(StringUtils.isNotBlank(dataCubeVo.getValue())) {
			try {
				return JSONArray.parseArray(dataCubeVo.getValue());
			}catch(Exception ex) {
				
			}
		}
		return new JSONArray();
	}

}
