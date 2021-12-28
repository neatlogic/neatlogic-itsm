package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.ProcessTaskSearchVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.util.TableResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskListForRelationApi extends PrivateApiComponentBase {

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
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
