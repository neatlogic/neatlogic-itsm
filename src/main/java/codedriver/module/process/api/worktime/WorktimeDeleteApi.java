package codedriver.module.process.api.worktime;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeRangeVo;
import codedriver.framework.process.exception.worktime.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class WorktimeDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "worktime/delete";
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
		WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
		worktimeRangeVo.setWorktimeUuid(uuid);
		worktimeMapper.deleteWorktimeRange(worktimeRangeVo);
		return null;
	}

}
