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

package neatlogic.module.process.api.processtask.manualintervention;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESSTASK_MODIFY.class)
public class UpdateProcessTaskFormConfigApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpapm.updateprocesstaskformconfigapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "formUuid", type = ApiParamType.LONG, desc = "term.framework.formuuid"),
    })
    @Output({})
    @Description(desc = "nmpapm.updateprocesstaskformconfigapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        String formUuid = paramObj.getString("formUuid");
        if (StringUtils.isBlank(formUuid)) {
            ProcessVo processVo = processMapper.getProcessByUuid(processTaskVo.getProcessUuid());
            if (processVo == null) {
                throw new ProcessNotFoundException(processTaskVo.getProcessUuid());
            }
            formUuid = processVo.getFormUuid();
        }
        if (StringUtils.isNotBlank(formUuid)) {
            FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(formUuid);
            if (formVersion == null) {
                throw new FormActiveVersionNotFoundExcepiton(formUuid);
            }
            String formContent = formVersion.getFormConfigStr();
            String formContentHash = DigestUtils.md5DigestAsHex(formContent.getBytes());
            ProcessTaskFormVo processTaskFormVo = new ProcessTaskFormVo();
            processTaskFormVo.setFormContent(formContent);
            processTaskFormVo.setFormContentHash(formContentHash);
            processTaskMapper.insertIgnoreProcessTaskFormContent(processTaskFormVo);

            processTaskMapper.deleteProcessTaskFormByProcessTaskId(processTaskId);
            processTaskFormVo.setProcessTaskId(processTaskId);
            processTaskFormVo.setFormUuid(formUuid);
            processTaskFormVo.setFormName(formVersion.getFormName());
            processTaskMapper.insertProcessTaskForm(processTaskFormVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "manualintervention/processtask/form/config/update";
    }
}
