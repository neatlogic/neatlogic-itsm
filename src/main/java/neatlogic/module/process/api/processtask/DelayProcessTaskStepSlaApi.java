/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepSlaDelayVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSlaMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class DelayProcessTaskStepSlaApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getName() {
        return "nmpap.delayprocesstaskstepslaapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "targetProcessTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.targetprocesstaskstepid"),
            @Param(name = "time", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.delaytime"),
            @Param(name = "unit", type = ApiParamType.ENUM, rule = "day,hour,minute", defaultValue = "minute", desc = "common.timeunit", help = "day:天,hour:小时,minute:分钟，默认为minute"),
    })
    @Output({})
    @Description(desc = "nmpap.delayprocesstaskstepslaapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        System.out.println("paramObj = " + paramObj);
        Long processTaskId = paramObj.getLong("processTaskId");
        Long targetProcessTaskStepId = paramObj.getLong("targetProcessTaskStepId");
        Long time = paramObj.getLong("time");
        String unit = paramObj.getString("unit");

        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        ProcessTaskStepVo targetProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(targetProcessTaskStepId);
        if (targetProcessTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(targetProcessTaskStepId);
        }

        long millis = 0;
        if ("hour".equals(unit)) {
            millis = TimeUnit.HOURS.toMillis(time);
        } else if ("day".equals(unit)) {
            millis = TimeUnit.DAYS.toMillis(time);
        } else {
            millis =  TimeUnit.MINUTES.toMillis(time);
        }

        List<Long> slaIdList = processTaskSlaMapper.getSlaIdListByProcessTaskStepId(targetProcessTaskStepId);
        if (CollectionUtils.isNotEmpty(slaIdList)) {
            ProcessTaskStepSlaDelayVo processTaskStepSlaDelayVo = new ProcessTaskStepSlaDelayVo();
            processTaskStepSlaDelayVo.setProcessTaskId(processTaskId);
            processTaskStepSlaDelayVo.setTargetProcessTaskId(targetProcessTaskStepVo.getProcessTaskId());
            processTaskStepSlaDelayVo.setTargetProcessTaskStepId(targetProcessTaskStepVo.getId());
            processTaskStepSlaDelayVo.setTime(millis);
            for (Long slaId : slaIdList) {
                processTaskStepSlaDelayVo.setId(null);
                processTaskStepSlaDelayVo.setSlaId(slaId);
                processTaskSlaMapper.insertProcessTaskStepSlaDelay(processTaskStepSlaDelayVo);
            }
            processStepHandlerUtil.calculateSla(targetProcessTaskStepVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/step/sla/delay";
    }
}
