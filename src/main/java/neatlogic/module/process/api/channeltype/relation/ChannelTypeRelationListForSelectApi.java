package neatlogic.module.process.api.channeltype.relation;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationListForSelectApi extends PrivateApiComponentBase {

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "process/channeltype/relation/list/forselect";
    }

    @Override
    public String getName() {
        return "nmpacr.channeltyperelationlistforselectapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "common.keyword"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid"),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "common.isactive", rule = "0,1"),
            @Param(name = "sourceChannelTypeUuid", type = ApiParamType.STRING, xss = true, desc = "term.itsm.sourcechanneltypeuuid"),
            @Param(name = "sourceChannelUuid", type = ApiParamType.STRING, xss = true, desc = "term.itsm.sourcechanneluuid"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
    })
    @Output({
            @Param(name = "list", explode = ValueTextVo[].class, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpacr.channeltyperelationlistforselectapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("list", new ArrayList<>());
        ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
        String sourceChannelTypeUuid = jsonObj.getString("sourceChannelTypeUuid");
        if (StringUtils.isNotBlank(sourceChannelTypeUuid)) {
            channelTypeRelationVo.setUseIdList(true);
            List<Long> channelTypeRelationIdList = channelTypeMapper.getChannelTypeRelationIdListBySourceChannelTypeUuid(sourceChannelTypeUuid);
            channelTypeRelationVo.setIdList(channelTypeRelationIdList);
        }
        String sourceChannelUuid = jsonObj.getString("sourceChannelUuid");
        if (StringUtils.isNotBlank(sourceChannelUuid)) {
            Set<Long> channelTypeRelationIdSet = new HashSet<>();
            channelTypeRelationVo.setUseIdList(true);
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            Long processTaskId = jsonObj.getLong("processTaskId");
            channelTypeRelationIdSet.addAll(getChannelTypeRelationIdList(sourceChannelUuid, processTaskId, authenticationInfoVo));
            /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//            String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(userUuid, "processtask");
//            if(StringUtils.isNotBlank(agentUuid)){
//                AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
//                channelTypeRelationIdSet.addAll(getChannelTypeRelationIdList(sourceChannelUuid, processTaskId, agentAuthenticationInfoVo));
//            }
            channelTypeRelationVo.setIdList(new ArrayList<>(channelTypeRelationIdSet));
        }
        if (!channelTypeRelationVo.isUseIdList() || CollectionUtils.isNotEmpty(channelTypeRelationVo.getIdList())) {
            int pageCount = 0;
            if (channelTypeRelationVo.getNeedPage()) {
                int rowNum = channelTypeMapper.getChannelTypeRelationCountForSelect(channelTypeRelationVo);
                pageCount = PageUtil.getPageCount(rowNum, channelTypeRelationVo.getPageSize());
                resultObj.put("currentPage", channelTypeRelationVo.getCurrentPage());
                resultObj.put("pageSize", channelTypeRelationVo.getPageSize());
                resultObj.put("pageCount", pageCount);
                resultObj.put("rowNum", rowNum);
            }
            if (!channelTypeRelationVo.getNeedPage() || channelTypeRelationVo.getCurrentPage() <= pageCount) {
                List<ValueTextVo> list = channelTypeMapper.getChannelTypeRelationListForSelect(channelTypeRelationVo);
                resultObj.put("list", list);
            }
        }

        return resultObj;
    }

    private List<Long> getChannelTypeRelationIdList(String sourceChannelUuid, Long processTaskId, AuthenticationInfoVo authenticationInfoVo) throws Exception {
        List<String> processUserTypeList = new ArrayList<>();
        String userUuid = authenticationInfoVo.getUserUuid();
        if(processTaskId != null){
            ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
            if(userUuid.equals(processTaskVo.getOwner())){
                processUserTypeList.add(ProcessUserType.OWNER.getValue());
            }
            if(userUuid.equals(processTaskVo.getReporter())){
                processUserTypeList.add(ProcessUserType.REPORTER.getValue());
            }
            List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserList(new ProcessTaskStepUserVo(processTaskId, null, userUuid));
            for(ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList){
                if(processTaskStepUserVo.getUserType().equals(ProcessUserType.MAJOR.getValue())){
                    processUserTypeList.add(ProcessUserType.MAJOR.getValue());
                }else {
                    processUserTypeList.add(ProcessUserType.MINOR.getValue());
                }
            }
            if (processUserTypeList.contains(ProcessUserType.MAJOR.getValue())){
                processUserTypeList.add(ProcessUserType.WORKER.getValue());
            }else {
                if(processTaskMapper.checkIsWorker(processTaskVo.getId(), null, ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0){
                    processUserTypeList.add(ProcessUserType.WORKER.getValue());
                }
            }
        }
        List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(sourceChannelUuid, userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), processUserTypeList);
        return channelTypeRelationIdList;
    }
}
