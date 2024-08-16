package neatlogic.module.process.notify.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepAutomaticNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyHandlerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: AutomaticNotifyPolicyHandler
 * @Package neatlogic.module.process.notify.handler
 * @Description: 自动处理节点通知策略处理器
 * @Author: linbq
 * @Date: 2021/3/8 11:01

 **/
@Component
public class AutomaticNotifyPolicyHandler extends ProcessTaskNotifyHandlerBase {
    @Override
    public String getName() {
        return ProcessStepHandlerType.AUTOMATIC.getName();
    }

    /**
     * 绑定权限，每种handler对应不同的权限
     */
    @Override
    public String getAuthName() {
        return PROCESS_MODIFY.class.getSimpleName();
    }

    @Override
    protected List<NotifyTriggerVo> myCustomNotifyTriggerList() {
        List<NotifyTriggerVo> returnList = new ArrayList<>();
        for (ProcessTaskStepAutomaticNotifyTriggerType notifyTriggerType : ProcessTaskStepAutomaticNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
        }
        return returnList;
    }

    @Override
    protected List<ConditionParamVo> myCustomSystemParamList() {
        List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
        for (ProcessTaskStepAutomaticNotifyParam param : ProcessTaskStepAutomaticNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
        }
        return notifyPolicyParamList;
    }

    @Override
    protected void myCustomAuthorityConfig(JSONObject config) {
        List<String> excludeList = config.getJSONArray("excludeList").toJavaList(String.class);
        excludeList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue() + "#" + ProcessUserType.MINOR.getValue());
        config.put("excludeList", excludeList);
    }
}
