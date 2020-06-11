package codedriver.module.process.counter.handler;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.GlobalReminderHandlerFactory;
import codedriver.framework.reminder.core.IGlobalReminderHandler;
import codedriver.framework.reminder.dto.ReminderMessageVo;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.reminder.handler.ProcessTaskRemindHandler;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-13 18:24
 **/
@Service
public class RemindItsmTestApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "remind/itsm/test";
    }

    @Override
    public String getName() {
        return "itsm实时动态插件测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({ @Param( name = "title", type = ApiParamType.STRING, desc = "标题", isRequired = true),
             @Param( name = "content", type = ApiParamType.STRING, desc = "邮件内容", isRequired = true),
             @Param( name = "receiverIdStr", type = ApiParamType.STRING, desc = "收件人ID,使用“,”隔开", isRequired = true),
             @Param( name = "fromUser" , type = ApiParamType.STRING, desc = "发件人名称", isRequired = true),
             @Param( name = "paramObj", type = ApiParamType.JSONOBJECT, desc = "自定义数据对象")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReminderMessageVo message = new ReminderMessageVo();
        message.setTitle(jsonObj.getString("title"));
        message.setContent(jsonObj.getString("title"));
        message.setFromUser(jsonObj.getString("fromUser"));
        String userUuidStr = jsonObj.getString("receiverIdStr");
        List<String> userUuidList = new ArrayList<>();
        for (String userUuid : userUuidStr.split(",")){
        	userUuidList.add(userUuid);
        }
        if (jsonObj.containsKey("paramObj")){
            message.setParamObj(jsonObj.getJSONObject("paramObj"));
        }
        message.setReceiverList(userUuidList);
        IGlobalReminderHandler reminder = GlobalReminderHandlerFactory.getReminder(ProcessTaskRemindHandler.class.getName());
        reminder.send(message);
        return new JSONObject();
    }
}
