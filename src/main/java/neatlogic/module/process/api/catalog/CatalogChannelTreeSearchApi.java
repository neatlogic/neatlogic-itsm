package neatlogic.module.process.api.catalog;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.CatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class CatalogChannelTreeSearchApi extends PrivateApiComponentBase {

    @Resource
    private CatalogService catalogService;

    @Resource
    private CatalogMapper catalogMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "process/catalog/channel/tree/search";
    }

    @Override
    public String getName() {
        return "nmpac.catalogchanneltreesearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "Return", explode = CatalogVo[].class, desc = "nmpac.catalogchanneltreesearchapi.output.param.desc.return.name")
    })
    @Description(desc = "nmpac.catalogchanneltreesearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
        CatalogVo rootCatalog = catalogService.buildRootCatalog();
        List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalog.getLft(), rootCatalog.getRht());
        //将虚拟的root节点加入到catalogList中
        catalogList.add(rootCatalog);
        for (CatalogVo catalogVo : catalogList) {
            uuidKeyMap.put(catalogVo.getUuid(), catalogVo);
        }
        for (CatalogVo catalogVo : catalogList) {
            String parentUuid = catalogVo.getParentUuid();
            CatalogVo parent = uuidKeyMap.get(parentUuid);
            if (parent != null) {
                catalogVo.setParent(parent);
            }
        }

        Map<String, Integer> channelReferencedCountMap = new HashMap<>();
        List<ChannelVo> channelReferencedCountList = processTaskMapper.getChannelReferencedCountList();
        for (ChannelVo channelVo : channelReferencedCountList) {
            channelReferencedCountMap.put(channelVo.getUuid(), channelVo.getChildrenCount());
        }
        List<ChannelVo> channelList = channelMapper.getChannelListForTree(null);
        for (ChannelVo channelVo : channelList) {
            String parentUuid = channelVo.getParentUuid();
            CatalogVo parent = uuidKeyMap.get(parentUuid);
            if (parent != null) {
                channelVo.setParent(parent);
                Integer count = channelReferencedCountMap.get(channelVo.getUuid());
                if (count != null) {
                    channelVo.setChildrenCount(count);
                }
            }
        }

        return rootCatalog.getChildren();
    }
}
