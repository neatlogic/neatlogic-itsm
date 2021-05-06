package codedriver.module.process.api.catalog;

import java.util.*;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.module.process.service.CatalogService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class CatalogChannelTreeSearchApi extends PrivateApiComponentBase {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CatalogMapper catalogMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "process/catalog/channel/tree/search";
    }

    @Override
    public String getName() {
        return "服务目录及通道树查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "Return", explode = CatalogVo[].class, desc = "服务目录及通道树")
    })
    @Description(desc = "服务目录及通道树查询接口")
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
