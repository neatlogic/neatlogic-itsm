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
