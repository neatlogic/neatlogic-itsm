package codedriver.module.process.api.processtask;

import java.util.*;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dto.ProcessTaskStepInOperationVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskProcessableStepList extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskMapper processTaskMapper;
    
    @Resource
    private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/processablestep/list";
	}

	@Override
	public String getName() {
		return "当前用户可处理的步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start,complete", desc = "操作类型")
	})
	@Output({
		@Param(name = "tbodyList", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表"),
		@Param(name = "status", type = ApiParamType.ENUM, rule = "ok,running", desc = "步骤信息列表")
	})
	@Description(desc = "当前用户可处理的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		JSONObject resultObj = new JSONObject();
		resultObj.put("status", "ok");
		List<ProcessTaskStepInOperationVo> processTaskStepInOperationList = processTaskMapper.getProcessTaskStepInOperationListByProcessTaskId(processTaskId);
		if (CollectionUtils.isNotEmpty(processTaskStepInOperationList)) {
			// 如果后台有正在异步处理中的步骤，则返回status=running，前端等待一定时间后再次请求
			for (ProcessTaskStepInOperationVo processTaskStepInOperationVo : processTaskStepInOperationList) {
				Date expireTime = processTaskStepInOperationVo.getExpireTime();
				if (expireTime == null) {
					resultObj.put("status", "running");
					return resultObj;
				} else {
					long after = System.currentTimeMillis() - expireTime.getTime();
					if (after > 0) {
						resultObj.put("status", "running");
						return resultObj;
					} else {
						processTaskMapper.deleteProcessTaskStepInOperationById(processTaskStepInOperationVo.getId());
					}
				}
			}
		}

		List<ProcessTaskStepVo> processableStepList = processTaskService.getProcessableStepList(processTaskVo, jsonObj.getString("action"));
		resultObj.put("tbodyList", processableStepList);
		return resultObj;
	}

}
