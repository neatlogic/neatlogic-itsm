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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.portal.widgetdata.core.PortalWidgetDataHandlerBase;
import neatlogic.framework.process.constvalue.CatalogChannelAuthorityAction;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class FavoritedServiceListPortalWidgetDataHandler extends PortalWidgetDataHandlerBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getHandler() {
        return "process.favoritedServiceList";
    }

    @Override
    protected JSONObject getMyData(JSONObject jsonObj) {
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", new ArrayList<>());
        ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
        channelVo.setIsFavorite(1);
        channelVo.setParentUuid(null);
        /* 查询所有收藏的服务时，parentUuid设为空即可 **/
//        if(channelVo.getIsFavorite() != null && channelVo.getIsFavorite() == 1 && "0".equals(channelVo.getParentUuid())){
//            channelVo.setParentUuid(null);
//        }
        channelVo.setUserUuid(UserContext.get().getUserUuid(true));
        boolean hasData = true;
        Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
        if(Objects.equal(isAuthenticate, 1)) {
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            List<String> authorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), CatalogChannelAuthorityAction.REPORT.getValue(), null);
            if(CollectionUtils.isNotEmpty(authorizedChannelUuidList)) {
                String channelUuid = jsonObj.getString("channelUuid");
                if(StringUtils.isNotBlank(channelUuid) && channelMapper.checkChannelIsExists(channelUuid) == 0) {
                    throw new ChannelNotFoundException(channelUuid);
                }
                Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
                if(channelTypeRelationId != null && channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
                    throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
                }
                if(StringUtils.isNotBlank(channelUuid) && channelTypeRelationId != null) {
                    List<String> channelRelationTargetChannelUuidList = catalogService.getChannelRelationTargetChannelUuidList(channelUuid, channelTypeRelationId);
                    if(CollectionUtils.isNotEmpty(channelRelationTargetChannelUuidList)) {
                        channelVo.setAuthorizedUuidList(ListUtils.retainAll(authorizedChannelUuidList, channelRelationTargetChannelUuidList));
                    }
                }else {
                    channelVo.setAuthorizedUuidList(authorizedChannelUuidList);
                }
            }
            //查出当前用户已授权的服务
//			channelVo.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
            channelVo.setIsActive(1);
            hasData = CollectionUtils.isNotEmpty(channelVo.getAuthorizedUuidList());
        }
        if(hasData) {
            int pageCount = 0;
            if(channelVo.getNeedPage()) {
                int rowNum = channelMapper.searchChannelCount(channelVo);
                pageCount = PageUtil.getPageCount(rowNum,channelVo.getPageSize());
                resultObj.put("currentPage",channelVo.getCurrentPage());
                resultObj.put("pageSize",channelVo.getPageSize());
                resultObj.put("pageCount", pageCount);
                resultObj.put("rowNum", rowNum);
            }
            if(!channelVo.getNeedPage() || channelVo.getCurrentPage() <= pageCount) {
                List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
                resultObj.put("tbodyList", channelList);
            }
        }
        return resultObj;
    }
}
