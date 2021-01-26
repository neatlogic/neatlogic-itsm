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
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcessTaskReporterColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return "reporter";
	}

	@Override
	public String getDisplayName() {
		return "代报人";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		if(StringUtils.isNotBlank(userUuid)) {
			userUuid =userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY);
		}
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid);
		if(userVo != null) {
			userJson.put("initType", userVo.getInitType());
			userJson.put("name", userVo.getUserName());
			userJson.put("pinyin", userVo.getPinyin());
			userJson.put("vipLevel", userVo.getVipLevel());
			userJson.put("avatar", userVo.getAvatar());
		}
		userJson.put("uuid", userUuid);
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
		return 4;
	}

	@Override
	public Object getSimpleValue(Object json) {
		String username = null;
		if(json != null){
			username = JSONObject.parseObject(json.toString()).getString("name");
		}
		return username;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new UserTable(),"reporter", Arrays.asList(
						new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(),"reporterUuid"),
						new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(),"reporterName"),
						new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(),"reporterInfo"),
						new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(),"reporterVipLevel"),
						new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(),"reporterPinYin")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new UserTable(),"reporter", new HashMap<String, String>() {{
					put(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskScoreSqlTable.FieldEnum.PROCESSTASK_ID.getValue());
				}}));
			}
		};
	}
}
