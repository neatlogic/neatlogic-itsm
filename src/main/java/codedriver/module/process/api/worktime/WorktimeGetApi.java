package codedriver.module.process.api.worktime;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeVo;
import codedriver.framework.process.exception.worktime.WorktimeNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorktimeGetApi extends PrivateApiComponentBase {
	
	@Autowired
	private WorktimeMapper worktimeMapper;

	@Override
	public String getToken() {
		return "worktime/get";
	}

	@Override
	public String getName() {
		return "工作时间窗口信息查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid")
	})
	@Output({
		@Param(explode = WorktimeVo.class)
	})
	@Description(desc = "工作时间窗口信息查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		WorktimeVo worktime = worktimeMapper.getWorktimeByUuid(uuid);
		if(worktime == null) {
			throw new WorktimeNotFoundException(uuid);
		}
		return worktime;
	}

}
