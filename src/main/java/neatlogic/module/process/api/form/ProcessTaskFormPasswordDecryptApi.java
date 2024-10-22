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

package neatlogic.module.process.api.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.FormAttributeParentVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.FormUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormPasswordDecryptApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getName() {
        return "nmpaf.processtaskformpassworddecryptapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "formAttributeUuid", type = ApiParamType.STRING,  isRequired = true, desc = "nmpaf.processtaskformpassworddecryptapi.formattributeuuid"),
            @Param(name = "otherParamConfig", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "nmpaf.processtaskformpassworddecryptapi.otherparamconfig")
    })
    @Output({
            @Param(name = "password", type = ApiParamType.STRING, desc = "common.password"),
            @Param(name = "error", type = ApiParamType.STRING, desc = "common.errormsg")
    })
    @Description(desc = "nmpaf.processtaskformpassworddecryptapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            return resultObj;
        }
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(processTaskId);
        if (CollectionUtils.isEmpty(processTaskFormAttributeDataList)) {
            return resultObj;
        }

        JSONObject passwordDecryptionObj = null;
        String formAttributeUuid = paramObj.getString("formAttributeUuid");
        JSONObject otherParamConfig = paramObj.getJSONObject("otherParamConfig");
        String rowUuid = otherParamConfig.getString("rowUuid");
        for (ProcessTaskFormAttributeDataVo processTaskFormAttributeData : processTaskFormAttributeDataList) {
            if (StringUtils.isBlank(processTaskFormAttributeData.getData())) {
                continue;
            }
            IFormAttributeDataConversionHandler formAttributeDataConversionHandler = FormAttributeDataConversionHandlerFactory.getHandler(processTaskFormAttributeData.getHandler());
            if (formAttributeDataConversionHandler == null) {
                continue;
            }
            if (StringUtils.isNotBlank(rowUuid)) {
                if (processTaskFormAttributeData.getData().contains(rowUuid)) {
                    if (Objects.equals(processTaskFormAttributeData.getHandler(), FormHandler.FORMTABLEINPUTER.getHandler())
                            || Objects.equals(processTaskFormAttributeData.getHandler(), FormHandler.FORMSUBASSEMBLY.getHandler())) {
                        passwordDecryptionObj = formAttributeDataConversionHandler.passwordDecryption(processTaskFormAttributeData.getDataObj(), formAttributeUuid, otherParamConfig);
                        if (MapUtils.isNotEmpty(passwordDecryptionObj)) {
                            JSONArray parentUuidList = passwordDecryptionObj.getJSONArray("parentUuidList");
                            if (parentUuidList == null) {
                                parentUuidList = new JSONArray();
                                passwordDecryptionObj.put("parentUuidList", parentUuidList);
                            }
                            parentUuidList.add(processTaskFormAttributeData.getAttributeUuid());
                        }
                    }
                }
            } else {
                if (Objects.equals(processTaskFormAttributeData.getHandler(), FormHandler.FORMPASSWORD.getHandler())) {
                    if (Objects.equals(processTaskFormAttributeData.getAttributeUuid(), formAttributeUuid)) {
                        passwordDecryptionObj = formAttributeDataConversionHandler.passwordDecryption(processTaskFormAttributeData.getDataObj(), formAttributeUuid, otherParamConfig);
                    }
                }
            }
            if (MapUtils.isNotEmpty(passwordDecryptionObj)) {
                break;
            }
        }
        if (MapUtils.isEmpty(passwordDecryptionObj)) {
            return resultObj;
        }
        JSONArray parentUuidList = passwordDecryptionObj.getJSONArray("parentUuidList");
        FormAttributeVo formAttributeVo = null;
        List<FormAttributeVo> allFormAttributeList = FormUtil.getAllFormAttributeList(processTaskFormVo.getFormContent());
        for (FormAttributeVo formAttribute : allFormAttributeList) {
            if (Objects.equals(formAttribute.getUuid(), formAttributeUuid)) {
                boolean flag = true;
                FormAttributeParentVo parent = formAttribute.getParent();
                while (parent != null) {
                    if (!Objects.equals(parent.getHandler(), FormHandler.FORMTAB.getHandler()) && !Objects.equals(parent.getHandler(), FormHandler.FORMCOLLAPSE.getHandler())) {
                        if (!parentUuidList.contains(parent.getUuid())){
                            flag = false;
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
                if (flag) {
                    formAttributeVo = formAttribute;
                    break;
                }
            }
        }
        if (formAttributeVo == null) {
            resultObj.put("error", $.t("nffe.formattributenotfoundexception.formattributenotfoundexception_a", formAttributeUuid));
            return resultObj;
        }
        boolean flag = false;
        JSONObject config = formAttributeVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONArray viewAuthorityList = config.getJSONArray("viewPasswordAuthorityList");
            if (CollectionUtils.isNotEmpty(viewAuthorityList)) {
                String userUuid = UserContext.get().getUserUuid();
                List<String> teamUuidList = UserContext.get().getTeamUuidList();
                List<String> roleUuidList = UserContext.get().getRoleUuidList();
                for (int i = 0; i < viewAuthorityList.size(); i++) {
                    String viewAuthority = viewAuthorityList.getString(i);
                    if (StringUtils.isNotBlank(viewAuthority)) {
                        String[] split = viewAuthority.split("#");
                        if (Objects.equals(split[0], "user")) {
                            if (Objects.equals(split[1], userUuid)) {
                                flag = true;
                                break;
                            }
                        } else if (Objects.equals(split[0], "team")) {
                            if (teamUuidList.contains(split[1])) {
                                flag = true;
                                break;
                            }
                        } else if (Objects.equals(split[0], "role")) {
                            if (roleUuidList.contains(split[1])) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!flag) {
            resultObj.put("error", $.t("nmpaf.processtaskformpassworddecryptapi.nopermissiontoviewpassword"));
            return resultObj;
        }
        String password = passwordDecryptionObj.getString("password");
        resultObj.put("password", password);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "processtask/form/password/decrypt";
    }

//    private JSONObject back(JSONObject paramObj) {
//        JSONObject resultObj = new JSONObject();
//        Long processTaskId = paramObj.getLong("processTaskId");
//        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
//        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
//            return resultObj;
//        }
//        String formAttributeUuid = paramObj.getString("formAttributeUuid");
//        FormAttributeVo formAttributeVo = null;
//        List<FormAttributeVo> allFormAttributeList = FormUtil.getAllFormAttributeList(processTaskFormVo.getFormContent());
//        for (FormAttributeVo formAttribute : allFormAttributeList) {
//            if (Objects.equals(formAttribute.getUuid(), formAttributeUuid)) {
//                formAttributeVo = formAttribute;
//                break;
//            }
//        }
//        if (formAttributeVo == null) {
//            resultObj.put("error", $.t("nffe.formattributenotfoundexception.formattributenotfoundexception_a", formAttributeUuid));
//            return resultObj;
//        }
//        boolean flag = false;
//        JSONObject config = formAttributeVo.getConfig();
//        if (MapUtils.isNotEmpty(config)) {
//            JSONArray viewAuthorityList = config.getJSONArray("viewPasswordAuthorityList");
//            if (CollectionUtils.isNotEmpty(viewAuthorityList)) {
//                String userUuid = UserContext.get().getUserUuid();
//                List<String> teamUuidList = UserContext.get().getTeamUuidList();
//                List<String> roleUuidList = UserContext.get().getRoleUuidList();
//                for (int i = 0; i < viewAuthorityList.size(); i++) {
//                    String viewAuthority = viewAuthorityList.getString(i);
//                    if (StringUtils.isNotBlank(viewAuthority)) {
//                        String[] split = viewAuthority.split("#");
//                        if (Objects.equals(split[0], "user")) {
//                            if (Objects.equals(split[1], userUuid)) {
//                                flag = true;
//                                break;
//                            }
//                        } else if (Objects.equals(split[0], "team")) {
//                            if (teamUuidList.contains(split[1])) {
//                                flag = true;
//                                break;
//                            }
//                        } else if (Objects.equals(split[0], "role")) {
//                            if (roleUuidList.contains(split[1])) {
//                                flag = true;
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (!flag) {
//            resultObj.put("error", $.t("nmpaf.processtaskformpassworddecryptapi.nopermissiontoviewpassword"));
//            return resultObj;
//        }
//        List<FormAttributeParentVo> parentList = new ArrayList<>();
//        FormAttributeParentVo parent = formAttributeVo.getParent();
//        while (parent != null) {
//            if (!Objects.equals(parent.getHandler(), FormHandler.FORMTAB.getHandler()) && !Objects.equals(parent.getHandler(), FormHandler.FORMCOLLAPSE.getHandler())) {
//                parentList.add(0, parent);
//            }
//            parent = parent.getParent();
//        }
//        FormAttributeVo formAttributeVo2 = null;
//        if (CollectionUtils.isNotEmpty(parentList)) {
//            for (FormAttributeVo formAttribute : allFormAttributeList) {
//                if (Objects.equals(formAttribute.getUuid(), parentList.get(0).getUuid())) {
//                    formAttributeVo2 = formAttribute;
//                    break;
//                }
//            }
//        } else {
//            formAttributeVo2 = formAttributeVo;
//        }
//        ProcessTaskFormAttributeDataVo processTaskFormAttributeData = processTaskService.getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(processTaskId, formAttributeVo2.getUuid());
//        IFormAttributeDataConversionHandler formAttributeDataConversionHandler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo2.getHandler());
//        if (formAttributeDataConversionHandler != null) {
//            JSONObject otherParamConfig = paramObj.getJSONObject("otherParamConfig");
//            String result = formAttributeDataConversionHandler.passwordDecryption(processTaskFormAttributeData.getDataObj(), formAttributeVo2.getConfig(), formAttributeUuid, otherParamConfig);
//            resultObj.put("password", result);
//        }
//        return resultObj;
//    }
}
