/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskDraftSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/draft/save";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskdraftsaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    //	"formAttributeDataList": [
//  		{
//  			"attributeUuid": "属性uuid",
//  			"label": "属性名",
//  			"handler": "formselect",
//  			"dataList": [
//  				"value"
//  			]
//  		}
//  	]                			
    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.channeluuid"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, maxLength = 200, desc = "common.title"),
            @Param(name = "owner", type = ApiParamType.STRING, desc = "term.itsm.owner"),
            @Param(name = "reporter", type = ApiParamType.STRING, desc = "term.itsm.reporter", help = "如果当前登录人和owner不一样则需要额外传reporter。没有代报人，则数据库对应reporter直接null"),
            @Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "common.priorityuuid"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "term.itsm.formattributedatalist"),
            @Param(name = "formExtendAttributeDataList", type = ApiParamType.JSONARRAY, desc = "term.itsm.formextendattributedatalist"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.hidecomponentlist"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.readcomponentlist"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "common.content"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "common.fileidlist"),
            @Param(name = "tagList", type = ApiParamType.JSONARRAY, desc = "common.taglist"),
            @Param(name = "focusUserUuidList", type = ApiParamType.JSONARRAY, desc = "term.itsm.focususeruuidlist"),
            @Param(name = "handlerStepInfo", type = ApiParamType.JSONOBJECT, desc = "term.itsm.handlerstepinfo"),
            @Param(name = "fromProcessTaskId", type = ApiParamType.LONG, desc = "term.itsm.fromprocesstaskid", help = "从转报进入上报页时，传fromProcessTaskId"),
            @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid", help = "从转报进入上报页时，传channelTypeRelationId"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
            @Param(name = "parentProcessTaskStepId", type = ApiParamType.LONG, desc = "nmpap.processtaskdraftgetapi.input.param.desc.parentprocesstaskstepid", help = "创建子流程时，传parentProcessTaskStepId"),
            @Param(name = "invoke", type = ApiParamType.STRING, desc = "nmpap.processtaskdraftsaveapi.input.param.desc.invoke", help = "subprocess :子流程")
    })
    @Output({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id")
    })
    @Description(desc = "nmpap.processtaskdraftsaveapi.getname")
    @Override
    @ResubmitInterval(5)
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        String channelUuid = jsonObj.getString("channelUuid");
//        if (channelMapper.checkChannelIsExists(channelUuid) == 0) {
//            throw new ChannelNotFoundException(channelUuid);
//        }
//        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
//        if (processMapper.checkProcessIsExists(processUuid) == 0) {
//            throw new ProcessNotFoundException(processUuid);
//        }
//        /**
//         * 由于批量上报是暂存与提交一并完成，
//         * 如果不校验优先级，那么会出现批量上报记录显示上报失败，
//         * 而实际上已经生成工单，只是状态是草稿
//         */
//        if(StringUtils.isBlank(jsonObj.getString("priorityUuid"))){//如果为空字符串，则为null
//            jsonObj.put("priorityUuid",null);
//        }
//        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
//        if (CollectionUtils.isNotEmpty(channelPriorityList) && channelPriorityList.stream().noneMatch(o -> o.getPriorityUuid().equals(jsonObj.getString("priorityUuid")))) {
//            throw new ProcessTaskPriorityNotMatchException();
//        }
//        String owner = jsonObj.getString("owner");
//        if (StringUtils.isNotBlank(owner) && owner.contains("#")) {
//            owner = owner.split("#")[1];
//            jsonObj.put("owner", owner);
//        }
//        ProcessTaskStepVo startProcessTaskStepVo = null;
//
//        Long processTaskId = jsonObj.getLong("processTaskId");
//        if (processTaskId != null) {
//            processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
//            startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
//        } else {
//            /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
//            if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
//                throw new PermissionDeniedException();
//                /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
////                String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
////                if (StringUtils.isNotBlank(agentUuid)) {
////                    if (!catalogService.channelIsAuthority(channelUuid, agentUuid)) {
////                        throw new PermissionDeniedException();
////                    }
////                } else {
////                    throw new PermissionDeniedException();
////                }
//            }
//            startProcessTaskStepVo = new ProcessTaskStepVo();
//            startProcessTaskStepVo.setProcessUuid(processUuid);
//            ProcessStepVo startProcessStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
//            startProcessTaskStepVo.setHandler(startProcessStepVo.getHandler());
//        }
//
//        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
//        if (handler == null) {
//            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
//        }
//
//        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
//        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
//        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
//
//        startProcessTaskStepVo.getParamObj().putAll(jsonObj);
//        handler.saveDraft(startProcessTaskStepVo);
//
//        processTaskStepDataVo.setData(jsonObj.toJSONString());
//        processTaskStepDataVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
//        processTaskStepDataVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
//        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
//        processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
//        JSONObject resultObj = new JSONObject();
//        resultObj.put("processTaskId", startProcessTaskStepVo.getProcessTaskId());
//        resultObj.put("processTaskStepId", startProcessTaskStepVo.getId());
//
//        //创建全文检索索引
//        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
//        if (indexHandler != null) {
//            indexHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
//        }
        //System.out.println(jsonObj.toJSONString());
        return processTaskService.saveProcessTaskDraft(jsonObj, null);
    }

}
