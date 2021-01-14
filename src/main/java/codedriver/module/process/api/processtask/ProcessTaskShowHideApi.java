package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESSTASK_MODIFY;
@Service
@Transactional
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskShowHideApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper taskMapper;
       
    @Override
    public String getToken() {
        return "processtask/show/hide";
    }

    @Override
    public String getName() {
        return "隐藏显示工单";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id", isRequired = true),
        @Param(name = "isShow", type = ApiParamType.INTEGER, desc = "是否 1显示/0隐藏", isRequired = true)
    })
    @Description(desc = "隐藏显示工单")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Integer isShow = jsonObj.getInteger("isShow");
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setId(processTaskId);
        processTaskVo.setIsShow(isShow);
        taskMapper.updateProcessTaskIsShow(processTaskVo);
        return null;
    }

}
