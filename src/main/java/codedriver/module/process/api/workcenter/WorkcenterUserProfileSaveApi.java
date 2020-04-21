package codedriver.module.process.api.workcenter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterParamException;
import codedriver.framework.process.workcenter.dto.WorkcenterUserProfileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Transactional
@Service
public class WorkcenterUserProfileSaveApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;

	@Override
	public String getToken() {
		return "workcenter/user/profile/save";
	}

	@Override
	public String getName() {
		return "工单中心分类用户设置保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="viewType", type = ApiParamType.STRING, desc="数据展示类型：table|card"),
		@Param(name="workcenterList[0].uuid", type = ApiParamType.STRING, desc="分类uuid"),
		@Param(name="workcenterList[0].sort", type = ApiParamType.INTEGER, desc="分类排序")
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类用户设置保存接口,如分类排序，数据展示类型")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		if(jsonObj.isEmpty()) {
			throw new WorkcenterParamException("viewType|workcenterList");
		}
		String userId = UserContext.get().getUserId();
		WorkcenterUserProfileVo userProfile= workcenterMapper.getWorkcenterUserProfileByUserId(userId);
		if(userProfile != null) {
			JSONObject configOld = JSONObject.parseObject(userProfile.getConfig());
			if(jsonObj.containsKey("viewType")) {
				String viewType = jsonObj.getString("viewType");
				if(StringUtils.isBlank(viewType)) {
					throw new WorkcenterParamException("viewType");
				}
				configOld.put("viewType", viewType);
			}
			if(jsonObj.containsKey("workcenterList")) {
				JSONArray workcenterList = jsonObj.getJSONArray("workcenterList");
				if(CollectionUtils.isEmpty(workcenterList)) {
					throw new WorkcenterParamException("workcenterList");
				}
				configOld.put("workcenterList", jsonObj.getString("workcenterList"));
			}
			userProfile.setConfig(configOld.toJSONString());
			workcenterMapper.deleteWorkcenterUserProfileByUserId(userId);
		}else {
			userProfile = new WorkcenterUserProfileVo(userId,jsonObj.toJSONString());
		}
		workcenterMapper.insertWorkcenterUserProfile(userProfile);
		return null;
	}

}