package codedriver.module.process.api.worktime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeDefineIsEmptyException;
import codedriver.framework.process.exception.WorktimeNameRepeatException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeDefineVo;
import codedriver.module.process.dto.WorktimeVo;

@Service
@Transactional
public class WorktimeSaveApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "process/worktime/save";
	}

	@Override
	public String getName() {
		return "工作时间窗口信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "工作时间窗口uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
		@Param(name = "worktimeDefineList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工作时段列表"),
		@Param(name = "worktimeDefineList[0].weekday", type = ApiParamType.INTEGER, desc = "星期几"),
		@Param(name = "worktimeDefineList[0].startTime", type = ApiParamType.STRING, desc = "开始时间"),
		@Param(name = "worktimeDefineList[0].endTime", type = ApiParamType.STRING, desc = "结束时间"),
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.STRING, desc = "工作时间窗口uuid")
	})
	@Description(desc = "工作时间窗口信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		WorktimeVo worktimeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeVo>() {});
		if(worktimeMapper.checkWorktimeNameIsRepeat(worktimeVo) > 0) {
			throw new WorktimeNameRepeatException(worktimeVo.getName());
		}
		worktimeVo.setLcu(UserContext.get().getUserId());
		String uuid = worktimeVo.getUuid();
		if(worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
			worktimeVo.setUuid(null);
			worktimeMapper.insertWorktime(worktimeVo);
			uuid = worktimeVo.getUuid();
		}else {
			worktimeMapper.updateWorktime(worktimeVo);
			worktimeMapper.deleteWorktimeDefineByWorktimeUuid(worktimeVo.getUuid());
		}
		List<WorktimeDefineVo> worktimeDefineList = worktimeVo.getWorktimeDefineList();
		if(worktimeDefineList.isEmpty()) {
			throw new WorktimeDefineIsEmptyException(worktimeVo.getName());
		}
		List<WorktimeDefineVo> batchWorktimeDefine = new ArrayList<>();
		for(WorktimeDefineVo worktimeDefineVo : worktimeDefineList) {
			worktimeDefineVo.setWorktimeUuid(uuid);
			batchWorktimeDefine.add(worktimeDefineVo);
			if(batchWorktimeDefine.size() > 1000) {
				worktimeMapper.insertBatchWorktimeDefine(batchWorktimeDefine);
				batchWorktimeDefine.clear();
			}
		}
		worktimeMapper.insertBatchWorktimeDefine(batchWorktimeDefine);
		return uuid;
	}

}
