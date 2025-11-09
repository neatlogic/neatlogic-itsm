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

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class ChannelPathParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.CHANNELPATH.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
            if (channelVo != null) {
                CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
                if (catalogVo != null) {
                    List<CatalogVo> catalogList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
                    List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
                    nameList.add(channelVo.getName());
                    return String.join("/", nameList);
                }
            }
        }
        return null;
    }
}
