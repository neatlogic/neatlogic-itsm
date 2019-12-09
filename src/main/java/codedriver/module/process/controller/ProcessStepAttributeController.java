package codedriver.module.process.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;
import codedriver.framework.attribute.dto.AttributeVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.dto.ProcessStepHandlerVo;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.service.AttributeService;
import codedriver.module.process.service.ProcessService;

//@Controller
@RequestMapping("/processstepattribute")
public class ProcessStepAttributeController {

	@Autowired
	private ProcessService processService;


	@Autowired
	private AttributeService attributeService;

	@RequestMapping(value = "/{uuid}")
	@ResponseBody
	public AttributeVo getAttributeByUuid(@PathVariable("uuid") String uuid) {
		return attributeService.getAttributeByUuid(uuid);
	}

	@RequestMapping(value = "/{uuid}/data")
	@ResponseBody
	public Object getAttributeData(@PathVariable("uuid") String uuid, HttpServletRequest request) {
		AttributeVo attributeVo = attributeService.getAttributeByUuid(uuid);
		if (attributeVo != null && StringUtils.isNotBlank(attributeVo.getHandler())) {
			IAttributeHandler handler = AttributeHandlerFactory.getHandler(attributeVo.getHandler());
			if (handler != null) {
				return handler.getData(attributeVo, request.getParameterMap());
			}
		}
		return null;
	}

	@RequestMapping(value = "editProcess.do")
	public String editProcess(String uuid, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(uuid)) {
			ProcessVo processVo = processService.getProcessByUuid(uuid);
			request.setAttribute("processVo", processVo);
		}
		// List<FlowTypeVo> typeList = flowService.getFlowTypeList();
		List<ProcessStepHandlerVo> componentList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
		request.setAttribute("componentList", componentList);
		// request.setAttribute("typeList", typeList);
		// request.setAttribute("actionTypeList", actionTypeList);
		return "/process/editProcess";
	}

	@RequestMapping(value = "/attributetest")
	public String editAttribute(HttpServletRequest request, HttpServletResponse response) {

		return "/attribute/attributetest";
	}
}
