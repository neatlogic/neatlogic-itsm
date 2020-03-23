package codedriver.framework.process.workcenter.elasticsearch.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPatch;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.process.workcenter.elasticsearch.core.WorkcenterEsHandlerBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.CatalogVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class WorkcenterUpdateHandler extends WorkcenterEsHandlerBase {
	Logger logger = LoggerFactory.getLogger(WorkcenterUpdateHandler.class);
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
	ProcessTaskAuditMapper processTaskAuditMapper;
	
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
						break TO;
					}
				}
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
		 getObjectPool().checkout(tenantUuid, null);
		 MultiAttrsObjectPatch patch = getObjectPool().save(taskId.toString());
		 /** 获取工单信息 **/
		 ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(taskId);
		 if(processTaskVo != null) {
			 /** 获取服务信息 **/
			 ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
			 /** 获取服务目录信息 **/
			 CatalogVo catalog = catalogMapper.getCatalogByUuid(channel.getParentUuid());
			 /** 获取开始节点内容信息 **/
			 ProcessTaskContentVo startContentVo = null;
			 List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(taskId, ProcessStepType.START.getValue());
			 if (stepList.size() == 1) {
				ProcessTaskStepVo startStepVo = stepList.get(0);
				List<ProcessTaskStepContentVo> contentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startStepVo.getId());
				if (contentList.size() > 0) {
					ProcessTaskStepContentVo contentVo = contentList.get(0);
					startContentVo = processTaskMapper.getProcessTaskContentByHash(contentVo.getContentHash());
				}
			 }
			 /** 获取转交记录 **/
			 List<ProcessTaskStepAuditVo> transferAuditList = processTaskAuditMapper.getProcessTaskAuditList(new ProcessTaskStepAuditVo(processTaskVo.getId(),ProcessTaskStepAction.TRANSFER.getValue()));
			 List<String> transferUserIdList = new ArrayList<String>();
			 for(ProcessTaskStepAuditVo auditVo : transferAuditList) {
				 transferUserIdList.add(auditVo.getUserId());
			 }
			 /** 获取工单当前步骤 **/
			 List<ProcessTaskStepVo>  processTaskActiveStepList = processTaskMapper.getProcessTaskActiveStepByProcessTaskId(taskId);
			 List<String> currentStepIdList = new ArrayList<String>();
			 JSONObject currentStepWorkerJson = new JSONObject();
			 JSONObject currentStepStausJson = new JSONObject();
			 for(ProcessTaskStepVo step : processTaskActiveStepList) {
				 currentStepIdList.add(step.getId().toString());
				 JSONArray currentStepWorkerArrayTmp = new JSONArray();
				 for(ProcessTaskStepWorkerVo worker : step.getWorkerList()) {
					 currentStepWorkerArrayTmp.add(worker.getWorkList());
				 }
				 currentStepWorkerJson.put(step.getId().toString(), currentStepWorkerArrayTmp);
				 currentStepStausJson.put(step.getId().toString(),step.getStatus());
			 }
			 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			 //标题
			 patch.set("title", processTaskVo.getTitle());
			 //工单状态
			 patch.set("status", processTaskVo.getStatus());
			 //优先级
			 patch.set("priority", processTaskVo.getPriorityUuid());
			 //服务目录
			 patch.set("catalog", catalog.getUuid());
			 //服务类型
			 patch.set("channelType",channel.getType());
			 //服务
			 patch.set("channel", channel.getUuid());
			 //上报内容
			 patch.set("content", startContentVo == null?"":startContentVo.getContent());
			 //工单开始时间
			 patch.set("createTime", sdf.format(processTaskVo.getStartTime()));
			 //工单结束时间
			 patch.set("endTime", processTaskVo.getEndTime() == null?null:sdf.format(processTaskVo.getEndTime()));
			 //上报人
			 patch.set("owner",processTaskVo.getOwner());
			 //代报人
			 patch.set("reporter", processTaskVo.getReporter());
			 //转交人
			 patch.setStrings("transferFromUsers", transferUserIdList);
			 //当前步骤idList
			 patch.setStrings("currentStep", currentStepIdList);
			 //当前步骤处理人List
			 patch.set("currentStepUser",  JSONObject.toJSONString(currentStepWorkerJson));
			 //当前步骤状态
			 patch.set("currentStepStatus", JSONObject.toJSONString(currentStepStausJson));
			 //时间窗口
			 patch.set("worktime", channel.getWorktimeUuid());
			 //超时时间
			 patch.set("expiredTime", processTaskVo.getExpireTime() == null?null:sdf.format(processTaskVo.getExpireTime()));
			 //表单属性
			 List<ProcessTaskFormAttributeDataVo> formAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(taskId);
			 for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
				 patch.set(attributeData.getAttributeUuid(), attributeData.getData());
			 }
			 patch.commit();
		 }else {
			 ElasticSearchPoolManager.getObjectPool(WorkcenterEsHandlerBase.POOL_NAME).delete(taskId.toString());
		 }
	}

}
