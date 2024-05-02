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

import neatlogic.framework.process.crossover.IProcessTaskSerialNumberCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessTaskSerialNumberMapper extends IProcessTaskSerialNumberCrossoverMapper {

    ProcessTaskSerialNumberPolicyVo getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(String channelTypeUuid);

    ProcessTaskSerialNumberPolicyVo getProcessTaskSerialNumberPolicyByChannelTypeUuid(String channelTypeUuid);

    List<ProcessTaskSerialNumberPolicyVo> getProcessTaskSerialNumberPolicyListByHandler(String handler);

    int insertProcessTaskSerialNumberPolicy(ProcessTaskSerialNumberPolicyVo policyVo);

    int insertProcessTaskSerialNumber(@Param("processTaskId") Long processTaskId, @Param("serialNumber") String serialNumber);

    int updateProcessTaskSerialNumberPolicyByChannelTypeUuid(ProcessTaskSerialNumberPolicyVo policyVo);

    int updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(@Param("channelTypeUuid") String channelTypeUuid, @Param("serialNumberSeed") Long serialNumberSeed);

    int updateProcessTaskSerialNumberPolicyStartTimeByChannelTypeUuid(String channelTypeUuid);

    int updateProcessTaskSerialNumberPolicyEndTimeByChannelTypeUuid(String channelTypeUuid);

    int deleteProcessTaskSerialNumberPolicyByChannelTypeUuid(String channelTypeUuid);

    int deleteProcessTaskSerialNumberByProcessTaskId(Long processTaskId);
}
