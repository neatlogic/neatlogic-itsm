package codedriver.module.process.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.dto.AttributeVo;
import codedriver.framework.common.ReturnJson;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;
import codedriver.module.process.service.AttributeService;
import codedriver.module.process.service.FormService;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessStepHandlerService;

//@Controller("ProcessFormController")
@RequestMapping("/form")
public class FormController {
	Logger logger = LoggerFactory.getLogger(FormController.class);

	@Autowired
	private ProcessService processService;

	@Autowired
	private ProcessStepHandlerService processHandlerService;

	@Autowired
	private AttributeService attributeService;

	@Autowired
	private FormService formService;
	

	@RequestMapping(value = "/{formUuid}/listattribute")
	@ResponseBody
	public JSONObject getAttributeByFormUuid(@PathVariable("formUuid") String formUuid, HttpServletRequest request, HttpServletResponse response) {
		List<AttributeVo> attributeList = formService.getAttributeByFormUuid(formUuid);
		JSONObject returnObj = new JSONObject();
		JSONArray jsonList = new JSONArray();
		for (AttributeVo attributeVo : attributeList) {
			JSONObject attrObj = new JSONObject();
			attrObj.put("label", attributeVo.getLabel());
			attrObj.put("uuid", attributeVo.getUuid());
			attrObj.put("handler", attributeVo.getHandler());
			attrObj.put("handlerName", attributeVo.getHandlerName());
			attrObj.put("type", attributeVo.getType());
			attrObj.put("typeName", attributeVo.getTypeName());
			jsonList.add(attrObj);
		}
		returnObj.put("attributeList", jsonList);
		return returnObj;
	}

	@RequestMapping(value = "/editForm.do")
	public String editFrom(String uuid, HttpServletRequest request, HttpServletResponse response) {
		FormVo formVo = formService.getFormDetailByUuid(uuid);
		List<AttributeVo> attributeList = attributeService.searchAttribute(new AttributeVo());
		request.setAttribute("attributeList", attributeList);
		request.setAttribute("formVo", formVo);
		return "/form/editForm";
	}

	@RequestMapping(value = "/version/{uuid}")
	@ResponseBody
	public FormVersionVo getFormVersionByUuid(@PathVariable("uuid") String uuid, HttpServletResponse response, HttpServletRequest request) {
		return formService.getFormVersionByUuid(uuid);
	}

	@RequestMapping(value = "/save")
	public void saveForm(FormVo formVo, HttpServletRequest request, HttpServletResponse response) {
		try {
			formService.saveForm(formVo);
			ReturnJson.success(response);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			ReturnJson.error(ex.getMessage(), response);
		}
	}

	@RequestMapping(value = "/searchactiveform")
	@ResponseBody
	public List<FormVo> searchActiveForm(FormVo formVo, HttpServletRequest request, HttpServletResponse response) {
		formVo.setIsActive(1);
		return formService.searchForm(formVo);
	}

	@RequestMapping(value = "/{uuid}")
	@ResponseBody
	public FormVo getFormByUuid(@PathVariable("uuid") String uuid) {
		FormVo formVo = formService.getFormDetailByUuid(uuid);
		if (formVo.getVersionList() != null && formVo.getVersionList().size() > 0) {
			for (FormVersionVo version : formVo.getVersionList()) {
				if (version.getIsActive().equals(1)) {
					formVo.setContent(version.getContent());
				}
			}
			formVo.setVersionList(null);
		}
		return formVo;
	}
}
