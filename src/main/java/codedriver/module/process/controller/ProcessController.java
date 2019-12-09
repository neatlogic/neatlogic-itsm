package codedriver.module.process.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.ReturnJson;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessStepAttributeVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepHandlerVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.dto.WorkerDispatcherVo;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessTaskService;

//@Controller
@RequestMapping("/process")
public class ProcessController {
	Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ProcessService processService;

	@Autowired
	private ProcessTaskService processTaskService;


	@RequestMapping(value = "/step/{stepUuid}/formattribute/{attributeUuid}")
	@ResponseBody
	public JSONObject getAttributeByUuid(@PathVariable("stepUuid") String stepUuid, @PathVariable("attributeUuid") String attributeUuid) {
		List<ProcessStepFormAttributeVo> attributeList = processService.getProcessStepFormAttributeByStepUuid(new ProcessStepFormAttributeVo(stepUuid, attributeUuid));
		JSONObject returnObj = new JSONObject();
		if (attributeList.size() == 1) {
			ProcessStepFormAttributeVo attributeVo = attributeList.get(0);
			returnObj.put("label", attributeVo.getLabel());
			returnObj.put("inputPage", attributeVo.getInputPage());
			returnObj.put("configPage", attributeVo.getConfigPage());
			;
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
			returnObj.put("configTemplate", attributeVo.getConfigTemplate());
			returnObj.put("viewTemplate", attributeVo.getViewTemplate());
		}
		return returnObj;
	}

	@RequestMapping(value = "/getstart/{uuid}")
	@ResponseBody
	public ProcessTaskStepVo getProcessStartStepByUuid(@PathVariable("uuid") String uuid) {
		/** 为了和流程执行时一致，创建流程时同样返回ProcessTaskStepVo类型 **/
		ProcessStepVo startStepVo = processService.getProcessStartStep(uuid);
		if (startStepVo != null) {
			ProcessTaskStepVo returnStep = new ProcessTaskStepVo(startStepVo);
			// 设为激活页面才能进入可处理状态
			returnStep.setIsActive(1);
			return returnStep;
		} else {
			return null;
		}
	}

	@RequestMapping(value = "/processstep/{processStepUuid}/attribute/{attributeUuid}")
	@ResponseBody
	public ProcessStepAttributeVo getProcessStepAttribute(@PathVariable("processStepUuid") String processStepUuid, @PathVariable("attributeUuid") String attributeUuid) {
		List<ProcessStepAttributeVo> attributeList = processService.getProcessStepAttributeByStepUuid(new ProcessStepAttributeVo(processStepUuid, attributeUuid));
		if (attributeList != null && attributeList.size() > 0) {
			return attributeList.get(0);
		} else {
			return null;
		}
	}

	@RequestMapping(value = "editProcess.do")
	public String editProcess(String uuid, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(uuid)) {
			ProcessVo processVo = processService.getProcessByUuid(uuid);
			ProcessFormVo processFormVo = processService.getProcessFormByProcessUuid(uuid);
			if (processFormVo != null) {
				processVo.setFormUuid(processFormVo.getFormUuid());
			}
			request.setAttribute("processVo", processVo);
		}
		// List<FlowTypeVo> typeList = flowService.getFlowTypeList();
		List<ProcessStepHandlerVo> componentList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
		request.setAttribute("componentList", componentList);
		// request.setAttribute("typeList", typeList);
		// request.setAttribute("actionTypeList", actionTypeList);
		return "/process/editProcess";
	}

	@RequestMapping(value = "/workerdispatcher/list")
	@ResponseBody
	public List<WorkerDispatcherVo> getAllActiveWorkerDispatcher() {
		return WorkerDispatcherFactory.getAllActiveWorkerDispatcher();
	}

	@RequestMapping(value = "/save")
	public void saveProcess(ProcessVo processVo, HttpServletRequest request, HttpServletResponse response) {
		try {
			System.out.println(processVo.getConfig());
			processVo.makeupFromConfigObj();
			processService.saveProcess(processVo);
			JSONObject returnData = new JSONObject();
			returnData.put("processId", processVo.getUuid());
			ReturnJson.success(returnData, response);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ReturnJson.error(ex.getMessage(), response);
		}

	}

}
