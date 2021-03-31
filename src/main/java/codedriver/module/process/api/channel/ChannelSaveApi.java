/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.channel;

import java.util.List;

import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.process.exception.channel.ChannelParentUuidCannotBeZeroException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.worktime.WorktimeNotFoundException;
import codedriver.framework.restful.core.IValid;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelRelationVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNameRepeatException;
import codedriver.framework.process.exception.channel.ChannelRelationSettingException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CATALOG_MODIFY;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = CATALOG_MODIFY.class)
public class ChannelSaveApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogMapper catalogMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private PriorityMapper priorityMapper;

    @Resource
    private WorktimeMapper worktimeMapper;

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
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "服务通道名称"),
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
            @Param(name = "defaultPriorityUuid", type = ApiParamType.STRING, isRequired = true, desc = "默认优先级uuid"),
            @Param(name = "priorityUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "关联优先级列表"),
            @Param(name = "priorityUuidList[0]", type = ApiParamType.STRING, isRequired = false, desc = "优先级uuid"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
            @Param(name = "channelTypeUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务类型uuid"),
            @Param(name = "allowTranferReport", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否允许转报"),
            @Param(name = "channelRelationList", type = ApiParamType.JSONARRAY, desc = "转报设置列表")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.STRING, desc = "服务通道uuid")
    })
    @Description(desc = "服务通道保存信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ChannelVo channelVo = JSON.toJavaObject(jsonObj, ChannelVo.class);
        //获取父级信息
        String parentUuid = channelVo.getParentUuid();
        if (CatalogVo.ROOT_UUID.equals(parentUuid)) {
            throw new ChannelParentUuidCannotBeZeroException();
        }
        if (catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
            throw new CatalogNotFoundException(parentUuid);
        }
        if (channelMapper.checkChannelNameIsRepeat(channelVo) > 0) {
            throw new ChannelNameRepeatException(channelVo.getName());
        }
        int sort;
        String uuid = channelVo.getUuid();
        ChannelVo existedChannel = channelMapper.getChannelByUuid(uuid);
        if (existedChannel == null) {//新增
            channelVo.setUuid(null);
            uuid = channelVo.getUuid();
            sort = channelMapper.getMaxSortByParentUuid(parentUuid) + 1;
        } else {//修改
            channelMapper.deleteChannelPriorityByChannelUuid(uuid);
            channelMapper.deleteChannelAuthorityByChannelUuid(uuid);
            sort = existedChannel.getSort();
        }
        channelVo.setSort(sort);
        channelMapper.replaceChannel(channelVo);
        if (processMapper.checkProcessIsExists(channelVo.getProcessUuid()) == 0) {
            throw new ProcessNotFoundException(channelVo.getProcessUuid());
        }
        channelMapper.replaceChannelProcess(uuid, channelVo.getProcessUuid());

        if (worktimeMapper.checkWorktimeIsExists(channelVo.getWorktimeUuid()) == 0) {
            throw new WorktimeNotFoundException(channelVo.getWorktimeUuid());
        }
        channelMapper.replaceChannelWorktime(uuid, channelVo.getWorktimeUuid());
        String defaultPriorityUuid = channelVo.getDefaultPriorityUuid();
        List<String> priorityUuidList = channelVo.getPriorityUuidList();
        for (String priorityUuid : priorityUuidList) {
            if (priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
                throw new PriorityNotFoundException(priorityUuid);
            }
            ChannelPriorityVo channelPriority = new ChannelPriorityVo();
            channelPriority.setChannelUuid(uuid);
            channelPriority.setPriorityUuid(priorityUuid);
            if (defaultPriorityUuid.equals(priorityUuid)) {
                channelPriority.setIsDefault(1);
            } else {
                channelPriority.setIsDefault(0);
            }
            channelMapper.insertChannelPriority(channelPriority);
        }
        List<AuthorityVo> authorityList = channelVo.getAuthorityVoList();
        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (AuthorityVo authorityVo : authorityList) {
                channelMapper.insertChannelAuthority(authorityVo, channelVo.getUuid());
            }
        }
        /** 转报设置逻辑，允许转报后，转报设置必填 **/
        channelMapper.deleteChannelRelationBySource(channelVo.getUuid());
        channelMapper.deleteChannelRelationAuthorityBySource(channelVo.getUuid());
        if (channelVo.getAllowTranferReport() == 1) {
            if (CollectionUtils.isEmpty(channelVo.getChannelRelationList())) {
                throw new ChannelRelationSettingException();
            }
            for (ChannelRelationVo channelRelationVo : channelVo.getChannelRelationList()) {
                channelRelationVo.setSource(channelVo.getUuid());
                for (String typeAndtarget : channelRelationVo.getTargetList()) {
                    if (typeAndtarget.contains("#")) {
                        String[] split = typeAndtarget.split("#");
                        channelRelationVo.setType(split[0]);
                        channelRelationVo.setTarget(split[1]);
                        channelMapper.insertChannelRelation(channelRelationVo);
                    }
                }
                for (String authority : channelRelationVo.getAuthorityList()) {
                    if (authority.contains("#")) {
                        String[] split = authority.split("#");
                        channelRelationVo.setType(split[0]);
                        channelRelationVo.setUuid(split[1]);
                        channelMapper.insertChannelRelationAuthority(channelRelationVo);
                    }

                }
            }
        }
        return uuid;
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
