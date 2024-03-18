/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
