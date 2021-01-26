package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskScoreSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.UserTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

	@Override
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
	}
	
	@Override
	public JSONObject getMyValueText(JSONObject json) {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
		if(userVo != null) {
			userJson.put("text", userVo.getUserName());
			userJson.put("value", userVo.getUserId());
		}
		return userJson;
	}

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

	@Override
	public Object getSimpleValue(Object json) {
		String userName = null;
		if(json != null){
			userName = JSONObject.parseObject(json.toString()).getString("userName");
		}
		return userName;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new UserTable(),"owner", Arrays.asList(
						new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(),"ownerUuid"),
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
					put(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskScoreSqlTable.FieldEnum.PROCESSTASK_ID.getValue());
				}}));
			}
		};
	}
}
