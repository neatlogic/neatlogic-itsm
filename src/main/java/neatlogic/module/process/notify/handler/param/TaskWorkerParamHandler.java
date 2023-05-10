/*
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
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepTaskUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class TaskWorkerParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private UserMapper userMapper;
    @Override
    public String getValue() {
        return ProcessTaskStepTaskNotifyParam.TASKWORKER.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType instanceof ProcessTaskStepTaskNotifyTriggerType)) {
            return null;
        }
        ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
        if(stepTaskVo != null ){
            List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = stepTaskVo.getStepTaskUserVoList();
            if(CollectionUtils.isNotEmpty(processTaskStepTaskUserList)){
                Set<String> userUuidSet = processTaskStepTaskUserList.stream().map(ProcessTaskStepTaskUserVo::getUserUuid).collect(Collectors.toSet());
                if(CollectionUtils.isNotEmpty(userUuidSet)){
                    List<UserVo> userList = userMapper.getUserByUserUuidList(new ArrayList<>(userUuidSet));
                    List<String> users = userList.stream().map(u->u.getName()+"("+u.getUserId()+")").collect(Collectors.toList());
                    return String.join(",",users);
                }
            }
        }
        return null;
    }
}
