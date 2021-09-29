package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import codedriver.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskCurrentStepColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Resource
	private NewWorkcenterService newWorkcenterService;
	@Override
	public String getName() {
		return "currentstep";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤";
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		//TODO 临时测试
		JSONArray stepArray = null;
		try {
		 stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		}catch(Exception ex){
			return "";
		}
		String processTaskStatus = json.getString("status");
		if(CollectionUtils.isEmpty(stepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
		ListIterator<Object> stepIterator = stepResultArray.listIterator();
		while(stepIterator.hasNext()) {
			JSONObject currentStepJson = (JSONObject)stepIterator.next();
			String stepStatus =currentStepJson.getString("status");
			Integer isActive =currentStepJson.getInteger("isactive");
			if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)&&(ProcessTaskStatus.DRAFT.getValue().equals(stepStatus)||(ProcessTaskStatus.PENDING.getValue().equals(stepStatus)&& isActive == 1)||ProcessTaskStatus.RUNNING.getValue().equals(stepStatus))) {
				JSONObject stepStatusJson = new JSONObject();
				stepStatusJson.put("name", stepStatus);
				stepStatusJson.put("text", ProcessTaskStatus.getText(stepStatus));
				stepStatusJson.put("color", ProcessTaskStatus.getColor(stepStatus));
				currentStepJson.put("status", stepStatusJson);
				JSONArray userTypeArray = currentStepJson.getJSONArray("usertypelist"); 
				if(CollectionUtils.isNotEmpty(userTypeArray)) {
					ListIterator<Object> userTypeIterator = userTypeArray.listIterator();
					while(userTypeIterator.hasNext()) {
						JSONObject userTypeJson = (JSONObject) userTypeIterator.next();
						//判断子任务|变更步骤
						if("changehandle".equals(currentStepJson.getString("handler"))
						    && ProcessUserType.MINOR.getValue().equals(userTypeJson.getString("usertype"))) {
						    userTypeJson.put("usertypename", "变更步骤");
						}else if(ProcessUserType.MINOR.getValue().equals(userTypeJson.getString("usertype"))){
						    userTypeJson.put("usertypename", "子任务");
						}
						//待处理
						if(userTypeJson.getString("usertype").equals(ProcessUserType.WORKER.getValue())) {
							JSONArray userArray = userTypeJson.getJSONArray("userlist");
							JSONArray userArrayTmp = new JSONArray();
							if(CollectionUtils.isNotEmpty(userArray)) {
								List<String> userList = userArray.stream().map(object -> object.toString()).collect(Collectors.toList());
								for(String user :userList) {
									if(StringUtils.isNotBlank(user.toString())) {
										if(user.toString().startsWith(GroupSearch.USER.getValuePlugin())) {
											UserVo userVo =userMapper.getUserBaseInfoByUuid(user.toString().replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
											if(userVo != null) {
												UserVo vo = new UserVo();
												BeanUtils.copyProperties(userVo,vo);
												userArrayTmp.add(vo);
											}
										}else if(user.toString().startsWith(GroupSearch.ROLE.getValuePlugin())) {
											RoleVo roleVo = roleMapper.getRoleByUuid(user.toString().replaceFirst(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
											if(roleVo != null) {
												JSONObject vo = new JSONObject();
												vo.put("initType",GroupSearch.ROLE.getValue());
												vo.put("uuid",roleVo.getUuid());
												vo.put("name",roleVo.getName());
												userArrayTmp.add(vo);
											}
										}else if(user.toString().startsWith(GroupSearch.TEAM.getValuePlugin())) {
											TeamVo teamVo = teamMapper.getTeamByUuid(user.toString().replaceFirst(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
											if(teamVo != null) {
												JSONObject vo = new JSONObject();
												vo.put("initType",GroupSearch.TEAM.getValue());
												vo.put("uuid",teamVo.getUuid());
												vo.put("name",teamVo.getName());
												userArrayTmp.add(vo);
											}
										}
									}

								}
								userTypeJson.put("workerlist", userArrayTmp);
								userTypeJson.put("userlist", CollectionUtils.EMPTY_COLLECTION);
							}
						}else {//处理中
							JSONArray userArray = userTypeJson.getJSONArray("userlist");
							JSONArray userArrayTmp = new JSONArray();
							if(CollectionUtils.isNotEmpty(userArray)) {
								List<String> userList = userArray.stream().map(object -> object.toString()).collect(Collectors.toList());
								for(String user :userList) {
									if(StringUtils.isNotBlank(user.toString())) {
										UserVo userVo =userMapper.getUserBaseInfoByUuid(user.toString().replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
										if(userVo != null) {
											UserVo vo = new UserVo();
											BeanUtils.copyProperties(userVo,vo);
											userArrayTmp.add(vo);
										}
									}
									
								}
								userTypeJson.put("userlist", userArrayTmp);
							}
						}
					}
				}
			}else {
				stepIterator.remove();
			}
		}
		return stepResultArray;
	}*/

	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 5;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		StringBuilder sb = new StringBuilder();
		if(json != null){
			JSONArray array = JSONArray.parseArray(json.toString());
			if(CollectionUtils.isNotEmpty(array)){
				for(int i = 0;i < array.size();i++){
					sb.append(array.getJSONObject(i).getString("name") + ";");
				}
			}
		}
		return sb.toString();
	}*/

	@Override
	public String getSimpleValue(ProcessTaskVo processTaskVo) {
		List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
		List<String> stepNameList = new ArrayList<>();
		if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
			for (ProcessTaskStepVo stepVo : stepVoList) {
				if ((ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) || (ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus()) && stepVo.getIsActive() == 1) || ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()))) {
					stepNameList.add(stepVo.getName());
				}
			}
		}
		return String.join(",", stepNameList);
	}
	
	@Override
	public Boolean getMyIsExport() {
        return false;
    }

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONArray currentStepArray = new JSONArray();
		List<ProcessTaskStepVo> stepVoList =  processTaskVo.getStepList();
		if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
			for (ProcessTaskStepVo stepVo : stepVoList) {
				if(ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) ||
						ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()) ||
						(ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())&& stepVo.getIsActive() == 1)
				) {
					JSONObject currentStepJson  = new JSONObject();
					currentStepJson.put("name",stepVo.getName());
					JSONObject currentStepStatusJson = new JSONObject();
					currentStepStatusJson.put("name",stepVo.getStatus());
					currentStepStatusJson.put("text", ProcessTaskStatus.getText(stepVo.getStatus()));
					currentStepStatusJson.put("color", ProcessTaskStatus.getColor(stepVo.getStatus()));
					currentStepJson.put("status",currentStepStatusJson);

					//查询其它步骤handler minorList
					JSONArray workerArray = new JSONArray();
					newWorkcenterService.getStepTaskWorkerList(workerArray,stepVo);

					currentStepJson.put("workerList",workerArray);
					currentStepArray.add(currentStepJson);
				}
			}
		}
		return currentStepArray;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return SqlTableUtil.getTableSelectColumn();
	}


	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return SqlTableUtil.getStepUserJoinTableSql();
	}
}
