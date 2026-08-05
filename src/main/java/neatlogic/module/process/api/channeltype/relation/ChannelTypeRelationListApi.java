package neatlogic.module.process.api.channeltype.relation;

import neatlogic.framework.util.$;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.ChannelTypeRelationChannelVo;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.dto.ChannelTypeVo;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeRelationListApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/relation/list";
	}

	@Override
	public String getName() {
		return "nmpacr.channeltyperelationlistapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "nmpacr.channeltyperelationlistapi.input.param.desc.keyword"),
        @Param(name = "isActive", type = ApiParamType.ENUM, desc = "nmpacr.channeltyperelationlistapi.input.param.desc.isactive", rule = "0,1"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpacr.channeltyperelationlistapi.input.param.desc.needpage"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpacr.channeltyperelationlistapi.input.param.desc.pagesize"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpacr.channeltyperelationlistapi.input.param.desc.currentpage")
	})
	@Output({
		@Param(name = "tbodyList", explode = ChannelTypeRelationVo[].class, desc = "nmpacr.channeltyperelationlistapi.output.param.desc.tbodylist"),
		@Param(explode = BasePageVo.class)
	})
	@Description(desc = "nmpacr.channeltyperelationlistapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    JSONObject resultObj = new JSONObject();
	    resultObj.put("tbodyList", new ArrayList<>());
	    ChannelTypeRelationVo channelTypeRelationVo = JSON.toJavaObject(jsonObj, ChannelTypeRelationVo.class);
 	    int pageCount = 0;
 	    if(channelTypeRelationVo.getNeedPage()) {
 	        int rowNum = channelTypeMapper.getChannelTypeRelationCount(channelTypeRelationVo);
 	        pageCount = PageUtil.getPageCount(rowNum, channelTypeRelationVo.getPageSize());
 	        resultObj.put("currentPage", channelTypeRelationVo.getCurrentPage());
 	        resultObj.put("pageSize", channelTypeRelationVo.getPageSize());
 	        resultObj.put("pageCount", pageCount);
 	        resultObj.put("rowNum", rowNum);
 	    }
 	   if(!channelTypeRelationVo.getNeedPage() || channelTypeRelationVo.getCurrentPage() <= pageCount) {
 	        List<ChannelTypeRelationVo> channelTypeRelationList = channelTypeMapper.getChannelTypeRelationList(channelTypeRelationVo);
 	        List<Long> channelTypeRelationIdList = new ArrayList<>();
 	        Map<Long, ChannelTypeRelationVo> channelTypeRelationMap = new HashMap<>();
 	        for(ChannelTypeRelationVo channelTypeRelation : channelTypeRelationList) {
 	           channelTypeRelationIdList.add(channelTypeRelation.getId());
 	           channelTypeRelationMap.put(channelTypeRelation.getId(), channelTypeRelation);
 	           Set<String> referenceUuidList = channelTypeMapper.getChannelTypeRelationReferenceUuidListByChannelTypeRelationId(channelTypeRelation.getId());
 	           channelTypeRelation.setReferenceCount(referenceUuidList.size());
 	           if(CollectionUtils.isNotEmpty(referenceUuidList)){
				   List<ChannelVo> referenceList = channelMapper.getChannelVoByUuidList(new ArrayList<>(referenceUuidList));
				   channelTypeRelation.setReferenceList(referenceList);
			   }
 	        }
 	        Map<String, ChannelTypeVo> channelTypeMap = new HashMap<>();
 	        ChannelTypeVo all = new ChannelTypeVo();
 	        all.setUuid("all");
	        all.setName($.t("nmpacr.channeltyperelationlistapi.all"));
 	        channelTypeMap.put("all", all);
 	        ChannelTypeVo channelTypeVo = new ChannelTypeVo();
 	        channelTypeVo.setPageSize(1000);
 	        List<ChannelTypeVo> channelTypeList = channelTypeMapper.searchChannelTypeList(channelTypeVo);
 	        for(ChannelTypeVo channelType : channelTypeList) {
 	           channelTypeMap.put(channelType.getUuid(), channelType);
 	        }
 	        List<ChannelTypeRelationChannelVo> channelTypeRelationSourceList = channelTypeMapper.getChannelTypeRelationSourceListByChannelTypeRelationIdList(channelTypeRelationIdList);
 	        for(ChannelTypeRelationChannelVo channelTypeRelationChannelVo : channelTypeRelationSourceList) {
 	           ChannelTypeRelationVo channelTypeRelation = channelTypeRelationMap.computeIfAbsent(channelTypeRelationChannelVo.getChannelTypeRelationId(), v -> new ChannelTypeRelationVo());
 	           channelTypeRelation.getSourceList().add(channelTypeRelationChannelVo.getChannelTypeUuid());
 	           channelTypeRelation.getSourceVoList().add(channelTypeMap.get(channelTypeRelationChannelVo.getChannelTypeUuid()).clone());
 	        }
 	        List<ChannelTypeRelationChannelVo> channelTypeRelationTargetList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationIdList(channelTypeRelationIdList);
 	        for(ChannelTypeRelationChannelVo channelTypeRelationChannelVo : channelTypeRelationTargetList) {
 	           ChannelTypeRelationVo channelTypeRelation = channelTypeRelationMap.computeIfAbsent(channelTypeRelationChannelVo.getChannelTypeRelationId(), v -> new ChannelTypeRelationVo());
 	           channelTypeRelation.getTargetList().add(channelTypeRelationChannelVo.getChannelTypeUuid());
 	           channelTypeRelation.getTargetVoList().add(channelTypeMap.get(channelTypeRelationChannelVo.getChannelTypeUuid()).clone());
            }
// 	        List<ChannelTypeRelationVo> channelTypeRelationReferenceCountList = channelTypeMapper.getChannelTypeRelationReferenceCountListByChannelTypeRelationIdList(channelTypeRelationIdList);
// 	        for(ChannelTypeRelationVo channelTypeRelation : channelTypeRelationReferenceCountList) {
// 	           channelTypeRelationMap.computeIfAbsent(channelTypeRelation.getId(), v -> new ChannelTypeRelationVo()).setReferenceCount(channelTypeRelation.getReferenceCount());
// 	        }
 	        resultObj.put("tbodyList", channelTypeRelationList);
	    }
		return resultObj;
	}

}
