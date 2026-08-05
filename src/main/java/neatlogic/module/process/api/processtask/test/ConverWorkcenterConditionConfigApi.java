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

package neatlogic.module.process.api.processtask.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Title: RandomCreateProcessTaskApi
 * @Package processtask
 * @Description: 1、create:随机获取服务、用户、优先级创建工单
 * 2、execute:随机执行工单步骤(因为异步原因，在create后需延迟50s后再执行工单)
 * @Author: 89770
 * @Date: 2020/12/28 10:49
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
class ConverWorkcenterConditionConfigApi extends PrivateApiComponentBase {
    @Resource
    private WorkcenterMapper workcenterMapper;


    @Override
    public String getName() {
        return "nmpapt.converworkcenterconditionconfigapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Description(desc = "nmpapt.converworkcenterconditionconfigapi.getname")
    public Object myDoService(JSONObject paramJson) throws Exception {
        List<WorkcenterVo> workcenterList = workcenterMapper.getAllWorkcenterConditionConfig();
        if (CollectionUtils.isNotEmpty(workcenterList)) {
            for (WorkcenterVo workcenterVo : workcenterList) {
                if (MapUtils.isNotEmpty(workcenterVo.getConditionConfig())) {
                    if (workcenterVo.getConditionConfig().containsKey("conditionConfig")) {
                        workcenterVo.setConditionConfig(workcenterVo.getConditionConfig().getJSONObject("conditionConfig"));
                        workcenterMapper.updateWorkcenterCondition(workcenterVo);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "workcenter/converconditionconfig";
    }


}
