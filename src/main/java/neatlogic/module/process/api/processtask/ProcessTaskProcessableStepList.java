package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
		return "nmpap.processtaskprocessablesteplist.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start,complete,recover", desc = "common.actiontype")
	})
	@Output({
		@Param(name = "tbodyList", explode = ProcessTaskStepVo[].class, desc = "common.tbodylist"),
		@Param(name = "status", type = ApiParamType.ENUM, rule = "ok,running", desc = "common.status")
	})
	@Description(desc = "nmpap.processtaskprocessablesteplist.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		if (processTaskVo == null) {
			throw new ProcessTaskNotFoundEditTargetException(processTaskId);
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("processTaskStatus", processTaskVo.getStatus());
		resultObj.put("status", "ok");
//		if (Objects.equals(processTaskVo.getStatus(), ProcessTaskStatus.DRAFT.getValue())) {
//			resultObj.put("status", "running");
//			return resultObj;
//		}
		int count = processTaskMapper.getProcessTaskStepInOperationCountByProcessTaskId(processTaskId);
		if (count > 0) {
			resultObj.put("status", "running");
			return resultObj;
		}
//		List<ProcessTaskStepInOperationVo> processTaskStepInOperationList = processTaskMapper.getProcessTaskStepInOperationListByProcessTaskId(processTaskId);
//		System.out.println("processTaskStepInOperationList = " + JSONObject.toJSONString(processTaskStepInOperationList));
//		if (CollectionUtils.isNotEmpty(processTaskStepInOperationList)) {
//			// 如果后台有正在异步处理中的步骤，则返回status=running，前端等待一定时间后再次请求
//			for (ProcessTaskStepInOperationVo processTaskStepInOperationVo : processTaskStepInOperationList) {
//				Date expireTime = processTaskStepInOperationVo.getExpireTime();
//				if (expireTime == null) {
//					resultObj.put("status", "running");
//					return resultObj;
//				} else {
//					long after = expireTime.getTime() - System.currentTimeMillis();
//					if (after > 0) {
//						resultObj.put("status", "running");
//						return resultObj;
//					} else {
//						processTaskMapper.deleteProcessTaskStepInOperationById(processTaskStepInOperationVo.getId());
//					}
//				}
//			}
//		}

		List<ProcessTaskStepVo> processableStepList = processTaskService.getProcessableStepList(processTaskVo, jsonObj.getString("action"));
		resultObj.put("tbodyList", processableStepList);
		return resultObj;
	}

}
