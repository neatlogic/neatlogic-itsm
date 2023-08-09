/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.groupsearch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.constvalue.ProcessTaskGroupSearch;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchGroupVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchOptionVo;
import neatlogic.framework.restful.groupsearch.core.GroupSearchVo;
import neatlogic.framework.restful.groupsearch.core.IGroupSearchHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProcessUserTypeGroupHandler implements IGroupSearchHandler<ValueTextVo> {
    @Override
    public String getName() {
        return ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue();
    }

    @Override
    public String getHeader() {
        return getName() + "#";
    }

    @Resource
    TaskMapper taskMapper;

    @Override
    public List<ValueTextVo> search(GroupSearchVo groupSearchVo) {
//        List<Object> includeList = jsonObj.getJSONArray("includeList");
//        List<Object> excludeList = jsonObj.getJSONArray("excludeList");
//        if (CollectionUtils.isEmpty(includeList)) {
//            includeList = new ArrayList<Object>();
//        }
//        List<String> includeStrList = includeList.stream().map(Object::toString).collect(Collectors.toList());
        List<String> includeStrList = groupSearchVo.getIncludeList();
        if (CollectionUtils.isEmpty(includeStrList)) {
            includeStrList = new ArrayList<>();
        }
        List<String> excludeList = groupSearchVo.getExcludeList();
        List<String> valuelist = new ArrayList<>();
        List<ValueTextVo> userTypeList = new ArrayList<>();
        for (ProcessUserType s : ProcessUserType.values()) {
            if (s.getIsShow() && s.getText().contains(groupSearchVo.getKeyword())) {
                String value = getHeader() + s.getValue();
                if (!valuelist.contains(value)) {
                    valuelist.add(value);
                    userTypeList.add(new ValueTextVo(value, s.getText()));
                }
            }
            if (includeStrList.contains(getHeader() + s.getValue())) {
                if (userTypeList.stream().noneMatch(o -> Objects.equals(o.getValue(), s.getValue()))) {
                    String value = getHeader() + s.getValue();
                    if (!valuelist.contains(value)) {
                        valuelist.add(value);
                        userTypeList.add(new ValueTextVo(value, s.getText()));
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
                        userTypeList.add(new ValueTextVo(value, configVo.getName() + "处理人"));
                    }
                }
            }
        }
        return userTypeList;
    }

    @Override
    public List<ValueTextVo> reload(GroupSearchVo groupSearchVo) {
        List<ValueTextVo> userTypeList = new ArrayList<>();
//        List<String> valueList = jsonObj.getJSONArray("valueList").toJavaList(String.class);
        List<String> valueList = groupSearchVo.getValueList();
        if (CollectionUtils.isNotEmpty(valueList)) {
            for (String value : valueList) {
                if (value.startsWith(getHeader())) {
                    String realValue = value.replace(getHeader(), "");
                    String text = ProcessUserType.getText(realValue);
                    if (StringUtils.isNotBlank(text)) {
                        userTypeList.add(new ValueTextVo(value, text));
                    }
                }
            }
            List<TaskConfigVo> configVoList = taskMapper.getTaskConfigByIdList(JSONArray.parseArray(JSONArray.toJSONString(valueList.stream().map(v -> v.replace(getHeader(), "")).collect(Collectors.toList()))));
            if (CollectionUtils.isNotEmpty(configVoList)) {
                configVoList.forEach(o -> {
                    userTypeList.add(new ValueTextVo(getHeader() + o.getId().toString(), o.getName() + "处理人"));
                });
            }
        }
        return userTypeList;
    }

    @Override
    public GroupSearchGroupVo repack(List<ValueTextVo> userTypeList) {
        GroupSearchGroupVo groupSearchGroupVo = new GroupSearchGroupVo();
        groupSearchGroupVo.setValue("processUserType");
        groupSearchGroupVo.setText("工单干系人");
        groupSearchGroupVo.setSort(getSort());
        List<GroupSearchOptionVo> dataList = new ArrayList<>();
        for (ValueTextVo userType : userTypeList) {
            GroupSearchOptionVo groupSearchOptionVo = new GroupSearchOptionVo();
            groupSearchOptionVo.setValue(userType.getValue().toString());
            groupSearchOptionVo.setText(userType.getText());
            dataList.add(groupSearchOptionVo);
        }
        groupSearchGroupVo.setDataList(dataList);
        return groupSearchGroupVo;
//        JSONObject userTypeObj = new JSONObject();
//        userTypeObj.put("value", "processUserType");
//        userTypeObj.put("text", "工单干系人");
//        userTypeObj.put("sort", getSort());
//        userTypeObj.put("dataList", userTypeList);
//        return userTypeObj;
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
