package codedriver.module.process.notify.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.notify.core.INotifyPolicyHandlerGroup;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.core.NotifyHandlerType;
import codedriver.framework.notify.dto.NotifyTriggerTemplateVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.framework.process.notify.core.NotifyDefaultTemplateFactory;
import codedriver.framework.process.notify.core.IDefaultTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.condition.core.IConditionHandler;
import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.dto.ExpressionVo;
import codedriver.framework.notify.core.NotifyPolicyHandlerBase;
import codedriver.framework.process.constvalue.ConditionProcessTaskOptions;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskParams;
import codedriver.framework.process.notify.constvalue.TaskNotifyTriggerType;
@Component
public class TaskNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return "工单";
	}
	
	@Override
	public List<NotifyTriggerVo> myNotifyTriggerList() {
		List<NotifyTriggerVo> returnList = new ArrayList<>();
		for (TaskNotifyTriggerType notifyTriggerType : TaskNotifyTriggerType.values()) {
			returnList.add(new NotifyTriggerVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText(),notifyTriggerType.getDescription()));
		}
		return returnList;
	}

    @Override
    protected List<NotifyTriggerTemplateVo> myNotifyTriggerTemplateList(NotifyHandlerType type) {
        List<NotifyTriggerTemplateVo> list = new ArrayList<>();
        /** 根据type获取通知handler全类名 */
        String handler = null;
        List<ValueTextVo> notifyHandlerTypeList = NotifyHandlerFactory.getNotifyHandlerNameList();
        Optional<ValueTextVo> first = notifyHandlerTypeList.stream().filter(o -> o.getText().equals(type.getValue())).findFirst();
        if(first != null){
            ValueTextVo notifyHandlerClass = first.get();
            handler = notifyHandlerClass.getValue().toString();
        }
        List<IDefaultTemplate> templateList = NotifyDefaultTemplateFactory.getTemplate(type.getValue());
        if(CollectionUtils.isNotEmpty(templateList)){
            Map<String, List<IDefaultTemplate>> map = templateList.stream().collect(Collectors.groupingBy(IDefaultTemplate::getTrigger));
            for (TaskNotifyTriggerType notifyTriggerType : TaskNotifyTriggerType.values()) {
                List<IDefaultTemplate> templates = map.get(notifyTriggerType.getTrigger().toLowerCase());
                for(IDefaultTemplate vo : templates){
                    list.add(new NotifyTriggerTemplateVo(notifyTriggerType.getText(),notifyTriggerType.getDescription(),vo.getTitle(),vo.getContent(),handler));
                }
            }
        }
        return list;
    }

    @Override
	protected List<ConditionParamVo> mySystemParamList() {
		List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
		for(ProcessTaskParams processTaskParams : ProcessTaskParams.values()) {
		    ConditionParamVo param = new ConditionParamVo();
		    param.setName(processTaskParams.getValue());
            param.setLabel(processTaskParams.getText());
            param.setParamType(processTaskParams.getParamType().getName());
            param.setParamTypeName(processTaskParams.getParamType().getText());
            param.setFreemarkerTemplate(processTaskParams.getFreemarkerTemplate());
            param.setIsEditable(0);
            notifyPolicyParamList.add(param);
		}
		return notifyPolicyParamList;
	}

    @Override
    protected List<ConditionParamVo> mySystemConditionOptionList() {
        List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
        for(ConditionProcessTaskOptions option : ConditionProcessTaskOptions.values()) {
            IConditionHandler condition = ConditionHandlerFactory.getHandler(option.getValue());
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
	}

    @Override
    public String getAuthName() {
        return AuthFactory.getAuthInstance("PROCESS_MODIFY").getAuthName();
    }

    @Override
    public INotifyPolicyHandlerGroup getGroup() {
        return null;
    }
}
