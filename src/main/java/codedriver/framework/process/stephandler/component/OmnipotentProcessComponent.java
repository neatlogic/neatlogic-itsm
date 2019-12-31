package codedriver.framework.process.stephandler.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;
import codedriver.framework.process.exception.ProcessTaskAbortException;
import codedriver.framework.process.exception.ProcessTaskException;
import codedriver.framework.process.exception.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.dto.ProcessTaskAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskAttributeValueVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskStepAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
public class OmnipotentProcessComponent extends ProcessStepHandlerBase {

	@Override
	public String getType() {
		return ProcessStepHandler.OMNIPOTENT.getType();
	}

	@Override
	public String getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return ProcessStepHandler.OMNIPOTENT.getName();
	}

	@Override
	public int getSort() {
		return 3;
	}

	@Override
	protected int myActive(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public String getEditPage() {
		return "process.step.handler.omnipotent.edit";
	}

	@Override
	public String getViewPage() {
		return "process.step.handler.omnipotent.view";
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}

	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepVo> nextStepList = new ArrayList<>();
		if (currentProcessTaskStepVo.getRelList() != null && currentProcessTaskStepVo.getRelList().size() > 0) {
			for (ProcessTaskStepRelVo relVo : currentProcessTaskStepVo.getRelList()) {
				if (relVo.getCondition() == null || relVo.getCondition().equals("") || relVo.getCondition().equals("always")) {
					nextStepList.add(new ProcessTaskStepVo() {
						{
							this.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							this.setId(relVo.getToProcessTaskStepId());
							this.setHandler(relVo.getToProcessStepHandler());
						}
					});
				} else if (relVo.getCondition().equalsIgnoreCase(currentProcessTaskStepVo.getStatus())) {
					nextStepList.add(new ProcessTaskStepVo() {
						{
							this.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
							this.setId(relVo.getToProcessTaskStepId());
							this.setHandler(relVo.getToProcessStepHandler());
						}
					});
				}
			}
		}
		return nextStepList;
	}

	@Override
	protected int myInit(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		/** 写入当前步骤的自定义属性值 **/
		ProcessTaskStepAttributeVo attributeVo = new ProcessTaskStepAttributeVo();
		attributeVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepAttributeVo> attributeList = processTaskMapper.getProcessTaskStepAttributeByStepId(attributeVo);
		currentProcessTaskStepVo.setAttributeList(attributeList);
		if (attributeList != null && attributeList.size() > 0) {
			JSONArray attributeObjList = null;
			if (paramObj != null && paramObj.containsKey("attributeValueList") && paramObj.get("attributeValueList") instanceof JSONArray) {
				attributeObjList = paramObj.getJSONArray("attributeValueList");
			}
			for (ProcessTaskStepAttributeVo attribute : attributeList) {
				if (attribute.getIsEditable().equals(1)) {
					if (attributeObjList != null && attributeObjList.size() > 0) {
						for (int i = 0; i < attributeObjList.size(); i++) {
							JSONObject attrObj = attributeObjList.getJSONObject(i);
							if (attrObj.getString("uuid").equals(attribute.getAttributeUuid())) {
								ProcessTaskAttributeDataVo attributeData = new ProcessTaskAttributeDataVo();
								attributeData.setData(attrObj.getString("data"));
								attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
								attributeData.setAttributeUuid(attribute.getAttributeUuid());
								// 放进去方便基类记录日志
								attribute.setAttributeData(attributeData);

								processTaskMapper.replaceProcessTaskAttributeData(attributeData);
								List<String> valueList = new ArrayList<>();
								if (attrObj.containsKey("value")) {
									if (attrObj.get("value") instanceof JSONArray) {
										for (int v = 0; v < attrObj.getJSONArray("value").size(); v++) {
											valueList.add(attrObj.getJSONArray("value").getString(v));
										}
									} else {
										valueList.add(attrObj.getString("value"));
									}
								}
								if (valueList != null && valueList.size() > 0) {
									for (String value : valueList) {
										if (StringUtils.isNotBlank(value)) {
											ProcessTaskAttributeValueVo attributeValue = new ProcessTaskAttributeValueVo();
											attributeValue.setValue(value);
											attributeValue.setAttributeUuid(attribute.getAttributeUuid());
											attributeValue.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
											processTaskMapper.insertProcessTaskAttributeValue(attributeValue);
										}
									}
								}
								break;
							}
						}
					}
					IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(attribute.getHandler());
					if (attributeHandler != null) {
						try {
							attributeHandler.valid(attribute.getAttributeData(), attribute.getConfigObj());
						} catch (Exception ex) {
							throw new ProcessTaskRuntimeException(ex);
						}
					}
				}
			}
		}

		/** 写入当前步骤的表单属性值 **/
		ProcessTaskStepFormAttributeVo formAttributeVo = new ProcessTaskStepFormAttributeVo();
		formAttributeVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepFormAttributeVo> formAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByStepId(formAttributeVo);
		currentProcessTaskStepVo.setFormAttributeList(formAttributeList);
		if (formAttributeList != null && formAttributeList.size() > 0) {
			JSONArray attributeObjList = null;
			if (paramObj != null && paramObj.containsKey("formAttributeValueList") && paramObj.get("formAttributeValueList") instanceof JSONArray) {
				attributeObjList = paramObj.getJSONArray("formAttributeValueList");
			}
			for (ProcessTaskStepFormAttributeVo attribute : formAttributeList) {
				if (attribute.getIsEditable().equals(1)) {
					if (attributeObjList != null && attributeObjList.size() > 0) {
						for (int i = 0; i < attributeObjList.size(); i++) {
							JSONObject attrObj = attributeObjList.getJSONObject(i);
							if (attrObj.getString("uuid").equals(attribute.getAttributeUuid())) {
								ProcessTaskAttributeDataVo attributeData = new ProcessTaskAttributeDataVo();
								attributeData.setData(attrObj.getString("data"));
								attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
								attributeData.setAttributeUuid(attribute.getAttributeUuid());
								processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
								// 放进去方便基类记录日志
								attribute.setAttributeData(attributeData);

								List<String> valueList = new ArrayList<>();
								if (attrObj.containsKey("value")) {
									if (attrObj.get("value") instanceof JSONArray) {
										for (int v = 0; v < attrObj.getJSONArray("value").size(); v++) {
											valueList.add(attrObj.getJSONArray("value").getString(v));
										}
									} else {
										valueList.add(attrObj.getString("value"));
									}
								}
								if (valueList != null && valueList.size() > 0) {
									for (String value : valueList) {
										if (StringUtils.isNotBlank(value)) {
											ProcessTaskAttributeValueVo attributeValue = new ProcessTaskAttributeValueVo();
											attributeValue.setValue(value);
											attributeValue.setAttributeUuid(attribute.getAttributeUuid());
											attributeValue.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
											processTaskMapper.insertProcessTaskFormAttributeValue(attributeValue);
										}
									}
								}
								break;
							}
						}
					}
					IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(attribute.getHandler());
					if (attributeHandler != null) {
						try {
							attributeHandler.valid(attribute.getAttributeData(), attribute.getConfigObj());
						} catch (Exception ex) {
							throw new ProcessTaskRuntimeException(ex);
						}
					}
				}
			}
		}

		/** 保存内容 **/
		if (paramObj != null && paramObj.containsKey("content") && StringUtils.isNotBlank(paramObj.getString("content"))) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(paramObj.getString("content"));
			processTaskMapper.insertProcessTaskContent(contentVo);
			processTaskMapper.insertProcessTaskStepContent(currentProcessTaskStepVo.getId(), contentVo.getId());
			currentProcessTaskStepVo.setContentId(contentVo.getId());
		}
		return 1;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException, ProcessTaskAbortException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int mySave(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myComment(ProcessTaskStepVo currentProcessTaskStepVo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}