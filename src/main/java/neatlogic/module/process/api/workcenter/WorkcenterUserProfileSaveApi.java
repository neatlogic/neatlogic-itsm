package neatlogic.module.process.api.workcenter;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterParamException;
import neatlogic.framework.process.workcenter.dto.WorkcenterUserProfileVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Transactional
@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class WorkcenterUserProfileSaveApi extends PrivateApiComponentBase {

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
		String userUuid = UserContext.get().getUserUuid();
		WorkcenterUserProfileVo userProfile= workcenterMapper.getWorkcenterUserProfileByUserUuid(userUuid);
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
			workcenterMapper.deleteWorkcenterUserProfileByUserUuid(userUuid);
		}else {
			userProfile = new WorkcenterUserProfileVo(userUuid,jsonObj.toJSONString());
		}
		workcenterMapper.insertWorkcenterUserProfile(userProfile);
		return null;
	}

}
