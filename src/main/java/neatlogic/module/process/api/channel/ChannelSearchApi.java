package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ChannelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelSearchApi extends PrivateApiComponentBase {

	@Resource
	private ChannelService channelService;

	@Override
	public String getToken() {
		return "process/channel/search";
	}

	@Override
	public String getName() {
		return "nmpac.channelsearchapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "term.itsm.cataloguuid"),
		@Param(name = "isFavorite", type = ApiParamType.ENUM, desc = "nmpac.channelsearchapi.input.param.desc.isfavorite", rule = "0,1", help = "nmpac.channelsearchapi.input.param.help.isfavorite"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "common.isactive", rule = "0,1"),
		@Param(name = "isAuthenticate", type = ApiParamType.ENUM, desc = "common.isauthenticate", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid"),
        @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "term.itsm.channeluuid")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="channelList",explode=ChannelVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpac.channelsearchapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		String channelUuid = jsonObj.getString("channelUuid");
		Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		List<ChannelVo> channelList = channelService.searchChannelList(channelVo, isAuthenticate, channelUuid, channelTypeRelationId);
		resultObj.put("currentPage",channelVo.getCurrentPage());
		resultObj.put("pageSize",channelVo.getPageSize());
		resultObj.put("pageCount", channelVo.getPageCount());
		resultObj.put("rowNum", channelVo.getRowNum());
		resultObj.put("channelList", channelList);
//		if(hasData) {
//		    int pageCount = 0;
//		    if(channelVo.getNeedPage()) {
//	            int rowNum = channelMapper.searchChannelCount(channelVo);
//	            pageCount = PageUtil.getPageCount(rowNum,channelVo.getPageSize());
//	            resultObj.put("currentPage",channelVo.getCurrentPage());
//	            resultObj.put("pageSize",channelVo.getPageSize());
//	            resultObj.put("pageCount", pageCount);
//	            resultObj.put("rowNum", rowNum);
//	        }
//		    if(!channelVo.getNeedPage() || channelVo.getCurrentPage() <= pageCount) {
//	            List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
//	            resultObj.put("channelList", channelList);
//		    }
//		}
		return resultObj;
	}

}
