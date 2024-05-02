/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.CHANNELTYPE_MODIFY;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
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
