package codedriver.module.process.api.processtask.datamigration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskImportAuditVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcesstaskImportAuditSearchApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/import/audit/search";
	}

	@Override
	public String getName() {
		return "查询工单导入记录";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "processTaskId",
					type = ApiParamType.LONG,
					desc = "工单ID"),
			@Param(name = "title",
					type = ApiParamType.STRING,
					desc = "标题",
					xss = true),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "needPage",
				type = ApiParamType.BOOLEAN,
				desc = "是否需要分页，默认true")
	})
	@Output({
			@Param(name = "auditList",
					type = ApiParamType.JSONARRAY,
					explode = ProcessTaskImportAuditVo[].class,
					desc = "工单导入记录"),
			@Param(explode=BasePageVo.class)
	})
	@Description(desc = "查询工单导入记录")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessTaskImportAuditVo auditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessTaskImportAuditVo>() {});
		JSONObject returnObj = new JSONObject();
		if (auditVo.getNeedPage()) {
			int rowNum = processTaskMapper.searchProcessTaskImportAuditCount(auditVo);
			returnObj.put("pageSize", auditVo.getPageSize());
			returnObj.put("currentPage", auditVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, auditVo.getPageSize()));
		}
		List<ProcessTaskImportAuditVo> auditList = processTaskMapper.searchProcessTaskImportAudit(auditVo);
		returnObj.put("auditList", auditList);
		return returnObj;
	}

}
