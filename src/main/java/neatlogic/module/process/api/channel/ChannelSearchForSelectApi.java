package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelSearchForSelectApi extends PrivateApiComponentBase {

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getToken() {
        return "process/channel/search/forselect";
    }

    @Override
    public String getName() {
        return "nmpac.channelsearchforselectapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpac.channelsearchforselectapi.input.param.desc.keyword"),
            @Param(name = "parentUuid", type = ApiParamType.STRING, desc = "nmpac.channelsearchforselectapi.input.param.desc.parentuuid"),
            @Param(name = "formUuid", type = ApiParamType.STRING, desc = "nmpac.channelsearchforselectapi.input.param.desc.formuuid"),
            @Param(name = "uuidList", type = ApiParamType.JSONARRAY, desc = "nmpac.channelsearchforselectapi.input.param.desc.uuidlist"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "nmpac.channelsearchforselectapi.input.param.desc.defaultvalue", xss = true),
            @Param(name = "isFavorite", type = ApiParamType.ENUM, desc = "nmpac.channelsearchforselectapi.input.param.desc.isfavorite", rule = "0,1"),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "nmpac.channelsearchforselectapi.input.param.desc.isactive", rule = "0,1"),
            @Param(name = "isAuthenticate", type = ApiParamType.ENUM, desc = "nmpac.channelsearchforselectapi.input.param.desc.isauthenticate", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpac.channelsearchforselectapi.input.param.desc.needpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpac.channelsearchforselectapi.input.param.desc.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpac.channelsearchforselectapi.input.param.desc.currentpage")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsearchforselectapi.output.param.desc.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsearchforselectapi.output.param.desc.pagesize"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsearchforselectapi.output.param.desc.pagecount"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channelsearchforselectapi.output.param.desc.rownum"),
            @Param(name = "list", explode = ValueTextVo[].class, desc = "nmpac.channelsearchforselectapi.output.param.desc.list")
    })
    @Description(desc = "nmpac.channelsearchforselectapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        ChannelVo channelVo = JSON.toJavaObject(jsonObj, ChannelVo.class);
        channelVo.setUserUuid(UserContext.get().getUserUuid(true));
        Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
        if (Objects.equal(isAuthenticate, 1)) {
            //查出当前用户已授权的服务
            channelVo.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
            channelVo.setIsActive(1);
        }
        //回显服务
        JSONArray defaultValue = channelVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> authorizationUuidList = channelVo.getAuthorizedUuidList();
            if (CollectionUtils.isNotEmpty(authorizationUuidList)) {
                authorizationUuidList.removeIf(uuid -> !defaultValue.contains(uuid));
            } else {
                channelVo.setAuthorizedUuidList(defaultValue.toJavaList(String.class));
            }
            channelVo.setNeedPage(false);
        }
        List<String> uuidList = channelVo.getUuidList();
        if (CollectionUtils.isNotEmpty(uuidList)) {
            List<String> authorizationUuidList = channelVo.getAuthorizedUuidList();
            if (CollectionUtils.isNotEmpty(authorizationUuidList)) {
                authorizationUuidList.removeIf(uuid -> !uuidList.contains(uuid));
            } else {
                channelVo.setAuthorizedUuidList(uuidList);
            }
            channelVo.setNeedPage(false);
        }
        if (channelVo.getNeedPage()) {
            int rowNum = channelMapper.searchChannelCount(channelVo);
            int pageCount = PageUtil.getPageCount(rowNum, channelVo.getPageSize());
            channelVo.setPageCount(pageCount);
            channelVo.setRowNum(rowNum);
            resultObj.put("currentPage", channelVo.getCurrentPage());
            resultObj.put("pageSize", channelVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }
        List<ValueTextVo> channelList = channelMapper.searchChannelListForSelect(channelVo);
        resultObj.put("list", channelList);
        return resultObj;
    }

}
