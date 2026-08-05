/*
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package neatlogic.module.process.portal.widget.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.service.ChannelService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FavoritedServiceListPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private ChannelService channelService;

    @Override
    public String getHandler() {
        return "process.favoritedServiceList";
    }

    @Override
    protected JSONObject getMyData(JSONObject jsonObj) {
        Integer isAuthenticate = 1;
        String channelUuid = null;
        Long channelTypeRelationId = null;
        ChannelVo channelVo = jsonObj.toJavaObject(ChannelVo.class);
        channelVo.setIsFavorite(1);
        channelVo.setParentUuid("0");
        List<ChannelVo> channelList = channelService.searchChannelList(channelVo, isAuthenticate, channelUuid, channelTypeRelationId);
        return TableResultUtil.getResult(channelList, channelVo);
    }
}
