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

package neatlogic.module.process.groupsearch;

import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchOptionVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchVo;
import neatlogic.framework.restful.groupsearch.core.IGroupSearchHandler;
import neatlogic.framework.util.$;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ProcessUserTypeGroupHandler implements IGroupSearchHandler {
    @Override
    public String getName() {
        return ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue();
    }

    @Override
    public String getLabel() {
        return ProcessTaskGroupSearch.PROCESSUSERTYPE.getText();
    }

    @Override
    public String getHeader() {
        return getName() + "#";
    }

    @Resource
    TaskMapper taskMapper;

    @Override
    public List<GroupSearchOptionVo> search(GroupSearchVo groupSearchVo) {
        List<String> includeStrList = groupSearchVo.getIncludeList();
        if (CollectionUtils.isEmpty(includeStrList)) {
            includeStrList = new ArrayList<>();
        }
        List<String> excludeList = groupSearchVo.getExcludeList();
        List<String> valuelist = new ArrayList<>();
        List<GroupSearchOptionVo> userTypeList = new ArrayList<>();
        for (ProcessUserType s : ProcessUserType.values()) {
            if (s.getIsShow() && (StringUtils.isBlank(groupSearchVo.getKeyword()) || s.getText().contains(groupSearchVo.getKeyword()))) {
                String value = getHeader() + s.getValue();
                if (!valuelist.contains(value)) {
                    valuelist.add(value);
                    GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
                    groupSearchOptionVo.setValue(value);
                    groupSearchOptionVo.setText(s.getText());
                    userTypeList.add(groupSearchOptionVo);
                }
            }
            if (includeStrList.contains(getHeader() + s.getValue())) {
                if (userTypeList.stream().noneMatch(o -> Objects.equals(o.getValue(), s.getValue()))) {
                    String value = getHeader() + s.getValue();
                    if (!valuelist.contains(value)) {
                        valuelist.add(value);
                        GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
                        groupSearchOptionVo.setValue(value);
                        groupSearchOptionVo.setText(s.getText());
                        userTypeList.add(groupSearchOptionVo);
                    }
                }
            }
        }
        //任务
        if (CollectionUtils.isNotEmpty(excludeList) && !excludeList.contains("processUserType#" + ProcessUserType.MINOR.getValue())) {
            TaskConfigVo configParam = new TaskConfigVo();
            configParam.setKeyword(groupSearchVo.getKeyword());
            List<TaskConfigVo> taskConfigVoList = taskMapper.searchTaskConfig(configParam);
            if (CollectionUtils.isNotEmpty(taskConfigVoList)) {
                for (TaskConfigVo configVo : taskConfigVoList) {
                    String value = getHeader() + configVo.getId().toString();
                    if (!valuelist.contains(value)) {
                        valuelist.add(value);
                        GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
                        groupSearchOptionVo.setValue(value);
                        groupSearchOptionVo.setText(configVo.getName() + $.t("common.worker"));
                        userTypeList.add(groupSearchOptionVo);
                    }
                }
            }
        }
        groupSearchVo.setPageSize(userTypeList.size());
        groupSearchVo.setRowNum(userTypeList.size());
        return userTypeList;
    }

    @Override
    public List<GroupSearchOptionVo> reload(GroupSearchVo groupSearchVo) {
        List<GroupSearchOptionVo> userTypeList = new ArrayList<>();
        List<String> valueList = groupSearchVo.getValueList();
        if (CollectionUtils.isNotEmpty(valueList)) {
            for (String value : valueList) {
                if (value.startsWith(getHeader())) {
                    String realValue = value.replace(getHeader(), "");
                    String text = ProcessUserType.getText(realValue);
                    if (StringUtils.isNotBlank(text)) {
                        GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
                        groupSearchOptionVo.setValue(value);
                        groupSearchOptionVo.setText(text);
                        userTypeList.add(groupSearchOptionVo);
                    }
                }
            }
            Set<Long> idSet = new HashSet<>();
            for (String value : valueList) {
                String str = value.replace(getHeader(), "");
                if (StringUtils.isNotBlank(str)) {
                    try {
                        long id = Long.parseLong(str);
                        idSet.add(id);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(idSet)) {
                List<TaskConfigVo> configVoList = taskMapper.getTaskConfigByIdList(new ArrayList<>(idSet));
                if (CollectionUtils.isNotEmpty(configVoList)) {
                    configVoList.forEach(o -> {
                        GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
                        groupSearchOptionVo.setValue(getHeader() + o.getId().toString());
                        groupSearchOptionVo.setText(o.getName() + $.t("common.worker"));
                        userTypeList.add(groupSearchOptionVo);
                    });
                }
            }
        }
        return userTypeList;
    }

    @Override
    public int getSort() {
        return 1;
    }

    @Override
    public Boolean isLimit() {
        return false;
    }
}
