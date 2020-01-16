package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelProcessVo;
import codedriver.module.process.dto.ChannelVo;
@Service
public class ProcessReferenceListApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/reference/list";
	}

	@Override
	public String getName() {
		return "流程引用列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name = "channelList", explode = ChannelVo[].class, desc = "流程引用列表")
	})
	@Description(desc = "流程引用列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ChannelProcessVo channelProcessVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelProcessVo>() {});
		if(channelProcessVo.getNeedPage()) {
			int rowNum = processMapper.getProcessReferenceCount(channelProcessVo.getProcessUuid());
			int pageCount = PageUtil.getPageCount(rowNum, channelProcessVo.getPageSize());
			int currentPage = channelProcessVo.getCurrentPage();
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", pageCount);
			resultObj.put("currentPage", currentPage);
			resultObj.put("pageSize", channelProcessVo.getPageSize());		
		}
		
		List<ChannelVo> channelList = processMapper.getProcessReferenceList(channelProcessVo);
		resultObj.put("channelList", channelList);
		return resultObj;
	}

}
