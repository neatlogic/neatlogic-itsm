/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskWhichCurrentStepIsTagStepOfMineApi extends PublicApiComponentBase {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "processtask/currentstepistagstepofmine/list";
    }

    @Override
    public String getName() {
        return "我的待办的工单中当前处理节点是打了某个标签的节点的工单列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "用户ID"),
            @Param(name = "tag", type = ApiParamType.STRING, isRequired = true, desc = "标签名称"),
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "工单ID"),
            @Param(name = "title", type = ApiParamType.STRING, desc = "工单标题"),
            @Param(name = "channelName", type = ApiParamType.STRING, desc = "服务名称"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "工单状态"),
            @Param(name = "stepName", type = ApiParamType.STRING, desc = "当前步骤"),
            @Param(name = "stepStatus", type = ApiParamType.STRING, desc = "当前步骤状态"),
            @Param(name = "userId", type = ApiParamType.STRING, desc = "当前步骤处理人id"),
            @Param(name = "userName", type = ApiParamType.STRING, desc = "当前步骤处理人名称"),
            @Param(name = "teamName", type = ApiParamType.STRING, desc = "当前步骤处理组"),
            @Param(name = "roleName", type = ApiParamType.STRING, desc = "当前步骤处理角色"),
    })
    @Description(desc = "我的待办的工单中当前处理节点是打了某个标签的节点的工单列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String userId = jsonObj.getString("userId");
        String tag = jsonObj.getString("tag");
        UserVo user = userMapper.getUserByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        AuthenticationInfoVo authenticationInfo = authenticationInfoService.getAuthenticationInfo(user.getUuid());
        return processTaskMapper.getProcessTaskListWhichIsProcessingByUserAndTag(tag, user.getUuid(), authenticationInfo.getTeamUuidList(), authenticationInfo.getRoleUuidList());
    }


}
