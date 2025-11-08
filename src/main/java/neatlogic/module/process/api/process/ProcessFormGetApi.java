/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.form.exception.FormVersionNotFoundException;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessFormGetApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "process/form/get";
    }

    @Override
    public String getName() {
        return "获取表单信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "processTaskId", type = ApiParamType.STRING, desc = "term.itsm.processtaskid"),
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"),
    })
    @Output({@Param(explode = FormVo.class)})
    @Description(desc = "获取表单信息")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskId != null) {
            ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (processTaskForm != null) {
                FormVo formVo = new FormVo();
                formVo.setUuid(processTaskForm.getFormUuid());
                formVo.setName(processTaskForm.getFormName());
                formVo.setIsActive(1);
                formVo.setReferenceCount(1);
                formVo.setFormConfig(JSONObject.parseObject(processTaskForm.getFormContent()));
                return formVo;
            }
            return null;
        } else {
            String currentVersionUuid = jsonObj.getString("currentVersionUuid");
            String uuid = jsonObj.getString("uuid");
            if (StringUtils.isNotBlank(currentVersionUuid)) {
                FormVersionVo formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
                if (formVersion == null) {
                    throw new FormVersionNotFoundException(currentVersionUuid);
                }
                FormVo formVo = formMapper.getFormByUuid(formVersion.getFormUuid());
                if (formVo == null) {
                    throw new FormNotFoundException(formVersion.getFormUuid());
                }
                formVo.setCurrentVersionUuid(currentVersionUuid);
                formVo.setFormConfig(formVersion.getFormConfig());
                List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(formVersion.getFormUuid());
                formVo.setVersionList(formVersionList);
                int count = DependencyManager.getDependencyCount(FrameworkFromType.FORM, formVo.getUuid());
                formVo.setReferenceCount(count);
                return formVo;
            } else if (StringUtils.isNotBlank(uuid)) {
                FormVo formVo = formMapper.getFormByUuid(uuid);
                if (formVo == null) {
                    throw new FormNotFoundException(uuid);
                }
                FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
                if (formVersion == null) {
                    throw new FormActiveVersionNotFoundExcepiton(uuid);
                }
                formVo.setCurrentVersionUuid(formVersion.getUuid());
                formVo.setFormConfig(formVersion.getFormConfig());
                List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
                formVo.setVersionList(formVersionList);
                int count = DependencyManager.getDependencyCount(FrameworkFromType.FORM, formVo.getUuid());
                formVo.setReferenceCount(count);
                return formVo;
            } else {
                throw new ParamNotExistsException("uuid", "currentVersionUuid");
            }
        }
    }

}
