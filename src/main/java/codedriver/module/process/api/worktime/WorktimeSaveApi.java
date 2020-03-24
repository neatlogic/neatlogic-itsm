package codedriver.module.process.api.worktime;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeVo;
import codedriver.framework.process.exception.worktime.WorktimeConfigIllegalException;
import codedriver.framework.process.exception.worktime.WorktimeNameRepeatException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class WorktimeSaveApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "worktime/save";
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
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, length = 50, desc = "工作时间窗口名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
		@Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "每周工作时段的定义")
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
		//验证config
		List<String> list = new ArrayList<>();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
		JSONObject configJson = JSON.parseObject(worktimeVo.getConfig());		
		for(Entry<String, Object> entry : configJson.entrySet()) {
			try {
				DayOfWeek.valueOf(entry.getKey().toUpperCase());
			}catch(IllegalArgumentException e) {
				throw new WorktimeConfigIllegalException(entry.getKey());
			}
			Object value = entry.getValue();
			if(value instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) value;
				for(int i = 0; i < jsonArray.size(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);			
					list.add("startTime");
					list.add("endTime");
					for(Entry<String, Object> entry2 : obj.entrySet()) {
						if(!list.remove(entry2.getKey())){
							throw new WorktimeConfigIllegalException(entry2.getKey());
						}
						String value2 = entry2.getValue().toString();
						try {
							timeFormatter.parse(value2);
						}catch(DateTimeException e) {
							throw new WorktimeConfigIllegalException(value2);
						}
					}
					if(list.size() > 0) {
						throw new WorktimeConfigIllegalException(obj.toString());
					}
				}
			}else {			
				throw new WorktimeConfigIllegalException(value.toString());
			}
		}
		
		worktimeVo.setLcu(UserContext.get().getUserId());
		String uuid = worktimeVo.getUuid();
		if(worktimeMapper.checkWorktimeIsExists(uuid) == 0) {
			worktimeVo.setUuid(null);
			worktimeMapper.insertWorktime(worktimeVo);
			uuid = worktimeVo.getUuid();
		}else {
			worktimeMapper.updateWorktime(worktimeVo);
		}

		return uuid;
	}

}
