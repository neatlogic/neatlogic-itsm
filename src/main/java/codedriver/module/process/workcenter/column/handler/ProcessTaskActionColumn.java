package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;

@Component
public class ProcessTaskActionColumn implements IWorkcenterColumn{
	@Autowired
	CatalogMapper catalogMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterField.ACTION.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterField.ACTION.getName();
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

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

}
