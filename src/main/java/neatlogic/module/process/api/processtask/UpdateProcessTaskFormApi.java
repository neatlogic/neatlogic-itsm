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
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskStepDataType;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESSTASK_MODIFY.class)
public class UpdateProcessTaskFormApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getName() {
        return "nmpap.updateprocesstaskformapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "term.itsm.formattributedatalist"),
            @Param(name = "formExtendAttributeDataList", type = ApiParamType.JSONARRAY, desc = "term.itsm.formextendattributedatalist"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.hidecomponentlist"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.readcomponentlist"),
    })
    @Description(desc = "nmpap.updateprocesstaskformapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskId);
        processTaskStepVo.setIsAutoGenerateId(false);
        JSONObject param = processTaskStepVo.getParamObj();
        param.put("formAttributeDataList", paramObj.getJSONArray("formAttributeDataList"));
        param.put("formExtendAttributeDataList", paramObj.getJSONArray("formExtendAttributeDataList"));
        param.put("hidecomponentList", paramObj.getJSONArray("hidecomponentList"));
        param.put("readcomponentList", paramObj.getJSONArray("readcomponentList"));
        param.put("needVerifyIsRequired", false);
        param.put("source", paramObj.getString("source"));
        processStepHandlerUtil.saveForm(processTaskStepVo);
        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UPDATEFORM);

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskId);
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        List<ProcessTaskStepDataVo> stepDraftSaveDataList = processTaskStepDataMapper.searchProcessTaskStepData(processTaskStepDataVo);
        for (ProcessTaskStepDataVo stepDraftSaveData : stepDraftSaveDataList) {
            processTaskStepDataMapper.deleteProcessTaskStepDataById(stepDraftSaveData.getId());
        }
        //创建全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.createIndex(processTaskId);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/form/update";
    }
}
