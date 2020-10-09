package codedriver.module.process.api.workcenter;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Transactional
@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class WorkcenterTheadSaveApi extends PrivateApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter/thead/save";
	}

	@Override
	public String getName() {
		return "工单中心thead保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid",isRequired = true),
		@Param(name="theadList", type = ApiParamType.JSONARRAY, desc="分类uuid",isRequired = true),
		@Param(name="theadList[0].name", type = ApiParamType.STRING, desc="字段名"),
		@Param(name="theadList[0].width", type = ApiParamType.INTEGER, desc="字段宽度"),
		@Param(name="theadList[0].isShow", type = ApiParamType.INTEGER, desc="字段是否展示"),
		@Param(name="theadList[0].type", type = ApiParamType.STRING, desc="字段类型"),
		@Param(name="theadList[0].sort", type = ApiParamType.INTEGER, desc="字段排序")
	})
	@Output({
		
	})
	@Description(desc = "工单中心thead保存接口，用于用户自定义保存字段显示与排序")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		JSONArray theadArray = jsonObj.getJSONArray("theadList");
		if(CollectionUtils.isNotEmpty(theadArray)) {
			workcenterMapper.deleteWorkcenterThead(new WorkcenterTheadVo(uuid,UserContext.get().getUserUuid(true)));
			for(Object thead : theadArray) {
				WorkcenterTheadVo workcenterTheadVo = new WorkcenterTheadVo((JSONObject)thead);
				workcenterTheadVo.setWorkcenterUuid(uuid);
				workcenterMapper.insertWorkcenterThead(workcenterTheadVo);
			}
		}
		return null;
	}

}
