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

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.ReturnJson;
import codedriver.framework.common.config.Config;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.dto.FormVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessTaskService;

//@Controller
@RequestMapping("/processtask")
public class ProcessTaskController {
	Logger logger = LoggerFactory.getLogger(ProcessTaskController.class);

	@Autowired
	private ProcessService processService;
	@Autowired
	private ProcessTaskService processTaskService;

	@RequestMapping(value = "/step/{stepId}/formattribute/{attributeUuid}")
	@ResponseBody
	public JSONObject getProcessTaskStepFormAttribute(@PathVariable("stepId") Long stepId, @PathVariable("attributeUuid") String attributeUuid) {
		List<ProcessTaskStepFormAttributeVo> attributeList = processTaskService.getProcessTaskStepFormAttributeByStepId(new ProcessTaskStepFormAttributeVo(stepId, attributeUuid));
		JSONObject returnObj = new JSONObject();
		if (attributeList.size() == 1) {
			ProcessTaskStepFormAttributeVo attributeVo = attributeList.get(0);
			returnObj.put("label", attributeVo.getLabel());
			returnObj.put("inputPage", attributeVo.getInputPage());
			returnObj.put("configPage", attributeVo.getConfigPage());
			returnObj.put("viewPage", attributeVo.getViewPage());
			returnObj.put("attributeUuid", attributeVo.getAttributeUuid());
			returnObj.put("description", attributeVo.getDescription());
			returnObj.put("dataCubeTextField", attributeVo.getDataCubeTextField());
			returnObj.put("dataCubeValueField", attributeVo.getDataCubeValueField());
			returnObj.put("dataCubeUuid", attributeVo.getDataCubeUuid());
			returnObj.put("typeName", attributeVo.getTypeName());
			returnObj.put("handler", attributeVo.getHandler());
			returnObj.put("handlerName", attributeVo.getHandlerName());
			returnObj.put("config", attributeVo.getConfig());
			returnObj.put("data", attributeVo.getData());
			returnObj.put("isEditable", attributeVo.getIsEditable());
			returnObj.put("editTemplate", attributeVo.getEditTemplate());
			returnObj.put("viewTemplate", attributeVo.getViewTemplate());
			returnObj.put("configTemplate", attributeVo.getConfigTemplate());
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

	@RequestMapping(value = "/processtaskstep/{id}")
	public void getProcessTaskStepById(@PathVariable("id") Long processTaskStepId, HttpServletResponse response) throws IOException {
		ProcessTaskStepVo processTaskStepVo = processTaskService.getProcessTaskStepDetailById(processTaskStepId);
		//JSONObject jsonObj = JSONObject.parseObject(processTaskStepVo);
		//response.setContentType(Config.RESPONSE_TYPE_JAVASCRIPT);
		//response.getWriter().print(jsonObj.toString());
	}

	@RequestMapping(value = "/{uuid}/startnewtask")
	public void startNewProcessTask(@PathVariable("uuid") String uuid, @RequestBody JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) {
		try {
			ProcessTaskVo processTaskVo = new ProcessTaskVo();
			Map<String, String[]> paramMap = request.getParameterMap();
			ProcessStepVo startStepVo = processService.getProcessStartStep(uuid);
			if (startStepVo != null) {
				ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo(startStepVo);
				// startTaskStep.setParamMap(request.getParameterMap());
				startTaskStep.setParamObj(paramObj);
				IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startStepVo.getHandler());
				if (handler != null) {
					handler.init(startTaskStep);
				}
			}
			ReturnJson.success(response);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ReturnJson.error(ex.getMessage(), response);
		}
	}

	@RequestMapping(value = "/processtaskstep/{processTaskStepId}/attribute/{uuid}")
	@ResponseBody
	public ProcessTaskStepAttributeVo getProcessTaskStepAttributeByUuid(@PathVariable("processTaskStepId") Long processTaskStepId, @PathVariable("uuid") String uuid) {
		/** 为了和流程执行时一致，创建流程时同样返回ProcessTaskStepVo类型 **/
		ProcessTaskStepAttributeVo processTaskStepAttributeVo = new ProcessTaskStepAttributeVo();
		processTaskStepAttributeVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepAttributeVo.setAttributeUuid(uuid);
		List<ProcessTaskStepAttributeVo> attributeList = processTaskService.getProcessTaskStepAttributeByStepId(processTaskStepAttributeVo);
		if (attributeList != null && attributeList.size() > 0) {
			return attributeList.get(0);
		} else {
			return null;
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

}
