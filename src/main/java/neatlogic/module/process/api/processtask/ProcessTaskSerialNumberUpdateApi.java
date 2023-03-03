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

package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.CHANNELTYPE_MODIFY;
import neatlogic.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberUpdateInProcessException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskSerialNumberService;
import neatlogic.module.process.thread.ProcessTaskSerialNumberUpdateThread;
import com.alibaba.fastjson.JSONObject;
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
