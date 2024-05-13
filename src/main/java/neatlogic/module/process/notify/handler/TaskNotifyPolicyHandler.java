package neatlogic.module.process.notify.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.notify.core.NotifyPolicyHandlerBase;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class TaskNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return "term.itsm.processtask";
	}
	
	@Override
	public List<NotifyTriggerVo> myNotifyTriggerList() {
		List<NotifyTriggerVo> returnList = new ArrayList<>();
		for (ProcessTaskNotifyTriggerType notifyTriggerType : ProcessTaskNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
		}
		return returnList;
	}

    @Override
	protected List<ConditionParamVo> mySystemParamList() {
		List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
        for(ProcessTaskNotifyParam param : ProcessTaskNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
        }
		return notifyPolicyParamList;
	}

    @Override
    protected List<ConditionParamVo> mySystemConditionOptionList() {
        List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
        for(ConditionProcessTaskOptions option : ConditionProcessTaskOptions.values()) {
            IConditionHandler condition = ProcessTaskConditionFactory.getHandler(option.getValue());
            if(condition != null) {
                ConditionParamVo param = new ConditionParamVo();
                param.setName(condition.getName());
                param.setLabel(condition.getDisplayName());
                param.setController(condition.getHandler(FormConditionModel.CUSTOM));
                if(condition.getConfig() != null) {
                    param.setConfig(condition.getConfig().toJSONString());
                }
                param.setType(condition.getType());
                ParamType paramType = condition.getParamType();
                if(paramType != null) {
                    param.setParamType(paramType.getName());
                    param.setParamTypeName(paramType.getText());
                    param.setDefaultExpression(paramType.getDefaultExpression().getExpression());
                    for(Expression expression : paramType.getExpressionList()) {
                        param.getExpressionList().add(new ExpressionVo(expression.getExpression(), expression.getExpressionName(),expression.getIsShowConditionValue()));
                    }
                }
                
                param.setIsEditable(0);
                notifyPolicyParamList.add(param);
            }
        }
        return notifyPolicyParamList;
    }

	@Override
	protected void myAuthorityConfig(JSONObject config) {
		List<String> groupList = JSON.parseArray(config.getJSONArray("groupList").toJSONString(), String.class);
		groupList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue());
		config.put("groupList", groupList);
        List<String> includeList = JSON.parseArray(config.getJSONArray("includeList").toJSONString(), String.class);
        includeList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue() + "#" + ProcessUserType.FOCUS_USER.getValue());
        config.put("includeList", includeList);
	}

    @Override
    public String getAuthName() {
        return PROCESS_MODIFY.class.getSimpleName();
    }

}
