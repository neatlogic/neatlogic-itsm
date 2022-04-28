/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskUserVo;
import codedriver.framework.process.dto.ProcessTaskStepTaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
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
