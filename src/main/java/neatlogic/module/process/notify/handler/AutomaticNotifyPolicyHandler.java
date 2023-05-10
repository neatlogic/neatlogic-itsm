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
import neatlogic.framework.util.I18nUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Title: AutomaticNotifyPolicyHandler
 * @Package neatlogic.module.process.notify.handler
 * @Description: 自动处理节点通知策略处理器
 * @Author: linbq
 * @Date: 2021/3/8 11:01
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **/
@Component
public class AutomaticNotifyPolicyHandler extends NotifyPolicyHandlerBase {
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
    public INotifyPolicyHandlerGroup getGroup() {
        return ProcessNotifyPolicyHandlerGroup.TASKSTEP;
    }

    @Override
    protected List<NotifyTriggerVo> myNotifyTriggerList() {
        List<NotifyTriggerVo> returnList = new ArrayList<>();
        for (ProcessTaskStepNotifyTriggerType notifyTriggerType : ProcessTaskStepNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
        }
        for (ProcessTaskStepAutomaticNotifyTriggerType notifyTriggerType : ProcessTaskStepAutomaticNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
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
            for (ProcessTaskStepAutomaticNotifyTriggerType notifyTriggerType : ProcessTaskStepAutomaticNotifyTriggerType.values()) {
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
        for(ProcessTaskNotifyParam param : ProcessTaskNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
        }
        for(ProcessTaskStepNotifyParam param : ProcessTaskStepNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
        }
        for (ProcessTaskStepAutomaticNotifyParam param : ProcessTaskStepAutomaticNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
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
        includeList.add(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue() + "#" + ProcessUserType.FOCUS_USER.getValue());
        config.put("includeList", includeList);
    }
}
