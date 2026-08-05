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

package neatlogic.module.process.api.processtask.asynccreate;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;
import neatlogic.framework.process.dto.ProcessTaskCreateVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAsyncCreateMapper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateAsyncCreateProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAsyncCreateMapper processTaskAsyncCreateMapper;

    @Override
    public String getName() {
        return "nmpapa.updateasynccreateprocesstaskapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "nmpapa.updateasynccreateprocesstaskapi.input.param.desc.id"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "title", type = ApiParamType.STRING, desc = "common.title"),
            @Param(name = "status", type = ApiParamType.ENUM, rule = "failed,redo", desc = "common.status"),
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "term.framework.serverid"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "common.config"),
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.BOOLEAN)
    })
    @Description(desc = "nmpapa.updateasynccreateprocesstaskapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        ProcessTaskAsyncCreateVo oldProcessTaskAsyncCreateVo = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateById(id);
        if (oldProcessTaskAsyncCreateVo == null) {
            return false;
        }
        if (!Objects.equals(oldProcessTaskAsyncCreateVo.getStatus(), "failed") && !Objects.equals(oldProcessTaskAsyncCreateVo.getStatus(), "redo")) {
            return false;
        }
        boolean flag = false;
        ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = new ProcessTaskAsyncCreateVo();
        processTaskAsyncCreateVo.setId(id);
        Long processTaskId = paramObj.getLong("processTaskId");
        if (processTaskId != null && !Objects.equals(processTaskId, oldProcessTaskAsyncCreateVo.getProcessTaskId())) {
            processTaskAsyncCreateVo.setProcessTaskId(processTaskId);
            flag = true;
        }
        String title = paramObj.getString("title");
        if (StringUtils.isNotBlank(title) && !Objects.equals(title, oldProcessTaskAsyncCreateVo.getTitle())) {
            processTaskAsyncCreateVo.setTitle(title);
            flag = true;
        }
        String status = paramObj.getString("status");
        if (StringUtils.isNotBlank(status) && !Objects.equals(status, oldProcessTaskAsyncCreateVo.getStatus())) {
            processTaskAsyncCreateVo.setStatus(status);
            flag = true;
        }
        Integer serverId = paramObj.getInteger("serverId");
        if (serverId != null && !Objects.equals(serverId, oldProcessTaskAsyncCreateVo.getServerId())) {
            processTaskAsyncCreateVo.setServerId(serverId);
            flag = true;
        }
        JSONObject config = paramObj.getJSONObject("config");
        if (MapUtils.isNotEmpty(config) && !Objects.equals(config.toJSONString(), JSONObject.toJSONString(oldProcessTaskAsyncCreateVo.getConfig()))) {
            ProcessTaskCreateVo processTaskCreateVo = config.toJavaObject(ProcessTaskCreateVo.class);
            processTaskAsyncCreateVo.setConfig(processTaskCreateVo);
            flag = true;
        }
        if (flag) {
            processTaskAsyncCreateMapper.updateProcessTaskAsyncCreateForManualIntervention(processTaskAsyncCreateVo);
            return true;
        }
        return false;
    }

    @Override
    public String getToken() {
        return "processtask/asynccreate/update";
    }
}
