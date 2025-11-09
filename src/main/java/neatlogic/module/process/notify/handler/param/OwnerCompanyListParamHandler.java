/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class OwnerCompanyListParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.OWNERCOMPANYLIST.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTaskVo.getOwner());
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                Set<Long> idSet = new HashSet<>();
                List<TeamVo> ownerDepartmentList = new ArrayList<>();
                List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
                for (TeamVo teamVo : teamList) {
                    List<TeamVo> departmentList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(), TeamLevel.COMPANY.getValue());
                    if (CollectionUtils.isNotEmpty(departmentList)) {
                        for (TeamVo team : departmentList) {
                            if (!idSet.contains(team.getId())) {
                                idSet.add(team.getId());
                                ownerDepartmentList.add(team);
                            }
                        }
                    }
                }
                return ownerDepartmentList.stream().map(TeamVo::getName).collect(Collectors.toList());
            }
        }
        return null;
    }
}
