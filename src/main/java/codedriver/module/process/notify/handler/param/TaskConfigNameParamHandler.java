/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.springframework.stereotype.Component;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class TaskConfigNameParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Override
    public String getValue() {
        return ProcessTaskStepTaskNotifyParam.TASKCONFIGNAME.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
        if(stepTaskVo != null ){
            return stepTaskVo.getTaskConfigName();
        }
        return null;
    }
}
