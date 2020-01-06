package codedriver.module.process.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.ReturnJson;
import codedriver.framework.common.config.Config;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.dto.FormVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.module.process.service.test.ProcessTaskService1;

@Controller
@RequestMapping("/processtask")
public class ProcessTaskController {
	Logger logger = LoggerFactory.getLogger(ProcessTaskController.class);

	@Autowired
	private ProcessService processService;
	@Autowired
	private ProcessTaskService processTaskService;
	@Autowired
	private ProcessTaskService1 processTaskService1;

	@RequestMapping(value = "/step/{stepId}/formattribute/{attributeUuid}")
	@ResponseBody
	public JSONObject getProcessTaskStepFormAttribute(@PathVariable("stepId") Long stepId, @PathVariable("attributeUuid") String attributeUuid) {
		List<ProcessTaskStepFormAttributeVo> attributeList = processTaskService.getProcessTaskStepFormAttributeByStepId(new ProcessTaskStepFormAttributeVo(stepId, attributeUuid));
		JSONObject returnObj = new JSONObject();
		if (attributeList.size() == 1) {
			ProcessTaskStepFormAttributeVo attributeVo = attributeList.get(0);
			returnObj.put("label", attributeVo.getLabel());
			returnObj.put("attributeUuid", attributeVo.getAttributeUuid());
			returnObj.put("typeName", attributeVo.getTypeName());
			returnObj.put("handler", attributeVo.getHandler());
			returnObj.put("handlerName", attributeVo.getHandlerName());
			returnObj.put("config", attributeVo.getConfig());
			returnObj.put("data", attributeVo.getData());
			returnObj.put("isEditable", attributeVo.getIsEditable());
		}
		return returnObj;
	}

	@RequestMapping(value = "/{processTaskId}/getform")
	@ResponseBody
	public FormVo getProcessTaskFormByProcessTaskId(@PathVariable("processTaskId") Long processTaskId) {
		ProcessTaskFormVo form = processTaskService.getProcessTaskFormByProcessTaskId(processTaskId);
		JSONObject returnObj = new JSONObject();
		FormVo formVo = new FormVo();
		// 为了和创建流程接口数据一致
		if (form != null) {
			formVo.setContent(form.getFormContent());
		}
		return formVo;
	}

	@RequestMapping(value = "/runProcess.do")
	public String addProcessTask(String uuid, HttpServletRequest request, HttpServletResponse response) {
		return "/processtask/runProcess";
	}

	@RequestMapping(value = "/handleStep.do")
	public String handleProcessTaskStep(Long id, HttpServletRequest request, HttpServletResponse response) {
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepBaseInfoById(id);
		if (processTaskStepVo != null) {
			request.setAttribute("processTaskStepVo", processTaskStepVo);
		} else {
			throw new RuntimeException("找不到流程步骤");
		}
		return "/processtask/handleStep";
	}

