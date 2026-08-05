package neatlogic.module.process.api.processtask;

import java.util.*;
import java.util.stream.Collectors;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.dto.ProcessTaskSearchVo;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskListForRelationApi extends PrivateApiComponentBase {

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getToken() {
        return "processtask/list/forrelation";
    }

    @Override
    public String getName() {
        return "nmpap.processtasklistforrelationapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "nmpap.processtasklistforrelationapi.input.param.desc.keyword"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtasklistforrelationapi.input.param.desc.channeltyperelationid"),
//        @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtasklistforrelationapi.input.param.desc.channeluuid"),
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtasklistforrelationapi.input.param.desc.processtaskid"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpap.processtasklistforrelationapi.input.param.desc.needpage"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpap.processtasklistforrelationapi.input.param.desc.pagesize"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpap.processtasklistforrelationapi.input.param.desc.currentpage")
    })
    @Output({
        @Param(name = "tbodyList", explode = ProcessTaskVo[].class, desc = "nmpap.processtasklistforrelationapi.output.param.desc.tbodylist"),
        @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpap.processtasklistforrelationapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", new ArrayList<>());
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<Long> relatedProcessTaskIdList = processTaskMapper.getRelatedProcessTaskIdListByProcessTaskId(processTaskId);
        relatedProcessTaskIdList.add(processTaskId);
        Long fromProcessTaskId = processTaskMapper.getFromProcessTaskIdByToProcessTaskId(processTaskId);
        if (fromProcessTaskId != null) {
            relatedProcessTaskIdList.add(fromProcessTaskId);
        }
        List<Long> toProcessTaskIdList = processTaskMapper.getToProcessTaskIdListByFromProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(toProcessTaskIdList)) {
            relatedProcessTaskIdList.addAll(toProcessTaskIdList);
        }
        Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
        if(channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
            throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
        }
        List<String> channelRelationTargetChannelUuidList = catalogService.getChannelRelationTargetChannelUuidList(processTaskVo.getChannelUuid(), channelTypeRelationId);

        ProcessTaskSearchVo processTaskSearchVo = JSONObject.toJavaObject(jsonObj, ProcessTaskSearchVo.class);
        List<ProcessTaskVo> processTaskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(channelRelationTargetChannelUuidList)) {
            processTaskSearchVo.setExcludeIdList(relatedProcessTaskIdList);
            processTaskSearchVo.setIncludeChannelUuidList(channelRelationTargetChannelUuidList);
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
        }
        return TableResultUtil.getResult(processTaskList, processTaskSearchVo);
//        BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
//        int pageCount = 0;
//        if(basePageVo.getNeedPage()) {
//            int rowNum = 0;
//            if(CollectionUtils.isNotEmpty(channelRelationTargetChannelUuidList)) {
//                rowNum = processTaskMapper.getProcessTaskCountByKeywordAndChannelUuidList(basePageVo, relatedProcessTaskIdList, channelRelationTargetChannelUuidList);
//            }
//            pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
//            resultObj.put("currentPage", basePageVo.getCurrentPage());
//            resultObj.put("pageSize", basePageVo.getPageSize());
//            resultObj.put("pageCount", pageCount);
//            resultObj.put("rowNum", rowNum);
//        }
//        if(!basePageVo.getNeedPage() || basePageVo.getCurrentPage() <= pageCount) {
//            List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByKeywordAndChannelUuidList(basePageVo, relatedProcessTaskIdList, channelRelationTargetChannelUuidList);
//            resultObj.put("tbodyList", processTaskList);
//        }
//        return resultObj;
    }

}
