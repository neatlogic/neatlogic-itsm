package codedriver.module.process.api.workcenter;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.WorkcenterService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterGetApi extends PrivateApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;
	
	@Autowired
	WorkcenterService workcenterService;
	
	@Override
	public String getToken() {
		return "workcenter/get";
	}

	@Override
	public String getName() {
		return "获取工单中心分类接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid",isRequired = true)
	})
	@Output({
		@Param(name="workcenter", explode = WorkcenterVo.class, desc="分类信息")
	})
	@Description(desc = "获取工单中心分类接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
		if(CollectionUtils.isEmpty(workcenterList)) {
			throw new WorkcenterNotFoundException(uuid);
		}
		return workcenterList.get(0);
	}
}
