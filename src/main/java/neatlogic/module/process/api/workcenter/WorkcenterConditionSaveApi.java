/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.workcenter;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.WORKCENTER_MODIFY;
import neatlogic.framework.process.constvalue.ProcessWorkcenterType;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterCanNotEditFactoryException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoCustomAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNoModifyAuthException;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Transactional
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class WorkcenterConditionSaveApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "workcenter/condition/save";
    }

    @Override
    public String getName() {
        return "工单中心分类条件修改接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "分类过滤配置，json格式", isRequired = true)
    })
    @Description(desc = "工单中心分类条件修改接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo workcenterVo = workcenterMapper.getWorkcenterByUuid(uuid);
        if (workcenterVo == null) {
            throw new WorkcenterNotFoundException(uuid);
        }
        if (ProcessWorkcenterType.FACTORY.getValue().equals(workcenterVo.getType())) {
            throw new WorkcenterCanNotEditFactoryException();
        } else if (ProcessWorkcenterType.SYSTEM.getValue().equals(workcenterVo.getType()) && !AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
            throw new WorkcenterNoModifyAuthException();
        } else if (ProcessWorkcenterType.CUSTOM.getValue().equals(workcenterVo.getType()) && !workcenterVo.getOwner().equalsIgnoreCase(UserContext.get().getUserUuid(true))) {
            throw new WorkcenterNoCustomAuthException();
        }
        workcenterVo.setConditionConfig(jsonObj.getJSONObject("conditionConfig"));
        workcenterMapper.updateWorkcenterCondition(workcenterVo);
        return null;
    }

}
