package codedriver.module.process.workcenter.elasticsearch.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPatch;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAuditMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterFieldBuilder;
import codedriver.framework.process.workcenter.elasticsearch.core.WorkcenterEsHandlerBase;

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
		 getObjectPool().checkout(tenantUuid);
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
			
			 /** 获取工单当前步骤 **/
			 @SuppressWarnings("serial")
			List<ProcessTaskStepVo>  processTaskStepList = processTaskMapper.getProcessTaskActiveStepByProcessTaskIdAndProcessStepType(taskId,new ArrayList<String>() {{add(ProcessStepType.PROCESS.getValue());add(ProcessStepType.START.getValue());}},null);
			 WorkcenterFieldBuilder builder = new WorkcenterFieldBuilder();
			 //form
			 JSONArray formArray = new JSONArray();
			 List<ProcessTaskFormAttributeDataVo> formAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(taskId);
			 for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
				 if(attributeData.getType().equals(ProcessFormHandler.FORMCASCADELIST.getHandler())
							||attributeData.getType().equals(ProcessFormHandler.FORMDIVIDER.getHandler())
							||attributeData.getType().equals(ProcessFormHandler.FORMDYNAMICLIST.getHandler())
							||attributeData.getType().equals(ProcessFormHandler.FORMSTATICLIST.getHandler())){
					 continue;
				 }
				 JSONObject formJson = new JSONObject();
				 formJson.put("key", attributeData.getAttributeUuid());
				 Object dataObj = attributeData.getDataObj();
				 if(dataObj == null) {
					 continue;
				 }
				 formJson.put("value_"+ProcessFormHandler.getDataType(attributeData.getType()),dataObj);
				 formArray.add(formJson);
			 }
			
			 //common
			 JSONObject WorkcenterFieldJson = builder
					.setId(taskId.toString())
					.setTitle(processTaskVo.getTitle())
			 		.setStatus(processTaskVo.getStatus())
			 		.setPriority(processTaskVo.getPriorityUuid())
			 		.setCatalog(catalog.getUuid())
			 		.setChannelType(channel.getChannelTypeUuid())
			 		.setChannel(channel.getUuid())
			 		.setContent(startContentVo)
			 		.setStartTime(processTaskVo.getStartTime())
			 		.setEndTime(processTaskVo.getEndTime())
			 		.setOwner(processTaskVo.getOwner())
			 		.setReporter(processTaskVo.getReporter(),processTaskVo.getOwner())
			 		.setStepList(processTaskStepList)
			 		.setTransferFromUserList(transferAuditList)
			 		.setWorktime(channel.getWorktimeUuid())
			 		.setExpiredTime(processTaskVo.getExpireTime())
			 		.build();
			
			 patch.set("form", formArray);
			 patch.set("common", WorkcenterFieldJson);
			 patch.commit();
		 }else {
			 ElasticSearchPoolManager.getObjectPool(WorkcenterEsHandlerBase.POOL_NAME).delete(taskId.toString());
		 }
	}

}
