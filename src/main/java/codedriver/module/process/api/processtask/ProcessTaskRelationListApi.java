package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRelationListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/relation/list";
    }

    @Override
    public String getName() {
        return "查询关联工单列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
        @Param(name = "processTaskRelationList", explode = ProcessTaskRelationVo[].class, desc = "关联工单列表"),
        @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询关联工单列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
//        ProcessTaskRelationVo processTaskRelationVo = JSON.toJavaObject(jsonObj, ProcessTaskRelationVo.class);
//        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskRelationVo.getProcessTaskId());
//        ProcessStepUtilHandlerFactory.getHandler().verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.POCESSTASKVIEW, true);
//        JSONObject resultObj = new JSONObject();
//        resultObj.put("processTaskRelationList", new ArrayList<>());
//        int pageCount = 0;
//        if(processTaskRelationVo.getNeedPage()) {
//            int rowNum = processTaskMapper.getProcessTaskRelationCount(processTaskRelationVo.getProcessTaskId());
//            pageCount = PageUtil.getPageCount(rowNum, processTaskRelationVo.getPageSize());
//            resultObj.put("currentPage", processTaskRelationVo.getCurrentPage());
//            resultObj.put("pageSize", processTaskRelationVo.getPageSize());
//            resultObj.put("pageCount", pageCount);
//            resultObj.put("rowNum", rowNum);           
//        }
//        if(!processTaskRelationVo.getNeedPage() || processTaskRelationVo.getCurrentPage() <= pageCount) {
//            Map<Long, ProcessTaskRelationVo> processTaskRelationMap = new HashMap<>();
//            Set<Long> processTaskSet = new HashSet<>();
//            Set<Long> channelTypeRelationIdSet = new HashSet<>();
//            List<ProcessTaskRelationVo> processTaskRelationList = processTaskMapper.getProcessTaskRelationList(processTaskRelationVo);            
//            for(ProcessTaskRelationVo processTaskRelation : processTaskRelationList) {
//                processTaskRelationMap.put(processTaskRelation.getProcessTaskId(), processTaskRelation);
//                processTaskSet.add(processTaskRelation.getProcessTaskId());
//                channelTypeRelationIdSet.add(processTaskRelation.getChannelTypeRelationId());
//            }
//        }
//        return resultObj;
        return null;
    }

}
