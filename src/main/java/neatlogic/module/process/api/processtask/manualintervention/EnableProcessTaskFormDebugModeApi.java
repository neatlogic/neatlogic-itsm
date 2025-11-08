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

package neatlogic.module.process.api.processtask.manualintervention;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESSTASK_MODIFY.class)
public class EnableProcessTaskFormDebugModeApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpapm.enableprocesstaskformdebugmodeapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "enableDebugMode", type = ApiParamType.BOOLEAN, isRequired = true, desc = "common.enabledebugmode")
    })
    @Output({})
    @Description(desc = "nmpapm.enableprocesstaskformdebugmodeapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        boolean flag = false;
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskForm != null) {
            String formContent = processTaskForm.getFormContent();
            if (StringUtils.isNotBlank(formContent)) {
                JSONObject config = JSONObject.parseObject(processTaskForm.getFormContent());
                if (MapUtils.isNotEmpty(config)) {
                    String formContentHash = DigestUtils.md5DigestAsHex(formContent.getBytes());
                    Boolean enableDebugMode = paramObj.getBoolean("enableDebugMode");
                    {
                        JSONArray tableList = config.getJSONArray("tableList");
                        if (CollectionUtils.isNotEmpty(tableList)) {
                            for (int i = 0; i < tableList.size(); i++) {
                                JSONObject tableObj = tableList.getJSONObject(i);
                                if (MapUtils.isNotEmpty(tableObj)) {
                                    JSONObject component = tableObj.getJSONObject("component");
                                    if (MapUtils.isNotEmpty(component)) {
                                        String handler = component.getString("handler");
                                        if (Objects.equals(handler, "formcustom")) {// 自定义表单组件
                                            String componentStr = component.toJSONString();
                                            if (enableDebugMode) {
                                                componentStr = componentStr.replace("\"isShow\":false", "\"isShow\":true");
                                            } else {
                                                componentStr = componentStr.replace("\"isShow\":true", "\"isShow\":false");
                                            }
                                            tableObj.put("component", JSONObject.parseObject(componentStr));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    JSONArray sceneList = config.getJSONArray("sceneList");
                    if (CollectionUtils.isNotEmpty(sceneList)) {
                        for (int j = 0; j < sceneList.size(); j++) {
                            JSONObject sceneObj = sceneList.getJSONObject(j);
                            if (MapUtils.isNotEmpty(sceneObj)) {
                                JSONArray tableList = sceneObj.getJSONArray("tableList");
                                if (CollectionUtils.isNotEmpty(tableList)) {
                                    for (int i = 0; i < tableList.size(); i++) {
                                        JSONObject tableObj = tableList.getJSONObject(i);
                                        if (MapUtils.isNotEmpty(tableObj)) {
                                            JSONObject component = tableObj.getJSONObject("component");
                                            if (MapUtils.isNotEmpty(component)) {
                                                String handler = component.getString("handler");
                                                if (Objects.equals(handler, "formcustom")) {// 自定义表单组件
                                                    String componentStr = component.toJSONString();
                                                    if (enableDebugMode) {
                                                        componentStr = componentStr.replace("\"isShow\":false", "\"isShow\":true");
                                                    } else {
                                                        componentStr = componentStr.replace("\"isShow\":true", "\"isShow\":false");
                                                    }
                                                    tableObj.put("component", JSONObject.parseObject(componentStr));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String newFormContent = config.toJSONString();
                    String newFormContentHash = DigestUtils.md5DigestAsHex(newFormContent.getBytes());
                    if (!Objects.equals(newFormContentHash, formContentHash)) {
                        processTaskForm.setFormContent(newFormContent);
                        processTaskForm.setFormContentHash(newFormContentHash);
                        processTaskMapper.insertIgnoreProcessTaskFormContent(processTaskForm);
                        processTaskMapper.insertProcessTaskForm(processTaskForm);
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    @Override
    public String getToken() {
        return "manualintervention/processtask/form/debug/enable";
    }
}
