/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.agent;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.user.*;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAgentMapper;
import codedriver.framework.process.dto.agent.ProcessTaskAgentCompobVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentInfoVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAgentSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private CatalogMapper catalogMapper;

    @Override
    public String getToken() {
        return "processtask/agent/save";
    }

    @Override
    public String getName() {
        return "保存用户任务授权信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "beginTime", type = ApiParamType.LONG, isRequired = true, desc = "开始时间"),
            @Param(name = "endTime", type = ApiParamType.LONG, isRequired = true, desc = "结束时间"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "启用"),
            @Param(name = "compobList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "授权对象列表")
    })
    @Output({})
    @Description(desc = "保存用户任务授权信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskAgentInfoVo processTaskAgentInfoVo = JSONObject.toJavaObject(jsonObj, ProcessTaskAgentInfoVo.class);
        List<ProcessTaskAgentCompobVo> compobList = processTaskAgentInfoVo.getCompobList();
        if (CollectionUtils.isEmpty(compobList)) {
            throw new ParamIrregularException("compobList");
        }
        String fromUserUuid = UserContext.get().getUserUuid(true);
        List<Long> processTaskAgentIdList = processTaskAgentMapper.getProcessTaskAgentIdListByFromUserUuid(fromUserUuid);
        if (CollectionUtils.isNotEmpty(processTaskAgentIdList)) {
            processTaskAgentMapper.deleteProcessTaskAgentByFromUserUuid(fromUserUuid);
            processTaskAgentMapper.deleteProcessTaskAgentTargetByProcessTaskAgentIdList(processTaskAgentIdList);
        }

        ProcessTaskAgentVo processTaskAgentVo = new ProcessTaskAgentVo();
        processTaskAgentVo.setBeginTime(processTaskAgentInfoVo.getBeginTime());
        processTaskAgentVo.setEndTime(processTaskAgentInfoVo.getEndTime());
        processTaskAgentVo.setIsActive(processTaskAgentInfoVo.getIsActive());
        processTaskAgentVo.setFromUserUuid(fromUserUuid);
        for (ProcessTaskAgentCompobVo compobVo : compobList) {
            processTaskAgentVo.setId(null);
            String toUserUuid = compobVo.getToUserUuid();
            if (toUserUuid.contains(GroupSearch.USER.getValuePlugin())) {
                toUserUuid = toUserUuid.substring(5);
            }
            if (userMapper.checkUserIsExists(toUserUuid) == 0) {
                throw new UserNotFoundException(toUserUuid);
            }
            processTaskAgentVo.setToUserUuid(toUserUuid);
            processTaskAgentMapper.insertProcessTaskAgent(processTaskAgentVo);
            List<ProcessTaskAgentTargetVo> targetList = compobVo.getTargetList();
            for (ProcessTaskAgentTargetVo target : targetList) {
                if ("channel".equals(target.getType())) {
                    if (channelMapper.checkChannelIsExists(target.getTarget()) == 0) {
                        throw new ChannelNotFoundException(target.getTarget());
                    }
                } else if ("catalog".equals(target.getType())) {
                    if (catalogMapper.checkCatalogIsExists(target.getTarget()) == 0) {
                        throw new CatalogNotFoundException(target.getTarget());
                    }
                }
                target.setProcessTaskAgentId(processTaskAgentVo.getId());
                processTaskAgentMapper.insertProcessTaskAgentTarget(target);
            }
        }
        return null;
    }

}
