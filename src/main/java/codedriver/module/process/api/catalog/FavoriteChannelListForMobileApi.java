/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.catalog;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import com.alibaba.fastjson.JSONObject;
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
