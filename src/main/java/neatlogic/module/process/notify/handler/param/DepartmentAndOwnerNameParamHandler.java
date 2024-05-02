/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class DepartmentAndOwnerNameParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.DEPARTMENTANDOWNERNAME.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            String owner = processTaskVo.getOwner();
            if (StringUtils.isNotBlank(owner)) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(owner);
                if (userVo != null) {
                    StringBuilder result = new StringBuilder();
                    List<String> teamUuidList =  teamMapper.getTeamUuidListByUserUuid(owner);
                    for (String teamUuid : teamUuidList) {
                        TeamVo teamVo = teamMapper.getTeamByUuid(teamUuid);
                        if (teamVo != null) {
                            if (TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                result.append(teamVo.getName());
                                result.append("/");
                                break;
                            }
                        }
                    }
                    result.append(userVo.getUserName());
                    return result.toString();
                }
            }
        }
        return null;
    }
}
