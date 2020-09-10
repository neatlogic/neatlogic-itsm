package codedriver.module.process.api.channeltype.relation;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationChannelVo;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.dto.ChannelTypeVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationListApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/list";
	}

	@Override
	public String getName() {
		return "查询服务类型关系列表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关系名称，关键字搜索"),
        @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name = "tbodyList", explode = ChannelTypeRelationVo[].class, desc = "服务类型关系列表"),
		@Param(explode = BasePageVo.class)
	})
	@Description(desc = "查询服务类型关系列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    JSONObject resultObj = new JSONObject();
	    resultObj.put("tbodyList", new ArrayList<>());
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
 	    int pageCount = 0;
 	    if(channelTypeRelationVo.getNeedPage()) {
 	        int rowNum = channelMapper.getChannelTypeRelationCount(channelTypeRelationVo);
 	        pageCount = PageUtil.getPageCount(rowNum, channelTypeRelationVo.getPageSize());
 	        resultObj.put("currentPage", channelTypeRelationVo.getCurrentPage());
 	        resultObj.put("pageSize", channelTypeRelationVo.getPageSize());
 	        resultObj.put("pageCount", pageCount);
 	        resultObj.put("rowNum", rowNum);
 	    }
 	   if(!channelTypeRelationVo.getNeedPage() || channelTypeRelationVo.getCurrentPage() <= pageCount) {
 	        List<ChannelTypeRelationVo> channelTypeRelationList = channelMapper.getChannelTypeRelationList(channelTypeRelationVo);
 	        List<Long> channelTypeRelationIdList = new ArrayList<>();
 	        Map<Long, ChannelTypeRelationVo> channelTypeRelationMap = new HashMap<>();
 	        for(ChannelTypeRelationVo channelTypeRelation : channelTypeRelationList) {
 	           channelTypeRelationIdList.add(channelTypeRelation.getId());
 	           channelTypeRelationMap.put(channelTypeRelation.getId(), channelTypeRelation);
 	        }
 	        Map<String, ChannelTypeVo> channelTypeMap = new HashMap<>();
 	        ChannelTypeVo all = new ChannelTypeVo();
 	        all.setUuid("all");
 	        all.setName("所有");
 	        channelTypeMap.put("all", all);
 	        ChannelTypeVo channelTypeVo = new ChannelTypeVo();
 	        channelTypeVo.setNeedPage(false);
 	        List<ChannelTypeVo> channelTypeList = channelMapper.searchChannelTypeList(channelTypeVo);
 	        for(ChannelTypeVo channelType : channelTypeList) {
 	           channelTypeMap.put(channelType.getUuid(), channelType);
 	        }
 	        List<ChannelTypeRelationChannelVo> channelTypeRelationSourceList = channelMapper.getChannelTypeRelationSourceListByChannelTypeRelationIdList(channelTypeRelationIdList);
 	        for(ChannelTypeRelationChannelVo channelTypeRelationChannelVo : channelTypeRelationSourceList) {
 	           ChannelTypeRelationVo channelTypeRelation = channelTypeRelationMap.computeIfAbsent(channelTypeRelationChannelVo.getChannelTypeRelationId(), v -> new ChannelTypeRelationVo());
 	           channelTypeRelation.getSourceList().add(channelTypeRelationChannelVo.getChannelTypeUuid());
 	           channelTypeRelation.getSourceVoList().add(new ChannelTypeVo(channelTypeMap.get(channelTypeRelationChannelVo.getChannelTypeUuid())));
 	        }
 	        List<ChannelTypeRelationChannelVo> channelTypeRelationTargetList = channelMapper.getChannelTypeRelationTargetListByChannelTypeRelationIdList(channelTypeRelationIdList);
 	        for(ChannelTypeRelationChannelVo channelTypeRelationChannelVo : channelTypeRelationTargetList) {
 	           ChannelTypeRelationVo channelTypeRelation = channelTypeRelationMap.computeIfAbsent(channelTypeRelationChannelVo.getChannelTypeRelationId(), v -> new ChannelTypeRelationVo());
 	           channelTypeRelation.getTargetList().add(channelTypeRelationChannelVo.getChannelTypeUuid());
 	           channelTypeRelation.getTargetVoList().add(new ChannelTypeVo(channelTypeMap.get(channelTypeRelationChannelVo.getChannelTypeUuid())));
            }
// 	        List<ChannelTypeRelationVo> channelTypeRelationReferenceCountList = channelMapper.getChannelTypeRelationReferenceCountListByChannelTypeRelationIdList(channelTypeRelationIdList);
 	        resultObj.put("tbodyList", channelTypeRelationList);
	    }
		return resultObj;
	}

}