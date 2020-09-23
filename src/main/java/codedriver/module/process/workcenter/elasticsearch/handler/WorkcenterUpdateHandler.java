package codedriver.module.process.workcenter.elasticsearch.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.elasticsearch.core.EsHandlerBase;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.WorkcenterService;

@Service
public class WorkcenterUpdateHandler extends EsHandlerBase {
	Logger logger = LoggerFactory.getLogger(WorkcenterUpdateHandler.class);
	
	public final static String POOL_NAME = "processtask";
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	FormMapper formMapper;
	@Autowired
	ProcessTaskMapper processTaskMapper;
	@Autowired
	ChannelMapper channelMapper;
	@Autowired
	CatalogMapper catalogMapper;
	@Autowired
	WorktimeMapper worktimeMapper;
	@Autowired
	WorkcenterService workcenterService;


	@Override
	public String getDocument() {
		return "processtask";
	}

	@Override
	public String getDocumentName() {
		return "es工单信息";
	}
	
	@Override
    public String getDocumentId() {
        return "taskId";
    }
	
	@Override
	public JSONObject getConfig(List<Object> params) {
		JSONObject paramJson = new JSONObject();
		ListIterator<Object> paramIterator =  params.listIterator();
		Long taskId = null;
		Long taskStepId = null;
		TO: while(paramIterator.hasNext()) {
			Object param = paramIterator.next();
			if(param instanceof ProcessTaskVo) {
				taskId = ((ProcessTaskVo)param).getId();
				break TO;
			}else{
				Method[] ms= param.getClass().getMethods();
				for(Method m : ms) {
					if(m.getName().equals("getProcessTaskId")) {
						try {
							taskId = (Long)param.getClass().getMethod("getProcessTaskId").invoke(param);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							logger.error(e.getMessage(),e);
						}
						if(taskId != null) {
							break TO;
						}
					}
					if(m.getName().equals("getProcessTaskStepId")) {
						try {
							taskStepId = (Long)param.getClass().getMethod("getProcessTaskStepId").invoke(param);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							logger.error(e.getMessage(),e);
						}
					}
				}
			}
		}
		if(taskId == null) {
			ProcessTaskStepVo  processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(taskStepId);
			if(processTaskStepVo != null) {
				taskId = processTaskStepVo.getProcessTaskId();
			}
		}
		paramJson.put("taskId", taskId);
		paramJson.put("tenantUuid", TenantContext.get().getTenantUuid());
		return paramJson;
	}
	
	@Override
	public JSONObject mySave(JSONObject paramJson) {
		 Long taskId = paramJson.getLong("taskId");
		 /** 获取工单信息 **/
		 ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(taskId);
		 JSONObject esObject = null;
		 if(processTaskVo != null) {
			 esObject = workcenterService.getProcessTaskESObject(processTaskVo);
		 }
		 return esObject;
	}
}
