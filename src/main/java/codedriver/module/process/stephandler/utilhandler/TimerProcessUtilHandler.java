/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.stephandler.utilhandler;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author linbq
 * @since 2021/12/27 16:04
 **/
@Component
public class TimerProcessUtilHandler extends ProcessStepInternalHandlerBase {
    @Override
    public String getHandler() {
        return ProcessStepHandlerType.TIMER.getHandler();
    }

    @Override
    public Object getHandlerStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public Object getHandlerStepInitInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return null;
    }

    @Override
    public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {

    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {

    }

    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        return null;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();
        String type = configObj.getString("type");
        if (StringUtils.isBlank(type)) {
            type = "custom";
        }
        resultObj.put("type", type);
        String value = configObj.getString("value");
        if (value == null) {
            value = "";
        }
        resultObj.put("value", value);
        String attributeUuid = configObj.getString("attributeUuid");
        if (attributeUuid == null) {
            attributeUuid = "";
        }
        resultObj.put("attributeUuid", attributeUuid);
        return resultObj;
    }
}
