package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.score.ProcesstaskScoreMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.score.ProcesstaskScoreVo;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskScoreApi extends PrivateApiComponentBase {

	@Autowired
	private ProcesstaskScoreMapper processtaskScoreMapper;

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ProcessTaskService processTaskService;

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
//		@Param(name = "scoreTemplateName", type = ApiParamType.STRING, isRequired = true, desc = "评分模版名称"),
		@Param(name = "scoreDimensionList", type = ApiParamType.JSONARRAY, isRequired = true,
				desc = "评分维度及分数，格式[{\"dimensionId\":133018403841111,\"dimensionName\":\"dim\",\"description\":\"see\",\"score\":3}]"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "评价内容")
	})
	@Output({})
	@Description(desc = "工单评分接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		processTaskMapper.getProcessTaskLockById(processTaskId);
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		//只有上报人才可评分
		handler.verifyOperationAuthoriy(processTaskId, null, ProcessTaskOperationType.SCORE, true);
        Long scoreTemplateId = jsonObj.getLong("scoreTemplateId");
//        String scoreTemplateName = jsonObj.getString("scoreTemplateName");
        JSONArray scoreDimensionList = jsonObj.getJSONArray("scoreDimensionList");
        String content = jsonObj.getString("content");

        Map<Long,Integer> dimensionIdScoreMap = new HashMap<>();
        JSONArray dimensionArray = new JSONArray();
        for(Object o : scoreDimensionList){
            JSONObject jsonObject = JSONObject.parseObject(o.toString());
            Long dimensionId = jsonObject.getLong("dimensionId");
            Integer score = jsonObject.getInteger("score");
			dimensionIdScoreMap.put(dimensionId,score);
			String dimensionName = jsonObject.getString("dimensionName");
			String description = jsonObject.getString("description");

			dimensionIdScoreMap.put(dimensionId,score);

			JSONObject dimensionObj = new JSONObject();
//			dimensionObj.put("dimensionId",dimensionId);
			dimensionObj.put("dimensionName",dimensionName);
			dimensionObj.put("score",score);
			dimensionObj.put("description",description);
			dimensionArray.add(dimensionObj);
        }
		ProcesstaskScoreVo processtaskScoreVo = new ProcesstaskScoreVo();
		processtaskScoreVo.setProcesstaskId(processTaskId);
		processtaskScoreVo.setScoreTemplateId(scoreTemplateId);
		processtaskScoreVo.setFcu(UserContext.get().getUserUuid());
		processtaskScoreVo.setIsAuto(0);
		for(Map.Entry<Long,Integer> entry : dimensionIdScoreMap.entrySet()){
			processtaskScoreVo.setScoreDimensionId(entry.getKey());
			processtaskScoreVo.setScore(entry.getValue());
			processtaskScoreMapper.insertProcesstaskScore(processtaskScoreVo);
		}

//		JSONObject contentObj = new JSONObject();
//		contentObj.put("processTaskId",processTaskId);
//		contentObj.put("scoreTemplateId",scoreTemplateId);
//		contentObj.put("scoreTemplateName",scoreTemplateName);
//		contentObj.put("content",content);
//		contentObj.put("dimensionList",dimensionArray);
		JSONObject paramObj = new JSONObject();
		paramObj.put("content",dimensionArray);


		/**processtask_content表存储了两份数据：
		 * 评价内容content本身和包含了评分模版名称、评分维度与分数等数据组装而成的JSON
		 */
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processtaskScoreVo.setContentHash(contentVo.getHash());
			processtaskScoreMapper.insertProcesstaskScoreContent(processtaskScoreVo);
		}

		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		processTaskStepVo.setParamObj(paramObj);
		/** 生成活动 */
		handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.SCORE);

		return null;
	}

}
