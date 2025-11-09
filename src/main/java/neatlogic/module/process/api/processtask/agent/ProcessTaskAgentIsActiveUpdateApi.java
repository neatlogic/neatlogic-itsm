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

package neatlogic.module.process.api.processtask.agent;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAgentMapper;
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
