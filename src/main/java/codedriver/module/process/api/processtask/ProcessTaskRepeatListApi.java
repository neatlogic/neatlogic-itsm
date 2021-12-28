/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
