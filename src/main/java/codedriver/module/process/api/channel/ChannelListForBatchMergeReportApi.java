/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.channel;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linbq
 * @since 2021/8/24 20:23
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelListForBatchMergeReportApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogMapper catalogMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "suzhoubank/channel/list/forbatchmergereport";
    }

    @Override
    public String getName() {
        return "查询服务列表（用于批量上报组件）";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊查询"),
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务uuid"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ChannelVo[].class, desc = "服务列表")
    })
    @Description(desc = "查询服务列表（用于批量上报组件）")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", new ArrayList<>());
        String channelUuid = null;
        Long processTaskId = paramObj.getLong("processTaskId");
        if (processTaskId != null) {
            ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
            channelUuid = processTaskVo.getChannelUuid();
        } else {
            channelUuid = paramObj.getString("channelUuid");
        }
        if (StringUtils.isBlank(channelUuid)) {
            throw new ParamNotExistsException("processTaskId", "channelUuid");
        }
        ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
        if (channelVo == null) {
            return resultObj;
        }
        String userUuid = UserContext.get().getUserUuid(true);
        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
        List<String> processUserTypeList = processTaskService.getProcessUserTypeList(processTaskId, authenticationInfoVo);
        List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(channelUuid, userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), processUserTypeList);
        /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//        if (CollectionUtils.isEmpty(channelTypeRelationIdList)) {
//            String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(userUuid, "processtask");
//            if(StringUtils.isNotBlank(agentUuid)){
//                AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
//                processUserTypeList = processTaskService.getProcessUserTypeList(processTaskId, agentAuthenticationInfoVo);
//                channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(channelUuid, agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), processUserTypeList);
//            }
//        }
        if (CollectionUtils.isEmpty(channelTypeRelationIdList)) {
            return resultObj;
        }
        channelTypeRelationIdList.sort(Long::compareTo);
        Long channelTypeRelationId = channelTypeRelationIdList.get(0);
        ChannelRelationVo channelRelationVo = new ChannelRelationVo();
        channelRelationVo.setSource(channelUuid);
        channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
        List<ChannelRelationVo> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
        if(CollectionUtils.isEmpty(channelRelationTargetList)) {
            return resultObj;
        }
        List<String> targetChannelUuidList = new ArrayList<>();
        List<String> targetCatalogUuidList = new ArrayList<>();
        for(ChannelRelationVo channelRelation : channelRelationTargetList) {
            if("channel".equals(channelRelation.getType())) {
                targetChannelUuidList.add(channelRelation.getTarget());
            }else if("catalog".equals(channelRelation.getType())) {
                targetCatalogUuidList.add(channelRelation.getTarget());
            }
        }
        if(CollectionUtils.isNotEmpty(targetCatalogUuidList)) {
            List<String> channelTypeUuidList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
            if(channelTypeUuidList.contains("all")) {
                channelTypeUuidList.clear();
            }
            List<String> channelUuidList = getChannelUuidListInTheCatalogUuidList(targetCatalogUuidList, channelTypeUuidList);
            if(CollectionUtils.isNotEmpty(channelUuidList)) {
                for(String targetChannelUuid : channelUuidList) {
                    if(!targetChannelUuidList.contains(targetChannelUuid)) {
                        targetChannelUuidList.add(targetChannelUuid);
                    }
                }
            }
        }
        ChannelVo searchVo = JSONObject.toJavaObject(paramObj, ChannelVo.class);
        searchVo.setAuthorizedUuidList(targetChannelUuidList);
        int rowNum = channelMapper.searchChannelCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            List<ChannelVo> channelList = channelMapper.searchChannelList(searchVo);
            resultObj.put("tbodyList", channelList);
        }
        resultObj.put("currentPage", searchVo.getCurrentPage());
        resultObj.put("pageSize", searchVo.getPageSize());
        resultObj.put("pageCount", searchVo.getPageCount());
        resultObj.put("rowNum", searchVo.getRowNum());
        return resultObj;
    }

    private List<String> getChannelUuidListInTheCatalogUuidList(List<String> catalogUuidList, List<String> channelTypeUuidList) {
        if(CollectionUtils.isNotEmpty(catalogUuidList)) {
            List<String> parentUuidList = new ArrayList<>();
            for(String catalogUuid : catalogUuidList) {
                if(!parentUuidList.contains(catalogUuid)) {
                    CatalogVo catalogVo = catalogMapper.getCatalogByUuid(catalogUuid);
                    if(catalogVo != null) {
                        List<String> uuidList = catalogMapper.getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
                        for(String uuid : uuidList) {
                            if(!parentUuidList.contains(uuid)) {
                                parentUuidList.add(uuid);
                            }
                        }
                    }
                }
            }
            return channelTypeMapper.getChannelUuidListByParentUuidListAndChannelTypeUuidList(parentUuidList, channelTypeUuidList);
        }
        return new ArrayList<>();
    }
}
