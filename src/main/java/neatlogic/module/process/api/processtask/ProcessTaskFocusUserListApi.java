/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskFocusUserListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/focususer/list";
    }

    @Override
    public String getName() {
        return "获取工单关注人列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")})
    @Output({
            @Param(name = "focusUserUuidList", type = ApiParamType.JSONARRAY, desc = "工单关注人uuid列表"),
            @Param(name = "isFocus", type = ApiParamType.INTEGER, desc = "当前用户是否关注了当前工单(1:是;0:否)")
    })
    @Description(desc = "获取工单关注人列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        int isFocus = processTaskMapper.checkProcessTaskFocusExists(processTaskId, UserContext.get().getUserUuid()) > 0 ? 1 : 0;
        List<String> focusUserUuidList = processTaskMapper.getFocusUserListByTaskId(processTaskId);
        JSONObject result = new JSONObject();
        result.put("focusUserUuidList", focusUserUuidList);
        result.put("isFocus", isFocus);
        return result;
    }

}