	@RequestMapping(value = "/{uuid}/startnewtask")
	public void startNewProcessTask(@PathVariable("uuid") String uuid, @RequestBody JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) {
		try {
			Map<String, String[]> paramMap = request.getParameterMap();
			ProcessStepVo startStepVo = processService.getProcessStartStep(uuid);
			JSONObject result = new JSONObject();
			if (startStepVo != null) {
				ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo(startStepVo);
				// startTaskStep.setParamMap(request.getParameterMap());
				startTaskStep.setParamObj(paramObj);
				IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startStepVo.getHandler());
				if (handler != null) {
					handler.startProcess(startTaskStep);
				}
				result.put("processTaskId", startTaskStep.getProcessTaskId());
			}
			
			
			ReturnJson.success(result,response);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ReturnJson.error(ex.getMessage(), response);
		}
	}

	@RequestMapping(value = "/processtaskstep/{id}/complete")
	public void complete(@PathVariable("id") Long processTaskStepId, @RequestBody JSONObject paramObj, HttpServletResponse response, HttpServletRequest request) {
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepBaseInfoById(processTaskStepId);
		if (processTaskStepVo != null) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
			if (handler != null) {
				processTaskStepVo.setParamObj(paramObj);
				handler.complete(processTaskStepVo);
			}
		} else {
			throw new RuntimeException("流程步骤不存在");
		}

	}
	
	@RequestMapping(value = "/processtaskstep/{id}/start")
	public void start(@PathVariable("id") Long processTaskStepId, @RequestBody JSONObject paramObj, HttpServletResponse response, HttpServletRequest request) {
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepBaseInfoById(processTaskStepId);
		if (processTaskStepVo != null) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
			if (handler != null) {
				processTaskStepVo.setParamObj(paramObj);
				handler.start(processTaskStepVo);
			}
		} else {
			throw new RuntimeException("流程步骤不存在");
		}

	}
	
	@RequestMapping(value = "/processtaskstep/{id}/accept")
	public void accept(@PathVariable("id") Long processTaskStepId, @RequestBody JSONObject paramObj, HttpServletResponse response, HttpServletRequest request) {
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepBaseInfoById(processTaskStepId);
		if (processTaskStepVo != null) {
			IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
			if (handler != null) {
				processTaskStepVo.setParamObj(paramObj);
				handler.accept(processTaskStepVo);
			}
		} else {
			throw new RuntimeException("流程步骤不存在");
		}

	}

	@RequestMapping(value = "/flowjobconfig/{flowJobId}")
	public void getFlowJobConfigById(@PathVariable("flowJobId") Long processTaskId, HttpServletResponse response) throws IOException {
		ProcessTaskVo flowJobVo = processTaskService1.getProcessTaskBaseInfoById(processTaskId);
		response.setContentType(Config.RESPONSE_TYPE_JSON);
		if (flowJobVo.getConfig() != null && !flowJobVo.getConfig().equals("")) {
			response.getWriter().print(flowJobVo.getConfig());
		} else {
			response.getWriter().print("{}");
		}
	}
	
	@RequestMapping(value = "/processTaskPic.do")
	public String processTaskPic(String processTaskId, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("processTaskId", processTaskId);
		return "/processtask/processTaskPicture";
	}
	
	@RequestMapping(value = "/{processTaskId}/stepstatus")
	public void getFlowJobStepStatus(@PathVariable("processTaskId") Long processTaskId, HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<ProcessTaskStepVo> stepList = processTaskService1.getProcessTaskStepStatusByFlowJobId(processTaskId);
		//ProcessTaskVo processTaskVo = processTaskService.getProcessTaskBaseInfoById(processTaskId);
		JSONArray stepObjList = new JSONArray();
		JSONArray relObjList = new JSONArray();
		JSONObject returnObj = new JSONObject();

		for (ProcessTaskStepVo stepVo : stepList) {
			if (!stepVo.getType().equals(ProcessStepHandler.START.getType()) && !stepVo.getType().equals(ProcessStepHandler.END.getType())) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", stepVo.getId());
				jsonObj.put("name", stepVo.getName());
				jsonObj.put("uid", stepVo.getProcessStepUuid());
				jsonObj.put("type", stepVo.getType());
				jsonObj.put("status", stepVo.getStatus());
				jsonObj.put("startTime", stepVo.getStartTime());
				jsonObj.put("endTime", stepVo.getEndTime());
				jsonObj.put("isActive", stepVo.getIsActive());
				jsonObj.put("hasRunRole", true);
				stepObjList.add(jsonObj);
			}
		}
		returnObj.put("stepList", stepObjList);
		List<ProcessTaskStepRelVo> relList = processTaskService1.getProcessTaskStepRelByProcessTaskId(processTaskId);
		for (ProcessTaskStepRelVo relVo : relList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("uid", relVo.getProcessStepRelUuid());
			jsonObj.put("status", relVo.getIsHit());
			relObjList.add(jsonObj);
		}
		returnObj.put("relList", relObjList);
		response.setContentType(Config.RESPONSE_TYPE_JSON);
		response.getWriter().print(returnObj);
	}
	
}
