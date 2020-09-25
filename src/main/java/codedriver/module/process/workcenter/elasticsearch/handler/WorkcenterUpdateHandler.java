package codedriver.module.process.workcenter.elasticsearch.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.elasticsearch.core.EsHandlerBase;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.WorkcenterService;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPatch;
import com.techsure.multiattrsearch.MultiAttrsObjectPool;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;

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
	public String getHandler() {
		return "processtask-update";
	}

	@Override
	public String getHandlerName() {
		return "更新es工单信息";
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
	public void doService(JSONObject paramJson) {
		 Long taskId = paramJson.getLong("taskId");
		 String tenantUuid = paramJson.getString("tenantUuid");
		 MultiAttrsObjectPool pool = getObjectPool(POOL_NAME,tenantUuid);
		 MultiAttrsObjectPatch patch = pool.save(taskId.toString());
		 /** 获取工单信息 **/
		 ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(taskId);
		 if(processTaskVo != null) {
			 JSONObject esObject = workcenterService.getProcessTaskESObject(processTaskVo);
			 if(MapUtils.isNotEmpty(esObject)){
				 patch.set("form", esObject.getJSONArray("form"));
				 patch.set("common", esObject.getJSONObject("common"));
				 patch.commit();
			 }
		 }else {
			 ElasticSearchPoolManager.getObjectPool(POOL_NAME).delete(taskId.toString());
		 }
	}

}
