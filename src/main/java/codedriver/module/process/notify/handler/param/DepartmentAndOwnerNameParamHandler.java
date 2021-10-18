/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
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
