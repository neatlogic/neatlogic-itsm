//package codedriver.module.process.api.processtask;
//
//import codedriver.framework.asynchronization.threadlocal.UserContext;
//import codedriver.framework.common.constvalue.ApiParamType;
//import codedriver.framework.exception.file.FileNotFoundException;
//import codedriver.framework.file.dao.mapper.FileMapper;
//import codedriver.framework.process.constvalue.ProcessTaskAuditType;
//import codedriver.framework.process.constvalue.ProcessTaskOperationType;
//import codedriver.framework.process.constvalue.ProcessTaskStepAction;
//import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
//import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
//import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
//import codedriver.framework.process.dto.*;
//import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
//import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
//import codedriver.framework.reminder.core.OperationTypeEnum;
//import codedriver.framework.restful.annotation.*;
//import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
//import codedriver.module.process.service.ProcessTaskService;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@Transactional
//@OperationType(type = OperationTypeEnum.CREATE)
//public class ProcessTaskScoreApi extends PrivateApiComponentBase {
//
//	@Autowired
//	private ProcessTaskMapper processTaskMapper;
//
//	@Autowired
//	private ProcessTaskService processTaskService;
//
//	@Autowired
//	private ProcessTaskStepDataMapper processTaskStepDataMapper;
//
//	@Autowired
//	private FileMapper fileMapper;
//
//	@Override
//	public String getToken() {
//		return "processtask/score";
//	}
//
//	@Override
//	public String getName() {
//		return "工单评分接口";
//	}
//
//	@Override
//	public String getConfig() {
//		return null;
//	}
//
//	@Input({
//		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
//		@Param(name = "scoreTemplateId", type = ApiParamType.LONG, isRequired = true, desc = "评分模版ID"),
//		@Param(name = "scoreDimensionList", type = ApiParamType.JSONARRAY, isRequired = true,
//				desc = "评分维度及分数，格式{\"scoreTemplateId\":173018403840000,\"scoreTemplateName\":\"temp\",\"content\":\"sss\",\"dimensionList\":[{\"dimensionId\":133018403841111,\"dimensionName\":\"dim\",\"description\":\"see\",\"score\":3}]}"),
//		@Param(name = "content", type = ApiParamType.STRING, desc = "评价内容")
//	})
//	@Output({
//
//	})
//	@Description(desc = "工单评分接口")
//	@Override
//	public Object myDoService(JSONObject jsonObj) throws Exception {
//		Long processTaskId = jsonObj.getLong("processTaskId");
////        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
////        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
//		processTaskMapper.getProcessTaskLockById(processTaskId);
//
//		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
//		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
//		handler.verifyOperationAuthoriy(processTaskId, processTaskStepId, ProcessTaskOperationType.COMMENT, true);
////		//删除暂存
////		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
////		processTaskStepDataVo.setProcessTaskId(processTaskId);
////		processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
////		processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
////		processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
////		processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
//
//		String content = jsonObj.getString("content");
////        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
////        if(StringUtils.isBlank(content) && CollectionUtils.isEmpty(fileIdList)) {
////            return null;
////        }
//
//        ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo();
//        processTaskStepContentVo.setProcessTaskId(processTaskId);
//        processTaskStepContentVo.setProcessTaskStepId(processTaskStepId);
//        processTaskStepContentVo.setType(ProcessTaskStepAction.COMMENT.getValue());
//        if (StringUtils.isNotBlank(content)) {
//            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
//            processTaskMapper.replaceProcessTaskContent(contentVo);
//            processTaskStepContentVo.setContentHash(contentVo.getHash());
//        }
//        processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
//
//        /** 保存附件uuid **/
////        if(CollectionUtils.isNotEmpty(fileIdList)) {
////            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
////            processTaskStepFileVo.setProcessTaskId(processTaskId);
////            processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
////            processTaskStepFileVo.setContentId(processTaskStepContentVo.getId());
////            for (Long fileId : fileIdList) {
////                if(fileMapper.getFileById(fileId) == null) {
////                    throw new FileNotFoundException(fileId);
////                }
////                processTaskStepFileVo.setFileId(fileId);
////                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
////            }
////        }
//
//        //生成活动
////        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
////        processTaskStepVo.setProcessTaskId(processTaskId);
////        processTaskStepVo.setId(processTaskStepId);
//        processTaskStepVo.setParamObj(jsonObj);
//        handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.COMMENT);
//
//		JSONObject resultObj = new JSONObject();
//		resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId));
//		return resultObj;
//	}
//
//}
