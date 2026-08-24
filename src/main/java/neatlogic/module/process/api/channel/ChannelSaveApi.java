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

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelNameRepeatException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.service.ChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelSaveApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelService channelService;

    @Override
    public String getToken() {
        return "process/channel/save";
    }

    @Override
    public String getName() {
        return "nmpac.channelsaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "common.name"),
            @Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "common.parentuuid"),
            @Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.processuuid"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "common.isactive", rule = "0,1"),
            @Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "common.worktimeuuid"),
            @Param(name = "support", type = ApiParamType.STRING, isRequired = true, desc = "common.scopeofuse", help = "all/pc/mobile"),
            @Param(name = "desc", type = ApiParamType.STRING, desc = "common.description", maxLength = 200, xss = true),
            @Param(name = "icon", type = ApiParamType.STRING, desc = "common.icon"),
            @Param(name = "color", type = ApiParamType.STRING, desc = "common.color"),
            @Param(name = "sla", type = ApiParamType.INTEGER, desc = "common.sla"),
            @Param(name = "contentHelp", type = ApiParamType.STRING, desc = "term.itsm.contenthelp"),
//            @Param(name = "isNeedPriority", type = ApiParamType.INTEGER, isRequired = true, desc = "common.isneedpriority"),
            @Param(name = "isActivePriority", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsaveapi.input.param.desc.isactivepriority"),
            @Param(name = "isDisplayPriority", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsaveapi.input.param.desc.isdisplaypriority"),
            @Param(name = "defaultPriorityUuid", type = ApiParamType.STRING, desc = "common.defaultpriorityuuid"),
            @Param(name = "priorityUuidList", type = ApiParamType.JSONARRAY, desc = "nmrap.updateprioritysortapi.input.param.desc.prioritylist"),
            @Param(name = "reportAuthorityList", type = ApiParamType.JSONARRAY, desc = "common.reportauthoritylist", help = "nmpac.channelsaveapi.input.param.help.reportauthoritylist"),
            // 代报授权与上报、查看授权使用相同的授权对象格式。
            @Param(name = "delegateAuthorityList", type = ApiParamType.JSONARRAY, desc = "common.delegateauthoritylist"),
            @Param(name = "viewAuthorityList", type = ApiParamType.JSONARRAY, desc = "common.viewauthoritylist", help = "nmpac.channelsaveapi.input.param.help.viewauthoritylist"),
            @Param(name = "channelTypeUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.channeltypeuuid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "common.uuid")
    })
    @Description(desc = "nmpac.channelsaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ChannelVo channelVo = jsonObj.toJavaObject(ChannelVo.class);
        return channelService.saveChannel(channelVo);
    }

    public IValid name() {
        return value -> {
            /* 需要传parentUuid，同一个目录下，不能出现重名服务 */
            ChannelVo channelVo = value.toJavaObject(ChannelVo.class);
            if (channelMapper.checkChannelNameIsRepeat(channelVo) > 0) {
                return new FieldValidResultVo(new ChannelNameRepeatException(channelVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
