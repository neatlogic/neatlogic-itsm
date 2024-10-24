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
        return "查询工单列表(关联工单专用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "模糊匹配，支持标题"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id"),        
//        @Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid"),
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
        @Param(name = "tbodyList", explode = ProcessTaskVo[].class, desc = "工单列表"),
        @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询工单列表(关联工单专用)")
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
