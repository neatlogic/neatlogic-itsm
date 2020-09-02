package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.dto.score.ProcesstaskScoreVo;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskScoreApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ProcessTaskService processTaskService;

	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;

	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "processtask/score";
	}

	@Override
	public String getName() {
		return "工单评分接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "scoreTemplateId", type = ApiParamType.LONG, isRequired = true, desc = "评分模版ID"),
		@Param(name = "scoreTemplateName", type = ApiParamType.STRING, isRequired = true, desc = "评分模版名称"),
		@Param(name = "scoreDimensionList", type = ApiParamType.JSONARRAY, isRequired = true,
				desc = "评分维度及分数，格式[{\"dimensionId\":133018403841111,\"dimensionName\":\"dim\",\"description\":\"see\",\"score\":3}]"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "评价内容")
	})
	@Output({

	})
	@Description(desc = "工单评分接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long scoreTemplateId = jsonObj.getLong("scoreTemplateId");
        String scoreTemplateName = jsonObj.getString("scoreTemplateName");
        JSONArray scoreDimensionList = jsonObj.getJSONArray("scoreDimensionList");
        String content = jsonObj.getString("content");

        Map<Long,Integer> dimensionIdScoreMap = new HashMap<>();
        for(Object o : scoreDimensionList){
            JSONObject jsonObject = JSONObject.parseObject(o.toString());
            Long dimensionId = jsonObject.getLong("dimensionId");
            Integer score = jsonObject.getInteger("score");
            dimensionIdScoreMap.put(dimensionId,score);
        }

        ProcesstaskScoreVo processtaskScoreVo = new ProcesstaskScoreVo();
        processtaskScoreVo.setProcesstaskId(processTaskId);
        processtaskScoreVo.setScoreTemplateId(scoreTemplateId);



//        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
//        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		processTaskMapper.getProcessTaskLockById(processTaskId);


//		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		// TODO 鉴权，只有上报人才可评分
//		handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.COMMENT, true);

        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskId);

        processTaskStepVo.setParamObj(jsonObj);
        handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.SCORE);

		JSONObject resultObj = new JSONObject();
//		resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId));
		return resultObj;
	}

}
