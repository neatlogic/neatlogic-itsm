package codedriver.module.process.api.workcenter;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.process.service.WorkcenterService;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出工单中心工单数据接口
 * 同时支持按分类导出与实时查询结果导出
 */
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterDataExportApi extends PrivateBinaryStreamApiComponentBase {
	@Autowired
	WorkcenterMapper workcenterMapper;

	@Autowired
	WorkcenterService workcenterService;

	@Override
	public String getToken() {
		return "workcenter/export";
	}

	@Override
	public String getName() {
		return "导出工单中心数据";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid,据此从数据库获取对应分类的条件"),
			@Param(name = "isMeWillDo", type = ApiParamType.INTEGER, desc = "是否带我处理的，1：是；0：否"),
			@Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件", isRequired = false),
			@Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型", isRequired = false)
	})
	@Output({})
	@Description(desc = "导出工单中心数据")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String title = "";
		String uuid = jsonObj.getString("uuid");
		if(StringUtils.isNotBlank(uuid)){
			Integer isMeWillDo = jsonObj.getInteger("isMeWillDo");
			List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
			if(CollectionUtils.isNotEmpty(workcenterList)){
				title = workcenterList.get(0).getName();
				jsonObj = JSONObject.parseObject(workcenterList.get(0).getConditionConfig());
				jsonObj.put("uuid", uuid);
				jsonObj.put("isMeWillDo", isMeWillDo);
			}
		}

		WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
		Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
		/** 获取表头 */
		List<WorkcenterTheadVo> theadList = workcenterService.getWorkcenterTheadList(workcenterVo, columnComponentMap,null);
		if(CollectionUtils.isNotEmpty(theadList)){
			theadList = theadList.stream().filter(o -> o.getDisabled() == 0).filter(o -> o.getIsExport() == 1).sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
		}

		List<Map<String,Object>> list = new ArrayList<>();
		Set<String> headList = new LinkedHashSet<>();
		Set<String> columnList = new LinkedHashSet<>();
		QueryResultSet resultSet = workcenterService.searchTaskIterate(workcenterVo);
		if(CollectionUtils.isNotEmpty(theadList) && resultSet.hasMoreResults()){
			while(resultSet.hasMoreResults()){
				QueryResult result = resultSet.fetchResult();
				if(!result.getData().isEmpty()){
					for(MultiAttrsObject el : result.getData()){
						Map<String,Object> map = new LinkedHashMap<>();
						for(WorkcenterTheadVo vo : theadList){
							IProcessTaskColumn column = columnComponentMap.get(vo.getName());
							Object value = column.getSimpleValue(column.getValue(el));
							map.put(column.getDisplayName(),value);
							headList.add(column.getDisplayName());
							columnList.add(column.getDisplayName());
						}
						list.add(map);
					}
				}
			}
		}

		SXSSFWorkbook workbook = new SXSSFWorkbook();
		ExcelUtil.exportData(workbook,headList.stream().collect(Collectors.toList()), columnList.stream().collect(Collectors.toList()), list,new Integer(35),0);
		String fileNameEncode = (StringUtils.isNotBlank(title) ? title : "工单数据") + ".xlsx";
		Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
		if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
			fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
		} else {
			fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
		}
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
		try (OutputStream os = response.getOutputStream()){
			workbook.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
