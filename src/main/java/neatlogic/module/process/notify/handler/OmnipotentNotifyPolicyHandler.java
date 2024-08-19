package neatlogic.module.process.notify.handler;

import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyHandlerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OmnipotentNotifyPolicyHandler extends ProcessTaskNotifyHandlerBase {

	@Override
	public String getName() {
		return ProcessStepHandlerType.OMNIPOTENT.getName();
	}
	
	@Override
	public List<NotifyTriggerVo> myCustomNotifyTriggerList() {
		List<NotifyTriggerVo> returnList = new ArrayList<>();
		//任务
        for (ProcessTaskStepTaskNotifyTriggerType notifyTriggerType : ProcessTaskStepTaskNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
        }
		return returnList;
	}

    @Override
	protected List<ConditionParamVo> myCustomSystemParamList() {
		List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
		for(ProcessTaskStepTaskNotifyParam param : ProcessTaskStepTaskNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
		}
		return notifyPolicyParamList;
	}

    @Override
    public String getAuthName() {
        return PROCESS_MODIFY.class.getSimpleName();
    }

//    @Override
//    public INotifyPolicyHandlerGroup getGroup() {
//        return ProcessNotifyPolicyHandlerGroup.TASKSTEP;
//    }
}
