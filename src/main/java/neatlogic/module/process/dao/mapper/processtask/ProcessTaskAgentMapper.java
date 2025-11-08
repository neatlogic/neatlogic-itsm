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

package neatlogic.module.process.dao.mapper.processtask;

import neatlogic.framework.process.crossover.IProcessTaskAgentCrossoverMapper;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentVo;

import java.util.List;

/**
 * @author linbq
 * @since 2021/10/9 20:01
 **/
public interface ProcessTaskAgentMapper extends IProcessTaskAgentCrossoverMapper {

    List<Long> getProcessTaskAgentIdListByFromUserUuid(String fromUserUuid);

    List<ProcessTaskAgentVo> getProcessTaskAgentListByFromUserUuid(String fromUserUuid);

    List<ProcessTaskAgentVo> getProcessTaskAgentListByToUserUuid(String toUserUuid);

    List<ProcessTaskAgentTargetVo> getProcessTaskAgentTargetListByProcessTaskAgentId(Long processTaskAgentId);

    List<ProcessTaskAgentVo> getProcessTaskAgentDetailListByToUserUuid(String toUserUuid);

    List<ProcessTaskAgentVo> getProcessTaskAgentDetailListByFromUserUuidList(List<String> fromUserUuidList);

    int insertProcessTaskAgent(ProcessTaskAgentVo processTaskAgentVo);

    int insertIgnoreProcessTaskAgentTarget(ProcessTaskAgentTargetVo processTaskAgentTargetVo);

    int updateProcessTaskAgentIsActiveByFromUserUuid(String fromUserUuid);

    int deleteProcessTaskAgentByFromUserUuid(String fromUserUuid);

    int deleteProcessTaskAgentTargetByProcessTaskAgentIdList(List<Long> processTaskAgentIdList);
}
