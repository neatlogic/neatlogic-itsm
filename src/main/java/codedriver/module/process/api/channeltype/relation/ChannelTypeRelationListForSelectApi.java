package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationListForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/list/forselect";
	}

	@Override
	public String getName() {
		return "查询服务类型关系列表（下拉框专用）";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关系名称，关键字搜索"),
        @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
        @Param(name = "source", type = ApiParamType.STRING, xss = true, desc = "来源服务类型uuid"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name = "list", explode = ValueTextVo[].class, desc = "服务类型关系列表"),
		@Param(explode = BasePageVo.class)
	})
	@Description(desc = "查询服务类型关系列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    JSONObject resultObj = new JSONObject();
	    resultObj.put("list", new ArrayList<>());
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
 	    int pageCount = 0;
 	    if(channelTypeRelationVo.getNeedPage()) {
 	        int rowNum = channelMapper.getChannelTypeRelationCountForSelect(channelTypeRelationVo);
 	        pageCount = PageUtil.getPageCount(rowNum, channelTypeRelationVo.getPageSize());
 	        resultObj.put("currentPage", channelTypeRelationVo.getCurrentPage());
 	        resultObj.put("pageSize", channelTypeRelationVo.getPageSize());
 	        resultObj.put("pageCount", pageCount);
 	        resultObj.put("rowNum", rowNum);
 	    }
 	   if(!channelTypeRelationVo.getNeedPage() || channelTypeRelationVo.getCurrentPage() <= pageCount) {
 	        List<ValueTextVo> list = channelMapper.getChannelTypeRelationListForSelect(channelTypeRelationVo);
 	       resultObj.put("list", list);
	    }
		return resultObj;
	}

}
