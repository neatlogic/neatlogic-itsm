/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.channel;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.auth.CATALOG_MODIFY;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelNameRepeatException;
import neatlogic.framework.process.service.ChannelService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        return "服务通道保存信息接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "服务通道uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "服务通道名称"),
            @Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "父级uuid"),
            @Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作流uuid"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
            @Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
            @Param(name = "support", type = ApiParamType.STRING, isRequired = true, desc = "使用范围，all/pc/mobile"),
            @Param(name = "desc", type = ApiParamType.STRING, desc = "服务说明", maxLength = 200, xss = true),
            @Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
            @Param(name = "color", type = ApiParamType.STRING, desc = "颜色"),
            @Param(name = "sla", type = ApiParamType.INTEGER, desc = "时效(单位：小时)"),
            @Param(name = "allowDesc", type = ApiParamType.ENUM, desc = "是否显示上报页描述", rule = "0,1"),
            @Param(name = "isActiveHelp", type = ApiParamType.ENUM, desc = "是否激活描述", rule = "0,1"),
            @Param(name = "help", type = ApiParamType.STRING, desc = "描述帮助"),
            @Param(name = "isNeedPriority", type = ApiParamType.INTEGER, isRequired = true, desc = "是否显示优先级"),
            @Param(name = "defaultPriorityUuid", type = ApiParamType.STRING, desc = "默认优先级uuid"),
            @Param(name = "priorityUuidList", type = ApiParamType.JSONARRAY, desc = "关联优先级列表"),
            @Param(name = "priorityUuidList[0]", type = ApiParamType.STRING, desc = "优先级uuid"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
            @Param(name = "channelTypeUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务类型uuid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置信息")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "服务通道uuid")
    })
    @Description(desc = "服务通道保存信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ChannelVo channelVo = JSON.toJavaObject(jsonObj, ChannelVo.class);
        return channelService.saveChannel(channelVo);
    }

    public IValid name() {
        return value -> {
            /** 需要传parentUuid，同一个目录下，不能出现重名服务 **/
            ChannelVo channelVo = JSON.toJavaObject(value, ChannelVo.class);
            if (channelMapper.checkChannelNameIsRepeat(channelVo) > 0) {
                return new FieldValidResultVo(new ChannelNameRepeatException(channelVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
