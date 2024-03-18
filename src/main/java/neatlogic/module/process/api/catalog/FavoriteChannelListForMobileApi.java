/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.catalog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FavoriteChannelListForMobileApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getToken() {
        return "/channel/favorite/list/mobile";
    }

    @Override
    public String getName() {
        return "获取收藏的服务列表(移动端)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "favoriteList", explode = ChannelVo[].class, desc = "收藏的服务列表")
    })
    @Description(desc = "获取收藏的服务列表(移动端)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        ChannelVo paramChannel = new ChannelVo();
        paramChannel.setUserUuid(UserContext.get().getUserUuid(true));
        //查出当前用户已授权的服务
        paramChannel.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
        paramChannel.setIsActive(1);
        List<ChannelVo> favoriteChannelList = channelMapper.getFavoriteChannelList(paramChannel);
        resultObj.put("favoriteList", favoriteChannelList);
        return resultObj;
    }

}
