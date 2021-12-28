/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/9/14 11:09
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskRepeatDeleteApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/repeat/delete";
    }

    @Override
    public String getName() {
        return "解绑重复工单接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(processTaskId);
        if (repeatGroupId != null) {
            processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(processTaskId);
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
//        processTaskStepVo.setParamObj(jsonObj);
            processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UNBINDREPEAT);
            List<Long> repeatProcessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            if (repeatProcessTaskIdList.size() == 1) {
                processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(repeatProcessTaskIdList.get(0));
                ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
                processTaskStep.setProcessTaskId(repeatProcessTaskIdList.get(0));
//        processTaskStepVo.setParamObj(jsonObj);
                processStepHandlerUtil.audit(processTaskStep, ProcessTaskAuditType.UNBINDREPEAT);
            }
        }
        return null;
    }
}
