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

package neatlogic.module.process.service;

import neatlogic.framework.process.crossover.IProcessTaskAgentCrossoverService;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import neatlogic.framework.process.dto.agent.ProcessTaskAgentVo;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAgentMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author linbq
 * @since 2021/10/11 15:51
 **/
@Service
public class ProcessTaskAgentServiceImpl implements ProcessTaskAgentService, IProcessTaskAgentCrossoverService {

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public List<String> getFromUserUuidListByToUserUuidAndChannelUuid(String toUserUuid, String channelUuid) {
        List<String> fromUserUuidList = new ArrayList<>();
        List<ProcessTaskAgentVo> processTaskAgentList = processTaskAgentMapper.getProcessTaskAgentDetailListByToUserUuid(toUserUuid);
        for (ProcessTaskAgentVo processTaskAgentVo : processTaskAgentList) {
            String fromUserUuid = processTaskAgentVo.getFromUserUuid();
            if (fromUserUuidList.contains(fromUserUuid)) {
                continue;
            }
            boolean flag = false;
            List<String> catalogUuidList = new ArrayList<>();
            List<ProcessTaskAgentTargetVo> processTaskAgentTargetList = processTaskAgentVo.getProcessTaskAgentTargetVos();
            for (ProcessTaskAgentTargetVo processTaskAgentTargetVo : processTaskAgentTargetList) {
                String type = processTaskAgentTargetVo.getType();
                if ("channel".equals(type)) {
                    if (channelUuid.equals(processTaskAgentTargetVo.getTarget())) {
                        flag = true;
                        break;
                    }
                } else if ("catalog".equals(type)) {
                    catalogUuidList.add(processTaskAgentTargetVo.getTarget());
                }
            }
            if (!flag && CollectionUtils.isNotEmpty(catalogUuidList)) {
                ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
                CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
                List<String> upwardUuidList = catalogMapper.getUpwardUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                flag = catalogUuidList.removeAll(upwardUuidList);
            }
            if (flag) {
                fromUserUuidList.add(fromUserUuid);
            }
        }
        return fromUserUuidList;
    }

    @Override
    public List<String> getChannelUuidListByProcessTaskAgentId(Long processTaskAgentId) {
        List<String> resultList = new ArrayList<>();
        Set<String> catalogUuidList = new HashSet<>();
        List<ProcessTaskAgentTargetVo> processTaskAgentTargetList = processTaskAgentMapper.getProcessTaskAgentTargetListByProcessTaskAgentId(processTaskAgentId);
        for (ProcessTaskAgentTargetVo processTaskAgentTargetVo : processTaskAgentTargetList) {
            String type = processTaskAgentTargetVo.getType();
            if ("channel".equals(type)) {
                if (channelMapper.checkChannelIsExists(processTaskAgentTargetVo.getTarget()) > 0) {
                    resultList.add(processTaskAgentTargetVo.getTarget());
                }
            } else if ("catalog".equals(type)) {
                CatalogVo catalogVo = catalogMapper.getCatalogByUuid(processTaskAgentTargetVo.getTarget());
                if (catalogVo != null) {
                    List<String> downwardUuidList = catalogMapper.getDownwardUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                    catalogUuidList.addAll(downwardUuidList);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(catalogUuidList)) {
            List<String> channelUuidList = channelMapper.getChannelUuidListByParentUuidList(new ArrayList<>(catalogUuidList));
            if (CollectionUtils.isNotEmpty(channelUuidList)) {
                channelUuidList.removeAll(resultList);
                resultList.addAll(channelUuidList);
            }
        }
        return resultList;
    }
}
