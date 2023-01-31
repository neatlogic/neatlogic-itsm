package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskFocusSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.UserTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessTaskFocusUsersColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "focususers";
	}

	/** 此列在工单中心不需要中文名，也不需要可拖拽，所以displayName为空且disable为true */
	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public Boolean getDisabled() {
		return true;
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject focusUserObj = new JSONObject();
		JSONArray focusUsers = json.getJSONArray(this.getName());
		focusUserObj.put("focusUserList",focusUsers);
		boolean isCurrentUserFocus = false;
		if(CollectionUtils.isNotEmpty(focusUsers)){
			String userUuid = "user#" + UserContext.get().getUserUuid();
			isCurrentUserFocus = focusUsers.contains(userUuid);
		}
		focusUserObj.put("isCurrentUserFocus",isCurrentUserFocus ? 1 : 0);
		return focusUserObj;
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
		return null;
	}

	@Override
	public Integer getSort() {
		return -1;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		return null;
	}*/

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONObject focusUserObj = new JSONObject();
		int isCurrentUserFocus = 0;
		if(CollectionUtils.isNotEmpty(processTaskVo.getFocusUserList())) {
			List<String> focusUserUuidList = processTaskVo.getFocusUserList().stream().map(UserVo::getUuid).collect(Collectors.toList());
			focusUserObj.put("focusUserList", focusUserUuidList);
			if(focusUserUuidList.contains(UserContext.get().getUserUuid())) {
				isCurrentUserFocus = 1;
			}
		}
		focusUserObj.put("isCurrentUserFocus",isCurrentUserFocus);
		return focusUserObj;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new UserTable(),"focus", Arrays.asList(
						new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(),"focusUuid"),
						new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(),"focusName"),
						new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(),"focusInfo"),
						new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(),"focusVipLevel"),
						new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(),"focusPinYin")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskFocusSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskFocusSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
				}}));
				add(new JoinTableColumnVo(new ProcessTaskFocusSqlTable(), new UserTable(),"focus", new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ProcessTaskFocusSqlTable.FieldEnum.USER_UUID.getValue(), UserTable.FieldEnum.UUID.getValue()));
				}}));
			}
		};
	}
}
