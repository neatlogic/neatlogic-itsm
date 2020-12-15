package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyNotFoundException;
import codedriver.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberUpdateInProcessException;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import codedriver.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import codedriver.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberUpdateThread;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class ProcessTaskSerialNumberUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;
    
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
        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(channelTypeUuid);
        if(processTaskSerialNumberPolicyVo == null) {
            throw new ProcessTaskSerialNumberPolicyNotFoundException(channelTypeUuid);
        }
        if(processTaskSerialNumberPolicyVo.getStartTime() != null && processTaskSerialNumberPolicyVo.getEndTime() == null) {
            throw new ProcessTaskSerialNumberUpdateInProcessException();
        }
        IProcessTaskSerialNumberPolicyHandler handler =
            ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(processTaskSerialNumberPolicyVo.getHandler());
        if (handler == null) {
            throw new ProcessTaskSerialNumberPolicyHandlerNotFoundException(processTaskSerialNumberPolicyVo.getHandler());
        }
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyStartTimeByChannelTypeUuid(channelTypeUuid);
        Long serialNumberSeed = handler.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo);
        if(serialNumberSeed != null) {
            processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(channelTypeUuid, serialNumberSeed);
        }
        CommonThreadPool.execute(new ProcessTaskSerialNumberUpdateThread(handler, processTaskSerialNumberPolicyVo));
        return null;
    }

}
