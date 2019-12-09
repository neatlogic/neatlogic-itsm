package codedriver.module.process.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;

import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.service.ProcessTaskAttributeService;

//@Controller
@RequestMapping("/taskattribute")
public class ProcessTaskAttributeController {

	@Autowired
	private ProcessTaskAttributeService processTaskAttributeService;

	@RequestMapping(value = "/{taskId}/{stepId}/listattribute")
	public void getAttributeByTaskStepId(@PathVariable("processTaskId") Long processTaskId, @PathVariable("processTaskStepId") Long processTaskStepId, HttpServletRequest request, HttpServletResponse response) {
		List<ProcessTaskStepAttributeVo> attributeList = processTaskAttributeService.getProcessTaskStepAttributeListByProcessTaskStepId(processTaskId, processTaskStepId);
		if (attributeList != null && attributeList.size() > 0) {
			JSONArray returnList = new JSONArray();
			for (ProcessTaskStepAttributeVo attribute : attributeList) {
				
			}
		}
	}

}
