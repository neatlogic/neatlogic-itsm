/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
