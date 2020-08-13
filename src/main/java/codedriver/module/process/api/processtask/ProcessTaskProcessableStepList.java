package codedriver.module.process.api.processtask;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskProcessableStepList extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

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
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "当前用户可处理的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		List<ProcessTaskStepVo> processableStepList = ProcessStepUtilHandlerFactory.getHandler().getProcessableStepList(processTaskId);
		String action = jsonObj.getString("action");
		if(StringUtils.isNotBlank(action)) {
			Iterator<ProcessTaskStepVo> iterator = processableStepList.iterator();
			while(iterator.hasNext()) {
				ProcessTaskStepVo processTaskStepVo = iterator.next();
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
				if(ProcessTaskStepAction.ACCEPT.getValue().equals(action)) {
					if(CollectionUtils.isNotEmpty(majorUserList)) {
						iterator.remove();
					}
				}else if(ProcessTaskStepAction.START.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}else if(ProcessTaskStepAction.COMPLETE.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}
			}
		}
		return processableStepList;
	}

}
