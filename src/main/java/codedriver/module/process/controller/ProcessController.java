package codedriver.module.process.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.ReturnJson;
import codedriver.framework.common.config.Config;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherFactory;
import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepHandlerVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessVo;
import codedriver.module.process.dto.WorkerDispatcherVo;
import codedriver.module.process.service.ProcessService;
import codedriver.module.process.service.ProcessTaskService;

@Controller
@RequestMapping("/process")
public class ProcessController {
	Logger logger = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private ProcessService processService;

	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private TeamMapper teamMapper;


	@RequestMapping(value = "/step/{stepUuid}/formattribute/{attributeUuid}")
	@ResponseBody
	public JSONObject getAttributeByUuid(@PathVariable("stepUuid") String stepUuid, @PathVariable("attributeUuid") String attributeUuid) {
		List<ProcessStepFormAttributeVo> attributeList = processService.getProcessStepFormAttributeByStepUuid(new ProcessStepFormAttributeVo(stepUuid, attributeUuid));
		JSONObject returnObj = new JSONObject();
		if (attributeList.size() == 1) {
			ProcessStepFormAttributeVo attributeVo = attributeList.get(0);
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
	
	@RequestMapping("searchuserandteam")
	public void searchUserAndTeam(String k, Integer limit, HttpServletRequest request, HttpServletResponse response) throws IOException {
		UserVo userVo = new UserVo();
		userVo.setUserId(k);
		userVo.setPageSize(limit);
		if (userVo.getNeedPage()) {
			int rowNum = userMapper.searchUserCount(userVo);
			userVo.setPageCount(PageUtil.getPageCount(rowNum, userVo.getPageSize()));
			userVo.setRowNum(rowNum);
		}
		List<UserVo> userList = userMapper.searchUser(userVo);
		JSONArray userObjList = new JSONArray();
		for (UserVo user : userList) {
			JSONObject userObj = new JSONObject();
			userObj.put("text", user.getUserName());
			userObj.put("value", user.getUserId());
			userObj.put("icon", "ts-user");
			userObjList.add(userObj);
		}

		TeamVo teamVo = new TeamVo();
		teamVo.setName(k);
		teamVo.setPageSize(limit);
		List<TeamVo> teamList = teamMapper.searchTeam(teamVo);
		JSONArray teamObjList = new JSONArray();
		for (TeamVo team : teamList) {
			JSONObject teamObj = new JSONObject();
			teamObj.put("value", team.getUuid());
			teamObj.put("text", team.getName());
			teamObj.put("icon", "ts-team");
			teamObjList.add(teamObj);
		}

		JSONObject returnObj = new JSONObject();
		JSONArray returnList = new JSONArray();
		if (userObjList.size() > 0) {
			JSONObject userDataObj = new JSONObject();
			userDataObj.put("text", "用户");
			userDataObj.put("value", "user");
			userDataObj.put("data", userObjList);
			returnList.add(userDataObj);
		}
		if (teamObjList.size() > 0) {
			JSONObject teamDataObj = new JSONObject();
			teamDataObj.put("text", "分组");
			teamDataObj.put("value", "team");
			teamDataObj.put("data", teamObjList);
			returnList.add(teamDataObj);
		}
		returnObj.put("data", returnList);
		response.setContentType(Config.RESPONSE_TYPE_JSON);
		response.getWriter().print(returnObj);
	}

}
