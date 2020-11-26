package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeVo;

@Component
public class ProcessTaskWorkTimeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	WorktimeMapper worktimeMapper;
	@Override
	public String getName() {
		return "worktime";
	}

	@Override
	public String getDisplayName() {
		return "时间窗口";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String worktimeUuid = json.getString(this.getName());
		String worktimeName = StringUtils.EMPTY;
		if(StringUtils.isBlank(worktimeName)) {
			WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
			if(worktimeVo != null) {
				worktimeName = worktimeVo.getName();
			}
		}
		return worktimeName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 12;
	}

	@Override
	public Object getSimpleValue(JSONObject json) {
		String worktimeName = json.getString(this.getName());
		return worktimeName;
	}
}
