package codedriver.module.process.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;
import codedriver.framework.attribute.dto.AttributeVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.dto.ProcessStepHandlerVo;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.service.AttributeService;
import codedriver.module.process.service.ProcessService;

//@Controller
@RequestMapping("/attribute")
public class AttributeController {

	@Autowired
	private ProcessService processService;


	@Autowired
	private AttributeService attributeService;

	@RequestMapping(value = "/get/{uuid}")
	@ResponseBody
	public JSONObject getAttributeByUuid(@PathVariable("uuid") String uuid) {
		AttributeVo attributeVo = attributeService.getAttributeByUuid(uuid);
		JSONObject returnObj = new JSONObject();
		returnObj.put("label", attributeVo.getLabel());
		returnObj.put("inputPage", attributeVo.getInputPage());
		returnObj.put("configPage", attributeVo.getConfigPage());
		returnObj.put("viewPage", attributeVo.getViewPage());
		returnObj.put("description", attributeVo.getDescription());
		returnObj.put("attributeUuid", attributeVo.getUuid());
		returnObj.put("dataCubeTextField", attributeVo.getDataCubeTextField());
		returnObj.put("dataCubeValueField", attributeVo.getDataCubeValueField());
		returnObj.put("dataCubeUuid", attributeVo.getDataCubeUuid());
		returnObj.put("typeName", attributeVo.getTypeName());
		returnObj.put("handler", attributeVo.getHandler());
		returnObj.put("handlerName", attributeVo.getHandlerName());
		returnObj.put("config", attributeVo.getConfig());
		returnObj.put("editTemplate", attributeVo.getEditTemplate());
		returnObj.put("configTemplate", attributeVo.getConfigTemplate());
		return returnObj;
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

	@RequestMapping(value = "/search")
	public void searchAttribute(AttributeVo attributeVo, HttpServletRequest request, HttpServletResponse response) throws IOException {
		attributeVo.setIsActive(1);
		List<AttributeVo> attributeList = attributeService.searchAttribute(attributeVo);
		JSONObject returnObj = new JSONObject();
		returnObj.put("attributeList", attributeList);
		returnObj.put("pageSize", attributeVo.getPageSize());
		returnObj.put("pageCount", attributeVo.getPageCount());
		returnObj.put("currentPage", attributeVo.getCurrentPage());
		//response.setContentType(Config.RESPONSE_TYPE_JAVASCRIPT);
		response.getWriter().print(returnObj);
	}

}
