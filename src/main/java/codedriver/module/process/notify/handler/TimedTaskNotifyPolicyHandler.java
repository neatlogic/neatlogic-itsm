/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler;

import codedriver.framework.dto.ConditionParamVo;
import codedriver.framework.notify.core.INotifyPolicyHandlerGroup;
import codedriver.framework.notify.core.NotifyHandlerType;
import codedriver.framework.notify.core.NotifyPolicyHandlerBase;
import codedriver.framework.notify.dto.NotifyTriggerTemplateVo;
import codedriver.framework.notify.dto.NotifyTriggerVo;
import codedriver.module.process.notify.constvalue.TimedTaskTriggerType;
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
        return "定时任务";
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
