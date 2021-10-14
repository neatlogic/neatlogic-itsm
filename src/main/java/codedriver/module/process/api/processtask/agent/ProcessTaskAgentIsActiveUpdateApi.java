/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.agent;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessTaskAgentMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAgentIsActiveUpdateApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAgentMapper processTaskAgentMapper;

    @Override
    public String getToken() {
        return "processtask/agent/isactive/update";
    }

    @Override
    public String getName() {
        return "启用或禁用用户任务授权信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({})
    @Description(desc = "启用或禁用用户任务授权信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        processTaskAgentMapper.updateProcessTaskAgentIsActiveByFromUserUuid(UserContext.get().getUserUuid(true));
        return null;
    }

}
