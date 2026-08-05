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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskSearchVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2021/9/14 11:44
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskListForRepeatApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/list/forrepeat";
    }

    @Override
    public String getName() {
        return "nmpap.processtasklistforrepeatapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpap.processtasklistforrepeatapi.input.param.desc.keyword"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtasklistforrepeatapi.input.param.desc.processtaskid"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpap.processtasklistforrepeatapi.input.param.desc.needpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpap.processtasklistforrepeatapi.input.param.desc.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpap.processtasklistforrepeatapi.input.param.desc.currentpage")
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskVo[].class, desc = "nmpap.processtasklistforrepeatapi.output.param.desc.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpap.processtasklistforrepeatapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<Long> processTaskIdList = new ArrayList<>();
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(processTaskId);
        if (repeatGroupId != null) {
            processTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
        } else {
            processTaskIdList.add(processTaskId);
        }
        List<ProcessTaskVo> processTaskList = new ArrayList<>();
        ProcessTaskSearchVo processTaskSearchVo = JSON.toJavaObject(paramObj, ProcessTaskSearchVo.class);
        processTaskSearchVo.setExcludeIdList(processTaskIdList);
        processTaskSearchVo.setIncludeChannelUuid(processTaskVo.getChannelUuid());
        processTaskSearchVo.setExcludeStatus(ProcessTaskStatus.DRAFT.getValue());
        int rowNum = processTaskMapper.getProcessTaskCountByKeywordAndChannelUuidList(processTaskSearchVo);
        if (rowNum > 0) {
            processTaskSearchVo.setRowNum(rowNum);
            if (processTaskSearchVo.getCurrentPage() <= processTaskSearchVo.getPageCount()) {
                processTaskList = processTaskMapper.getProcessTaskListByKeywordAndChannelUuidList(processTaskSearchVo);
                Set<String> userUuidSet = processTaskList.stream().map(ProcessTaskVo::getOwner).collect(Collectors.toSet());
                List<UserVo> userList = userMapper.getUserByUserUuidList(new ArrayList<>(userUuidSet));
                Map<String, UserVo> userMap = userList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
                for (ProcessTaskVo processTask : processTaskList) {
                    UserVo userVo = userMap.get(processTask.getOwner());
                    if (userVo != null) {
                        UserVo ownerVo = new UserVo();
                        BeanUtils.copyProperties(userVo,ownerVo);
                        processTask.setOwnerVo(ownerVo);
                    }
                }
            }
        }
        return TableResultUtil.getResult(processTaskList, processTaskSearchVo);
    }
}
