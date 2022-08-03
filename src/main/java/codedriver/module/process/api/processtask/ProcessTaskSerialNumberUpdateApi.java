/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.CHANNELTYPE_MODIFY;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyNotFoundException;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberUpdateInProcessException;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import codedriver.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskSerialNumberService;
import codedriver.module.process.thread.ProcessTaskSerialNumberUpdateThread;
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
