package neatlogic.module.process.api.catalog;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CalalogBreadcrumbSearchApi extends PrivateApiComponentBase {

    @Resource
    private CatalogService catalogService;

    @Resource
    private CatalogMapper catalogMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "process/catalog/breadcrumb/search";
    }

    @Override
    public String getName() {
        return "nmpacr.calalogbreadcrumbapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "catalogUuid", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.cataloguuid"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "breadcrumbList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist")
    })
    @Description(desc = "nmpacr.calalogbreadcrumbapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String catalogUuid = jsonObj.getString("catalogUuid");
        CatalogVo catalog = null;
        //如果catalogUuid为0，则构建一个虚拟的root目录
        if (CatalogVo.ROOT_UUID.equals(catalogUuid)) {
            catalog = catalogService.buildRootCatalog();
        } else {
            catalog = catalogMapper.getCatalogByUuid(catalogUuid);
            if (catalog == null) {
                throw new CatalogNotFoundException(catalogUuid);
            }
        }

        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        JSONObject resultObj = new JSONObject();
        resultObj.put("breadcrumbList", new ArrayList<>());
        //已授权的服务uuid
        List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
        if (CollectionUtils.isEmpty(currentUserAuthorizedChannelUuidList)) {
            return resultObj;
        }

        BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
        ChannelVo channel = new ChannelVo();
        channel.setKeyword(basePageVo.getKeyword());
        channel.setIsActive(1);
        channel.setAuthorizedUuidList(currentUserAuthorizedChannelUuidList);
        channel.setPageSize(100);
        int channelCount = channelMapper.searchChannelCount(channel);
        if (channelCount == 0) {
            return resultObj;
        }
        channel.setRowNum(channelCount);
        List<ChannelVo> channelList = new ArrayList<>();
        for (int i = 1; i <= channel.getPageCount(); i++) {
            channel.setCurrentPage(i);
            channelList.addAll(channelMapper.searchChannelList(channel));
        }

        List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(catalog.getLft(), catalog.getRht());
        if (CollectionUtils.isNotEmpty(catalogList)) {
            if (CatalogVo.ROOT_UUID.equals(catalogUuid)) {
                catalogList.add(catalog);
            }
            //已授权的目录uuid
            List<String> currentUserAuthorizedCatalogUuidList = catalogMapper.getAuthorizedCatalogUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);

            Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
            for (CatalogVo catalogVo : catalogList) {
                if (currentUserAuthorizedCatalogUuidList.contains(catalogVo.getUuid())) {
                    catalogVo.setAuthority(true);
                }
                uuidKeyMap.put(catalogVo.getUuid(), catalogVo);
            }
            //设置父级
            for (CatalogVo catalogVo : catalogList) {
                String parentUuid = catalogVo.getParentUuid();
                CatalogVo parent = uuidKeyMap.get(parentUuid);
                if (parent != null) {
                    catalogVo.setParent(parent);
                }
            }

            //排序
            Collections.sort(catalogList);
            //查出有已启用且有授权服务的目录uuid
            List<String> hasActiveChannelCatalogUuidList = channelList.stream().map(ChannelVo::getParentUuid).collect(Collectors.toList());
            List<Map<String, Object>> calalogBreadcrumbList = new ArrayList<>();
            for (CatalogVo catalogVo : catalogList) {
                if (!CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {//root根目录不返回
                    if (catalogVo.isAuthority() && hasActiveChannelCatalogUuidList.contains(catalogVo.getUuid())) {
                        Map<String, Object> treePathMap = new HashMap<>();
                        treePathMap.put("uuid", catalogVo.getUuid());
                        treePathMap.put("path", catalogVo.getNameList());
                        treePathMap.put("keyword", basePageVo.getKeyword());
                        calalogBreadcrumbList.add(treePathMap);
                    }
                }
            }
            if (basePageVo.getNeedPage()) {
                int rowNum = calalogBreadcrumbList.size();
                int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
                resultObj.put("currentPage", basePageVo.getCurrentPage());
                resultObj.put("pageSize", basePageVo.getPageSize());
                resultObj.put("pageCount", pageCount);
                resultObj.put("rowNum", rowNum);
                int fromIndex = basePageVo.getStartNum();
                if (fromIndex < rowNum) {
                    int toIndex = fromIndex + basePageVo.getPageSize();
                    toIndex = toIndex > rowNum ? rowNum : toIndex;
                    resultObj.put("breadcrumbList", calalogBreadcrumbList.subList(fromIndex, toIndex));
                }
            } else {
                resultObj.put("breadcrumbList", calalogBreadcrumbList);
            }
        }
        return resultObj;
    }

}
