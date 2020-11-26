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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
		return "工单中心导出接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid,有则去数据库获取对应分类的条件，无则根据传的过滤条件查询"),
			@Param(name = "isMeWillDo", type = ApiParamType.INTEGER, desc = "是否带我处理的，1：是；0：否"),
			@Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件", isRequired = false),
			@Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型", isRequired = false),
			@Param(name = "headerList", type = ApiParamType.JSONARRAY, desc = "显示的字段", isRequired = false),
	})
	@Output({})
	@Description(desc = "工单中心导出接口")
	@Override
	public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(jsonObj.containsKey("uuid")) {
			String uuid = jsonObj.getString("uuid");
			Integer isMeWillDo = jsonObj.getInteger("isMeWillDo");
			List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
			if(CollectionUtils.isNotEmpty(workcenterList)) {
				jsonObj = JSONObject.parseObject(workcenterList.get(0).getConditionConfig());
				jsonObj.put("uuid", uuid);
//				jsonObj.put("currentPage", 1);
				jsonObj.put("pageSize", 2000);
				jsonObj.put("isMeWillDo", isMeWillDo);
			}
		}
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		List<WorkcenterTheadVo> headList = null;
		for(int page = 1;page < 5;page++){
			jsonObj.put("currentPage", page);
			JSONObject data = workcenterService.doSearch(new WorkcenterVo(jsonObj));
			if(MapUtils.isNotEmpty(data)){
				Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
				JSONArray theadList = data.getJSONArray("theadList");
				JSONArray tbodyList = data.getJSONArray("tbodyList");
				if(CollectionUtils.isEmpty(headList) && CollectionUtils.isNotEmpty(theadList)){
					headList = theadList.toJavaList(WorkcenterTheadVo.class);
					headList = headList.stream().filter(o -> o.getDisabled() == 0).collect(Collectors.toList());
				}
				if(CollectionUtils.isNotEmpty(tbodyList)){
					List<Map<String,Object>> dataMapList = new ArrayList<>();
					for(int i = 0;i < tbodyList.size();i++){
						Map<String,Object> map = new LinkedHashMap<>();
						JSONObject object = tbodyList.getJSONObject(i);
						for(WorkcenterTheadVo vo : headList){
							Object value = columnComponentMap.get(vo.getName()).getSimpleValue(object);
							map.put(vo.getDisplayName(),value);
						}
						dataMapList.add(map);
					}
					List<String> headerList = new ArrayList<>();
					List<String> columnList = new ArrayList<>();
					if(CollectionUtils.isNotEmpty(dataMapList)){
						Map<String, Object> map = dataMapList.get(0);
						for(String key : map.keySet()){
							headerList.add(key);
							columnList.add(key);
						}
						ExcelUtil.exportData(workbook,headerList,columnList,dataMapList,new Integer(30),page-1);
					}
				}else{
					break;
				}
			}
		}
		String fileNameEncode = "工单数据.xlsx";
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
