package neatlogic.module.process.api.process;

import java.util.List;
import java.util.Objects;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.dto.ProcessVo;
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessSearchApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/search";
	}

	@Override
	public String getName() {
		return "nmpap.processsearchapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", help = "匹配名称"),
		@Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "common.isactive", rule = "0,1"),
		@Param(name = "isICreated", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "nmpap.processsearchapi.input.param.desc.isicreated"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="processList",explode=ProcessVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpap.processsearchapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ProcessVo processVo = JSONObject.toJavaObject(jsonObj, ProcessVo.class);
		JSONArray defaultValue = processVo.getDefaultValue();
		if (CollectionUtils.isNotEmpty(defaultValue)) {
			List<ProcessVo> processList = processMapper.getProcessListByUuidList(defaultValue.toJavaList(String.class));
			resultObj.put("processList", processList);
			return resultObj;
		}
		Integer isICreated = jsonObj.getInteger("isICreated");
		if (Objects.equals(isICreated, 1)) {
			processVo.setFcu(UserContext.get().getUserUuid());
		}
		if(processVo.getNeedPage()) {
			int rowNum = processMapper.searchProcessCount(processVo);
			int pageCount = PageUtil.getPageCount(rowNum, processVo.getPageSize());
			processVo.setPageCount(pageCount);
			processVo.setRowNum(rowNum);
			resultObj.put("currentPage", processVo.getCurrentPage());
			resultObj.put("pageSize", processVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ProcessVo> processList = processMapper.searchProcessList(processVo);
		int count = 0;
		for(ProcessVo process : processList) {
			count = processMapper.getProcessReferenceCount(process.getUuid());
			process.setReferenceCount(count);
		}	
		resultObj.put("processList", processList);
		return resultObj;
	}

}
