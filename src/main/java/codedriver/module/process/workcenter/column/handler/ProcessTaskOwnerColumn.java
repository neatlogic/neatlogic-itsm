package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dashboard.dto.DashboardDataGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataSubGroupVo;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.UserTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskOwnerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return "owner";
	}

	@Override
	public String getDisplayName() {
		return "上报人";
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
//		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
//		if(userVo != null) {
//			userJson.put("username", userVo.getUserName());
//			//获取用户头像
//			userJson.put("avatar", userVo.getAvatar());
//			//获取用户VIP等级
//			userJson.put("vipLevel",userVo.getVipLevel());
//		}
		return userVo != null ? JSON.parseObject(JSONObject.toJSONString(userVo)) : null;
	}*/
	
	/*@Override
	public JSONObject getMyValueText(JSONObject json) {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
		if(userVo != null) {
			userJson.put("text", userVo.getUserName());
			userJson.put("value", userVo.getUserId());
		}
		return userJson;
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
		return 3;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		String userName = null;
		if(json != null){
			userName = ((UserVo)json).getUserName();
		}
		return userName;
	}*/

	@Override
	public String getSimpleValue(ProcessTaskVo taskVo) {
		if(taskVo.getOwnerVo() != null) {
			return taskVo.getOwnerVo().getName();
		}
		return null;
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		return processTaskVo.getOwnerVo();
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new UserTable(),"owner", Arrays.asList(
						new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(),"ownerUuid",true),
						new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(),"ownerName"),
						new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(),"ownerInfo"),
						new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(),"ownerVipLevel"),
						new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(),"ownerPinYin")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new UserTable(),"owner", new HashMap<String, String>() {{
					put(ProcessTaskSqlTable.FieldEnum.OWNER.getValue(), UserTable.FieldEnum.UUID.getValue());
				}}));
			}
		};
	}

	@Override
	public void getMyDashboardDataVo(DashboardDataVo dashboardDataVo, WorkcenterVo workcenterVo, List<Map<String, Object>> mapList) {
		if (getName().equals(workcenterVo.getDashboardConfigVo().getGroup())) {
			DashboardDataGroupVo dashboardDataGroupVo = new DashboardDataGroupVo("ownerUuid", workcenterVo.getDashboardConfigVo().getGroup(), "ownerName", workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
			dashboardDataVo.setDataGroupVo(dashboardDataGroupVo);
		}
		//如果存在子分组
		if (getName().equals(workcenterVo.getDashboardConfigVo().getSubGroup())) {
			DashboardDataSubGroupVo dashboardDataSubGroupVo = null;
			dashboardDataSubGroupVo = new DashboardDataSubGroupVo("ownerUuid", workcenterVo.getDashboardConfigVo().getSubGroup(), "ownerName");
			dashboardDataVo.setDataSubGroupVo(dashboardDataSubGroupVo);
		}
	}

	@Override
	public LinkedHashMap<String, Object> getMyExchangeToDashboardGroupDataMap(List<Map<String, Object>> mapList) {
		LinkedHashMap<String, Object> groupDataMap = new LinkedHashMap<>();
		for (Map<String, Object> dataMap : mapList) {
			groupDataMap.put(dataMap.get("ownerUuid").toString(), dataMap.get("count"));
		}
		return groupDataMap;
	}
}
