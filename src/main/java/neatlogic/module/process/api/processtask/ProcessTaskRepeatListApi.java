/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/9/13 18:48
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRepeatListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/repeat/list";
    }

    @Override
    public String getName() {
        return "查询重复工单列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskVo[].class, desc = "重复工单列表")
    })
    @Description(desc = "查询重复工单列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(processTaskId);
        if (repeatGroupId == null) {
            return resultObj;
        }
        List<Long> repeatProcessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
        repeatProcessTaskIdList.remove(processTaskId);
        List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(repeatProcessTaskIdList);
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if (channelVo != null) {
            ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if (channelTypeVo != null) {
                for (ProcessTaskVo processTask : processTaskList) {
                    processTask.setChannelType(channelTypeVo);
                }
            }
        }
        resultObj.put("tbodyList", processTaskList);
        return resultObj;
    }
}
