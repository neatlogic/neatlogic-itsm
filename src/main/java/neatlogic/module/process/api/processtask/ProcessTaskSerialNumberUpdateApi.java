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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.CHANNELTYPE_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberUpdateInProcessException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import neatlogic.module.process.service.ProcessTaskSerialNumberService;
import neatlogic.module.process.thread.ProcessTaskSerialNumberUpdateThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
@AuthAction(action = CHANNELTYPE_MODIFY.class)
public class ProcessTaskSerialNumberUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;
    @Autowired
    private ProcessTaskSerialNumberService processTaskSerialNumberService;

    @Override
    public String getToken() {
        return "processtask/serialnumber/update";
    }

    @Override
    public String getName() {
        return "更新历史工单的工单号";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input(@Param(name = "channelTypeUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务类型uuid"))
    @Description(desc = "更新历史工单的工单号")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String channelTypeUuid = jsonObj.getString("channelTypeUuid");
        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo =
                processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyByChannelTypeUuid(channelTypeUuid);
        if (processTaskSerialNumberPolicyVo == null) {
            throw new ProcessTaskSerialNumberPolicyNotFoundException(channelTypeUuid);
        }
        if (processTaskSerialNumberPolicyVo.getStartTime() != null
                && processTaskSerialNumberPolicyVo.getEndTime() == null) {
            throw new ProcessTaskSerialNumberUpdateInProcessException();
        }
        IProcessTaskSerialNumberPolicyHandler handler =
                ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(processTaskSerialNumberPolicyVo.getHandler());
        if (handler == null) {
            throw new ProcessTaskSerialNumberPolicyHandlerNotFoundException(
                    processTaskSerialNumberPolicyVo.getHandler());
        }
        Function<ProcessTaskSerialNumberPolicyVo, Long> function = (serialNumberPolicyVo) -> {
            IProcessTaskSerialNumberPolicyHandler policyHandler =
                    ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(serialNumberPolicyVo.getHandler());
            if (policyHandler == null) {
                throw new ProcessTaskSerialNumberPolicyHandlerNotFoundException(serialNumberPolicyVo.getHandler());
            }
            return policyHandler.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(serialNumberPolicyVo);
        };
        processTaskSerialNumberService.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(channelTypeUuid, function);
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyStartTimeByChannelTypeUuid(channelTypeUuid);
        CachedThreadPool.execute(new ProcessTaskSerialNumberUpdateThread(handler, processTaskSerialNumberPolicyVo));
        return null;
    }

}
