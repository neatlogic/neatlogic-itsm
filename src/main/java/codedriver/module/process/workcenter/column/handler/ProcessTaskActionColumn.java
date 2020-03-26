package codedriver.module.process.workcenter.column.handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskActionColumn implements IWorkcenterColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterColumn.ACTION.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.ACTION.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		
		return "";
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
