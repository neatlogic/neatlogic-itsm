/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
