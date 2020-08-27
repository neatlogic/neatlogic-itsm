package codedriver.module.process.api.workcenter.test;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPool;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.elasticsearch.core.ElasticSearchFactory;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.elasticsearch.core.ProcessTaskEsHandlerBase;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterUpdateEsApi extends PrivateApiComponentBase {

	@Autowired
	ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "workcenter/update/es";
	}

	@Override
	public String getName() {
		return "修改工单数据到es";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="fromDate", type = ApiParamType.STRING, desc="开始时间"),
		@Param(name="toDate", type = ApiParamType.STRING, desc="开始时间"),
		@Param(name="processTaskIds", type = ApiParamType.JSONARRAY, desc="工单数组")
	})
	@Output({
		
	})
	@Description(desc = "修改工单数据到es")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<Object> taskIds = jsonObj.getJSONArray("processTaskIds");
		List<Long> taskIdList = null;
		if(CollectionUtils.isNotEmpty(taskIds)) {
			taskIdList = taskIds.stream().map(object -> Long.parseLong(object.toString())).collect(Collectors.toList());
		}
		String fromDate = jsonObj.getString("fromDate");
		String toDate = jsonObj.getString("toDate");
		List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskListByKeywordAndIdList(null,taskIdList,fromDate,toDate);
		
		MultiAttrsObjectPool  poll = ElasticSearchPoolManager.getObjectPool(ProcessTaskEsHandlerBase.POOL_NAME);
		poll.checkout(TenantContext.get().getTenantUuid());
		for(ProcessTaskVo processTaskVo :processTaskVoList) {
			JSONObject paramObj = new JSONObject();
			paramObj.put("taskId", processTaskVo.getId());
			paramObj.put("tenantUuid", TenantContext.get().getTenantUuid());
			try {
				ElasticSearchFactory.getHandler("processtask-update").doService(paramObj);
			}catch(Exception e) {
				poll.delete(processTaskVo.getId().toString());
			}
		}
		
		return null;
	}
}
