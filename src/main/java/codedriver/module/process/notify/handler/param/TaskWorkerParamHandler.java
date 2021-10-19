/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskUserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class TaskWorkerParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
        if(stepTaskVo != null ){
            if(CollectionUtils.isNotEmpty(stepTaskVo.getStepTaskUserVoList())){
                List<UserVo> userVoList = stepTaskVo.getStepTaskUserVoList().stream().map(ProcessTaskStepTaskUserVo::getUserVo).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(userVoList)){
                    List<String> users = userVoList.stream().map(u->u.getName()+"("+u.getUserId()+")").collect(Collectors.toList());
                    return String.join(",",users);
                }
            }
        }
        return null;
    }
}
