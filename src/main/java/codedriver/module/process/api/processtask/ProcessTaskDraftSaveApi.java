package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.core.IFullTextIndexHandler;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskPriorityNotMatchException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.fulltextindex.FullTextIndexType;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskDraftSaveApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private CatalogService catalogService;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/draft/save";
    }

    @Override
    public String getName() {
        return "工单上报暂存接口";
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
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid"),
            @Param(name = "title", type = ApiParamType.STRING, maxLength = 80, desc = "标题"),
            @Param(name = "owner", type = ApiParamType.STRING, desc = "请求人"),
            @Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "隐藏表单属性列表"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "只读表单属性列表"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "附件id列表"),
            @Param(name = "tagList", type = ApiParamType.JSONARRAY, desc = "标签列表"),
            @Param(name = "focusUserUuidList", type = ApiParamType.JSONARRAY, desc = "工单关注人列表"),
            @Param(name = "handlerStepInfo", type = ApiParamType.JSONOBJECT, desc = "处理器特有的步骤信息"),
            @Param(name = "fromProcessTaskId", type = ApiParamType.LONG, desc = "来源工单id，从转报进入上报页时，传fromProcessTaskId"),
            @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "关系类型id，从转报进入上报页时，传channelTypeRelationId"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "来源")
    })
    @Output({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id")
    })
    @Description(desc = "工单上报暂存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String channelUuid = jsonObj.getString("channelUuid");
        if (channelMapper.checkChannelIsExists(channelUuid) == 0) {
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        /**
         * 由于批量上报是暂存与提交一并完成，
         * 如果不校验优先级，那么会出现批量上报记录显示上报失败，
         * 而实际上已经生成工单，只是状态是草稿
         */
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
        if (!channelPriorityList.stream().anyMatch(o -> o.getPriorityUuid().equals(jsonObj.getString("priorityUuid")))) {
            throw new ProcessTaskPriorityNotMatchException();
        }
        String owner = jsonObj.getString("owner");
        if (StringUtils.isNotBlank(owner) && owner.contains("#")) {
            owner = owner.split("#")[1];
            jsonObj.put("owner", owner);
        }
        ProcessTaskStepVo startProcessTaskStepVo = null;

        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskId != null) {
            processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
            startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
        } else {
            startProcessTaskStepVo = new ProcessTaskStepVo();
            startProcessTaskStepVo.setProcessUuid(processUuid);
            /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
            if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
                String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
                if (StringUtils.isNotBlank(agentUuid)) {
                    if (!catalogService.channelIsAuthority(channelUuid, agentUuid)) {
                        throw new PermissionDeniedException();
                    }
                } else {
                    throw new PermissionDeniedException();
                }
            }
            ProcessStepVo startProcessStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
            startProcessTaskStepVo.setHandler(startProcessStepVo.getHandler());
        }

        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setData(jsonObj.toJSONString());

        startProcessTaskStepVo.setParamObj(jsonObj);
        handler.saveDraft(startProcessTaskStepVo);
        processTaskStepDataVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
        processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTaskId", startProcessTaskStepVo.getProcessTaskId());
        resultObj.put("processTaskStepId", startProcessTaskStepVo.getId());

        //创建全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getComponent(FullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
        }
        IFullTextIndexHandler indexFormHandler = FullTextIndexHandlerFactory.getComponent(FullTextIndexType.PROCESSTASK_FORM);
        if (indexFormHandler != null) {
            indexFormHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
        }
        return resultObj;
    }

}
