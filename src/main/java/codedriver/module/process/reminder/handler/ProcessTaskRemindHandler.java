package codedriver.module.process.reminder.handler;

import codedriver.framework.reminder.core.GlobalReminderBase;
import codedriver.framework.reminder.dto.param.GlobalReminderParamVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-12 10:55
 **/
@Service
public class ProcessTaskRemindHandler extends GlobalReminderBase {

    @Override
    public String getName() {
        return "工作流引擎";
    }

    @Override
    public String getPluginId(){
        return ClassUtils.getUserClass(this.getClass()).getName();
    }

    @Override
    public String getDescription() {
        return "获取流程实时动态消息";
    }

    @Override
    public String getShowTemplate() {
        return "balantflow.instantmessage.message.flow";
    }

    @Override
    public String getPopUpTemplate() {
        return "balantflow.instantmessage.message.modelshow";
    }

    @Override
    public void packMyData(JSONObject resultObj, JSONObject controlData) {

    }

    @Override
    public void myConfig(List<GlobalReminderParamVo> paramVoList) {

    }
}
