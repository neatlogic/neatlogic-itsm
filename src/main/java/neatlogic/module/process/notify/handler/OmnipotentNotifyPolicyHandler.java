package neatlogic.module.process.notify.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.condition.core.ConditionHandlerFactory;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.dto.ExpressionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.notify.core.INotifyPolicyHandlerGroup;
import neatlogic.framework.notify.core.NotifyHandlerFactory;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.notify.core.NotifyPolicyHandlerBase;
import neatlogic.framework.notify.dto.NotifyTriggerTemplateVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.constvalue.ConditionProcessTaskOptions;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.notify.constvalue.*;
import neatlogic.framework.process.notify.core.IDefaultTemplate;
import neatlogic.framework.process.notify.core.NotifyDefaultTemplateFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OmnipotentNotifyPolicyHandler extends NotifyPolicyHandlerBase {

	@Override
	public String getName() {
		return ProcessStepHandlerType.OMNIPOTENT.getName();
	}
	
	@Override
	public List<NotifyTriggerVo> myNotifyTriggerList() {
		List<NotifyTriggerVo> returnList = new ArrayList<>();
		for (ProcessTaskStepNotifyTriggerType notifyTriggerType : ProcessTaskStepNotifyTriggerType.values()) {
			returnList.add(new NotifyTriggerVo(notifyTriggerType.getTrigger(), notifyTriggerType.getText(),notifyTriggerType.getDescription()));
		}
		//任务
        for (ProcessTaskStepTaskNotifyTriggerType notifyTriggerType : ProcessTaskStepTaskNotifyTriggerType.values()) {
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
            for (ProcessTaskStepNotifyTriggerType notifyTriggerType : ProcessTaskStepNotifyTriggerType.values()) {
                List<IDefaultTemplate> templates = map.get(notifyTriggerType.getTrigger().toLowerCase());
                if (CollectionUtils.isEmpty(templates)) {
                    continue;
                }
                for(IDefaultTemplate vo : templates){
                    list.add(new NotifyTriggerTemplateVo(notifyTriggerType.getText(),notifyTriggerType.getDescription(),vo.getTitle(),vo.getContent(),handler));
                }
            }
            //任务
            for (ProcessTaskStepTaskNotifyTriggerType notifyTriggerType : ProcessTaskStepTaskNotifyTriggerType.values()) {
                List<IDefaultTemplate> templates = map.get(notifyTriggerType.getTrigger().toLowerCase());
                if (CollectionUtils.isEmpty(templates)) {
                    continue;
                }
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
//		for(ProcessTaskParams processTaskParams : ProcessTaskParams.values()) {
//		    ConditionParamVo param = new ConditionParamVo();
//		    param.setName(processTaskParams.getValue());
//            param.setLabel(processTaskParams.getText());
//            param.setParamType(processTaskParams.getParamType().getName());
//            param.setParamTypeName(processTaskParams.getParamType().getText());
//            param.setFreemarkerTemplate(processTaskParams.getFreemarkerTemplate());
//            param.setIsEditable(0);
//            notifyPolicyParamList.add(param);
//		}
		for(ProcessTaskNotifyParam param : ProcessTaskNotifyParam.values()) {
		    ConditionParamVo paramVo = new ConditionParamVo();
            paramVo.setName(param.getValue());
            paramVo.setLabel(param.getText());
            paramVo.setParamType(param.getParamType().getName());
            paramVo.setParamTypeName(param.getParamType().getText());
            paramVo.setFreemarkerTemplate(param.getFreemarkerTemplate());
            paramVo.setIsEditable(0);
            notifyPolicyParamList.add(paramVo);
		}
		for(ProcessTaskStepNotifyParam param : ProcessTaskStepNotifyParam.values()) {
            ConditionParamVo paramVo = new ConditionParamVo();
            paramVo.setName(param.getValue());
            paramVo.setLabel(param.getText());
            paramVo.setParamType(param.getParamType().getName());
            paramVo.setParamTypeName(param.getParamType().getText());
            paramVo.setFreemarkerTemplate(param.getFreemarkerTemplate());
            paramVo.setIsEditable(0);
            notifyPolicyParamList.add(paramVo);
		}
		for(ProcessTaskStepTaskNotifyParam param : ProcessTaskStepTaskNotifyParam.values()) {
            ConditionParamVo paramVo = new ConditionParamVo();
            paramVo.setName(param.getValue());
            paramVo.setLabel(param.getText());
            paramVo.setParamType(param.getParamType().getName());
            paramVo.setParamTypeName(param.getParamType().getText());
            paramVo.setFreemarkerTemplate(param.getFreemarkerTemplate());
            paramVo.setIsEditable(0);
            notifyPolicyParamList.add(paramVo);
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
        List<String> includeList = JSON.parseArray(config.getJSONArray("includeList").toJSONString(), String.class);
        includeList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue() + "#" + ProcessUserType.DEFAULT_WORKER.getValue());
        config.put("includeList", includeList);
	}

    @Override
    public String getAuthName() {
        return PROCESS_MODIFY.class.getSimpleName();
    }

    @Override
    public INotifyPolicyHandlerGroup getGroup() {
        return ProcessNotifyPolicyHandlerGroup.TASKSTEP;
    }
}
