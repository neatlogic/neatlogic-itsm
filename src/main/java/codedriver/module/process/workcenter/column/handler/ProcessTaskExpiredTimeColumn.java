package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.WorktimeMapper;

@Component
public class ProcessTaskExpiredTimeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
	
	@Autowired
	WorktimeMapper worktimeMapper;

	@Override
	public String getName() {
		return "expiretime";
	}

	@Override
	public String getDisplayName() {
		return "剩余时间";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray resultArray = new JSONArray();
		String worktimeUuid = json.getString("worktime");
		JSONArray processTaskSlaArray = json.getJSONArray(this.getName());
		if(json.getString(ProcessWorkcenterField.STATUS.getValue()).equals(ProcessTaskStatus.RUNNING.getValue())&&CollectionUtils.isNotEmpty(processTaskSlaArray)) {
			for (int i = 0; i < processTaskSlaArray.size(); i++) {
				JSONObject tmpJson = new JSONObject();
				JSONObject processTaskSla = processTaskSlaArray.getJSONObject(i);
				JSONObject slaTimeJson = processTaskSla.getJSONObject("slaTimeVo");
				Long expireTime = slaTimeJson.getLong("expireTime");
				Long realExpireTime = slaTimeJson.getLong("realExpireTime");
                Long expireTimeLong = slaTimeJson.getLong("expireTimeLong");
                Long realExpireTimeLong = slaTimeJson.getLong("realExpireTimeLong");
                expireTime = expireTime == null ? expireTimeLong : expireTime;
                realExpireTime = realExpireTime == null ? realExpireTimeLong : realExpireTime;
				if(expireTime != null) {
					long timeLeft = worktimeMapper.calculateCostTime(worktimeUuid, System.currentTimeMillis(),expireTime);
					tmpJson.put("timeLeft", timeLeft);
					tmpJson.put("expireTime", expireTime);
				}
				if(realExpireTime != null) {
					long realTimeLeft = realExpireTime - System.currentTimeMillis();
					tmpJson.put("realTimeLeft", realTimeLeft);
					tmpJson.put("realExpireTime", realExpireTime);
				}
				tmpJson.put("slaName", processTaskSla.getString("name"));
				//获取即将超时规则，默认分钟（从超时通知策略获取）
				JSONObject configObj = processTaskSla.getJSONObject("configObj");
				if(configObj != null && configObj.containsKey("willOverTimeRule")) {
					Integer willOverTimeRule =  configObj.getInteger("willOverTimeRule");
					tmpJson.put("willOverTimeRule",willOverTimeRule);
					if(willOverTimeRule != null && expireTime != null) {
						tmpJson.put("willOverTime", expireTime - willOverTimeRule*60*100);
					}
				}
				resultArray.add(tmpJson);
				
			}
		}
		return resultArray;
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
		return 14;
	}

}
