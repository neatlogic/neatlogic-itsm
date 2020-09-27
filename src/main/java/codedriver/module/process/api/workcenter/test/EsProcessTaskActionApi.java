package codedriver.module.process.api.workcenter.test;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.elasticsearch.core.ElasticSearchFactory;
import codedriver.framework.elasticsearch.core.IElasticSearchHandler;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.elasticsearch.constvalue.ESHandler;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EsProcessTaskActionApi extends PrivateApiComponentBase {

	@Autowired
	ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/es/action";
	}

	@Override
	public String getName() {
		return "更新es工单数据";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="fromDate", type = ApiParamType.STRING, desc="开始时间"),
		@Param(name="toDate", type = ApiParamType.STRING, desc="开始时间"),
		@Param(name="processTaskIds", type = ApiParamType.JSONARRAY, desc="工单数组"),
		@Param(name="action", type = ApiParamType.STRING, desc="delete,update")
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
		String action = jsonObj.getString("action");
		if(action == null) {
		    action = "update"; 
		}
		List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskListByKeywordAndIdList(null,taskIdList,fromDate,toDate);
		IElasticSearchHandler  handler = ElasticSearchFactory.getHandler(ESHandler.PROCESSTASK.getValue());
		for(ProcessTaskVo processTaskVo :processTaskVoList) {
			JSONObject paramObj = new JSONObject();
			paramObj.put("taskId", processTaskVo.getId());
			paramObj.put("tenantUuid", TenantContext.get().getTenantUuid());
			if(action.equals("update")) {
    			try {
    			    handler.save(paramObj);
    			}catch(Exception e) {
    			    handler.delete(processTaskVo.getId().toString());
    			}
			}else {
			    handler.delete(processTaskVo.getId().toString());
			}
		}
		
		return null;
	}
}
