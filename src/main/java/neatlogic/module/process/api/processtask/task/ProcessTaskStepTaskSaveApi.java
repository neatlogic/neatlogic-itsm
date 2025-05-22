/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.processtask.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskStepTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskSaveApi extends PrivateApiComponentBase {

    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/task/save";
    }

    @Override
    public String getName() {
        return "nmrai.saveissueapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "term.rdm.issueid", help = "如果不为空则是编辑，为空则新增"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
            @Param(name = "stepTaskUserVoList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "term.itsm.steptaskuserlist"),
            @Param(name = "taskConfigId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.taskconfigid"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "common.content"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.LONG, desc = "term.rdm.issueid")
    })
    @Description(desc = "nmrai.saveissueapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        String source = jsonObj.getString("source");
        JSONArray stepTaskUserVoList = jsonObj.getJSONArray("stepTaskUserVoList");
        if (CollectionUtils.isEmpty(stepTaskUserVoList)) {
            throw new ParamIrregularException("stepTaskUserVoList");
        }
        ProcessTaskStepTaskVo processTaskStepTaskVo = jsonObj.toJavaObject(ProcessTaskStepTaskVo.class);
        return processTaskStepTaskService.saveTask(id, processTaskStepTaskVo, stepTaskUserVoList, source);
    }
}
