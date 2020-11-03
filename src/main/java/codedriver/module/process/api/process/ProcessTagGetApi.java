package codedriver.module.process.api.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessTagVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTagGetApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;

	@Override
	public String getToken() {
		return "process/tag/get";
	}

	@Override
	public String getName() {
		return "获取标签_下拉";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
	    @Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
        @Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
        @Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
        @Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
        @Param(name="list",explode=ValueTextVo[].class,desc="流程列表")
	})
	@Description(desc = "获取标签_下拉")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    JSONObject resultObj = new JSONObject();
		ProcessTagVo processTagVo = JSON.toJavaObject(jsonObj, ProcessTagVo.class);
		if (processTagVo.getNeedPage()) {
            int rowNum = processMapper.getProcessTagCount(processTagVo);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", processTagVo.getPageSize());
            resultObj.put("currentPage", processTagVo.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, processTagVo.getPageSize()));
        }
		resultObj.put("list", processMapper.getProcessTagForSelect(processTagVo));
		return resultObj;
	}

}
