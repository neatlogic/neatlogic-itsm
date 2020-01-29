package codedriver.module.process.counter.handler;

import codedriver.framework.counter.core.GlobalCounterBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-12 11:01
 **/
@Service
public class ProcessTaskCounterHandler extends GlobalCounterBase {
    @Override
    public String getName() {
       return  "工作流引擎";
    }

    @Override
    public String getPluginId() {
        return ClassUtils.getUserClass(this.getClass()).getName();
    }

    @Override
    public String getPreviewPath() {
        return "/resources/images/notify-statistics/ITSM.png";
    }

    @Override
    public String getDescription() {
        return "流程消息统计插件";
    }

    @Override
    public String getShowTemplate() {
        return "balantflow.systemremind.flowtask.show";
    }

    @Override
    public Object getMyShowData() {
        JSONObject dataObj = new JSONObject();
        dataObj.put("name", "itsmtest");
        return dataObj;
    }
}
