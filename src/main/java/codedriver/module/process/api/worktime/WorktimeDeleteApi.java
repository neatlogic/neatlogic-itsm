package codedriver.module.process.api.worktime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeDetailVo;

@Service
@Transactional
public class WorktimeDeleteApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "process/worktime/delete";
	}

	@Override
	public String getName() {
		return "工作时间窗口删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
	})
	@Description(desc = "工作时间窗口删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
			throw new WorktimeNotFoundException(uuid);
		}
		worktimeMapper.deleteWorktimeByUuid(uuid);
		worktimeMapper.deleteWorktimeDefineByWorktimeUuid(uuid);
		WorktimeDetailVo worktimeDetailVo = new WorktimeDetailVo();
		worktimeDetailVo.setWorktimeUuid(uuid);
		worktimeMapper.deleteWorktimeDetail(worktimeDetailVo);
		return null;
	}

}
