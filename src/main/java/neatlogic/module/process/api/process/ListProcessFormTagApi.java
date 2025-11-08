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
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AuthAction(action = NoAuth.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListProcessFormTagApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "process/form/tag/list";
    }

    @Override
    public String getName() {
        return "nmtaf.listformtagapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.STRING, desc = "term.itsm.processtaskid"),
            @Param(name = "formUuid", type = ApiParamType.STRING, desc = "term.framework.formuuid")
    })
    @Output({
            @Param(name = "tbodyList", explode = String[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmtaf.listformtagapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FormVersionVo formVersionVo = null;
        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskId != null) {
            ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (processTaskForm != null) {
                formVersionVo = new FormVersionVo();
                formVersionVo.setFormUuid(processTaskForm.getFormUuid());
                formVersionVo.setFormName(processTaskForm.getFormName());
                formVersionVo.setFormConfigStr(processTaskForm.getFormContent());
            }
        } else {
            String formUuid = jsonObj.getString("formUuid");
            FormVo form = formMapper.getFormByUuid(formUuid);
            if (form == null) {
                throw new FormNotFoundException(formUuid);
            }
            formVersionVo = formMapper.getActionFormVersionByFormUuid(formUuid);
            if (formVersionVo == null) {
                throw new FormActiveVersionNotFoundExcepiton(form.getName());
            }
        }
        Set<String> tagSet = new HashSet<>();
        if (formVersionVo != null) {
            List<FormAttributeVo> formCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : formCustomExtendAttributeList) {
                    tagSet.add(formAttributeVo.getTag());
                }
            }
        }
        return TableResultUtil.getResult(new ArrayList<>(tagSet));
    }

}
