package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codedriver.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationListForSelectApi extends PrivateApiComponentBase {

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "process/channeltype/relation/list/forselect";
    }

    @Override
    public String getName() {
        return "查询服务类型关系列表（下拉框专用）";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关系名称，关键字搜索"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
            @Param(name = "sourceChannelTypeUuid", type = ApiParamType.STRING, xss = true, desc = "来源服务类型uuid"),
            @Param(name = "sourceChannelUuid", type = ApiParamType.STRING, xss = true, desc = "来源服务uuid"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "list", explode = ValueTextVo[].class, desc = "服务类型关系列表"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询服务类型关系列表")
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
            String userUuid = UserContext.get().getUserUuid(true);
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
            List<String> userRoleUuidList = UserContext.get().getRoleUuidList();
            List<String> teamRoleUuidList = roleMapper.getRoleUuidListByTeamUuidList(teamUuidList);
            Set<String> roleUuidSet = new HashSet<>();
            roleUuidSet.addAll(userRoleUuidList);
            roleUuidSet.addAll(teamRoleUuidList);
            List<String> roleUuidList = new ArrayList<>(roleUuidSet);
            Long processTaskId = jsonObj.getLong("processTaskId");
            channelTypeRelationIdSet.addAll(getChannelTypeRelationIdList(sourceChannelUuid, processTaskId, userUuid, teamUuidList, roleUuidList));
            String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(userUuid, "processtask");
            if(StringUtils.isNotBlank(agentUuid)){
                List<String> agentTeamUuidList = teamMapper.getTeamUuidListByUserUuid(agentUuid);
                List<String> agentUserRoleUuidList = roleMapper.getRoleUuidListByUserUuid(agentUuid);
                List<String> agentTeamRoleUuidList = roleMapper.getRoleUuidListByTeamUuidList(agentTeamUuidList);
                Set<String> agentRoleUuidSet = new HashSet<>();
                agentRoleUuidSet.addAll(agentUserRoleUuidList);
                agentRoleUuidSet.addAll(agentTeamRoleUuidList);
                List<String> agentRoleUuidList = new ArrayList<>(roleUuidSet);
                channelTypeRelationIdSet.addAll(getChannelTypeRelationIdList(sourceChannelUuid, processTaskId, agentUuid, agentTeamUuidList, agentRoleUuidList));
            }
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

    public List<Long> getChannelTypeRelationIdList(String sourceChannelUuid, Long processTaskId, String userUuid, List<String> teamUuidList, List<String> roleUuidList) throws Exception {
        List<String> processUserTypeList = new ArrayList<>();
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
                if(processTaskMapper.checkIsWorker(processTaskVo.getId(), null, ProcessUserType.MAJOR.getValue(), userUuid, teamUuidList, roleUuidList) > 0){
                    processUserTypeList.add(ProcessUserType.WORKER.getValue());
                }
            }
        }
        List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(sourceChannelUuid, userUuid, teamUuidList, roleUuidList, processUserTypeList);
        return channelTypeRelationIdList;
    }
}
