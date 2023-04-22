/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.notify.handler;

import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.core.INotifyPolicyHandlerGroup;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.notify.core.NotifyPolicyHandlerBase;
import neatlogic.framework.notify.dto.NotifyTriggerTemplateVo;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.module.process.notify.constvalue.TimedTaskTriggerType;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务通知策略处理器
 * @author: linbq
 * @since: 2021/4/8 18:15
 **/
@Service
public class TimedTaskNotifyPolicyHandler extends NotifyPolicyHandlerBase {
    @Override
    public String getName() {
        return "handler.notify.timedtask";
    }

//    @Override
//    public List<NotifyTriggerVo> getNotifyTriggerListForNotifyTree() {
//        List<NotifyTriggerVo> returnList = new ArrayList<>();
//        for (TimedTaskTriggerType type : TimedTaskTriggerType.values()) {
//            returnList.add(new NotifyTriggerVo(type.getTrigger(), type.getText(), type.getDescription()));
//        }
//        return returnList;
//    }

    @Override
    protected List<NotifyTriggerVo> myNotifyTriggerList() {
        List<NotifyTriggerVo> returnList = new ArrayList<>();
        for (TimedTaskTriggerType type : TimedTaskTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(type.getTrigger(), type.getText(), type.getDescription()));
        }
        return returnList;
    }

    @Override
    protected List<NotifyTriggerTemplateVo> myNotifyTriggerTemplateList(NotifyHandlerType type) {
        return null;
    }

    @Override
    protected List<ConditionParamVo> mySystemParamList() {
        return null;
    }

    @Override
    protected List<ConditionParamVo> mySystemConditionOptionList() {
        return null;
    }

    @Override
    protected void myAuthorityConfig(JSONObject config) {

    }

    /**
     * 绑定权限，每种handler对应不同的权限
     */
    @Override
    public String getAuthName() {
        return null;
    }

    @Override
    public INotifyPolicyHandlerGroup getGroup() {
        return null;
    }

    @Override
    public boolean isPublic(){
        return false;
    }
}
