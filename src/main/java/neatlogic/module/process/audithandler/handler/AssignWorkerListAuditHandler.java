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

package neatlogic.module.process.audithandler.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssignWorkerListAuditHandler implements IProcessTaskStepAuditDetailHandler {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getType() {
        return ProcessTaskAuditDetailType.ASSIGNWORKERLIST.getValue();
    }

    @Override
    public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
        int result = 0;
        String oldContent = processTaskStepAuditDetailVo.getOldContent();
        if(StringUtils.isNotBlank(oldContent)) {
            String parse = parse(oldContent);
            if (StringUtils.isNotBlank(parse)) {
                processTaskStepAuditDetailVo.setOldContent(parse(oldContent));
                result = 1;
            }
        }
        String newContent = processTaskStepAuditDetailVo.getNewContent();
        if(StringUtils.isNotBlank(newContent)) {
            String parse = parse(newContent);
            if (StringUtils.isNotBlank(parse)) {
                processTaskStepAuditDetailVo.setNewContent(parse);
                result = 1;
            }
        }
        return result;
    }

    private String parse(String content) {
        JSONArray assignWorkerList = new JSONArray();
        JSONArray assignWorkerArray = JSONArray.parseArray(content);
        if (CollectionUtils.isNotEmpty(assignWorkerArray)) {
            for (int i = 0; i < assignWorkerArray.size(); i++) {
                JSONObject assignWorkerObj = assignWorkerArray.getJSONObject(i);
                if (MapUtils.isNotEmpty(assignWorkerObj)) {
                    JSONArray workerArray = assignWorkerObj.getJSONArray("workerList");
                    if (CollectionUtils.isNotEmpty(workerArray)) {
                        JSONObject assignWorker = new JSONObject();
                        Long processTaskStepId = assignWorkerObj.getLong("processTaskStepId");
                        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
                        if (processTaskStepVo != null) {
                            assignWorker.put("processTaskStepName", processTaskStepVo.getName());
                        }
                        List<Map<String, String>> workerList = new ArrayList<>();
                        for(int j = 0; j < workerArray.size(); j++) {
                            String worker = workerArray.getString(j);
                            String[] split = worker.split("#");
                            if(GroupSearch.USER.getValue().equals(split[0])) {
                                UserVo userVo = userMapper.getUserBaseInfoByUuid(split[1]);
                                if(userVo != null) {
                                    Map<String, String> userMap = new HashMap<>();
                                    userMap.put("initType", GroupSearch.USER.getValue());
                                    userMap.put("uuid", userVo.getUuid());
                                    userMap.put("name", userVo.getUserName());
                                    workerList.add(userMap);
                                }
                            }else if(GroupSearch.TEAM.getValue().equals(split[0])) {
                                TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
                                if(teamVo != null) {
                                    Map<String, String> teamMap = new HashMap<>();
                                    teamMap.put("initType", GroupSearch.TEAM.getValue());
                                    teamMap.put("uuid", teamVo.getUuid());
                                    teamMap.put("name", teamVo.getName());
                                    workerList.add(teamMap);
                                }
                            }else if(GroupSearch.ROLE.getValue().equals(split[0])) {
                                RoleVo roleVo = roleMapper.getRoleByUuid(split[1]);
                                if(roleVo != null) {
                                    Map<String, String> roleMap = new HashMap<>();
                                    roleMap.put("initType", GroupSearch.ROLE.getValue());
                                    roleMap.put("uuid", roleVo.getUuid());
                                    roleMap.put("name", roleVo.getName());
                                    workerList.add(roleMap);
                                }
                            }
                        }
                        assignWorker.put("workerList", workerList);
                        assignWorkerList.add(assignWorker);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(assignWorkerList)){
            return JSON.toJSONString(assignWorkerList);
        }
        return null;
    }
}
