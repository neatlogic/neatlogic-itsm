package neatlogic.module.process.api.catalog.relation;

import java.util.*;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.CatalogMapper;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.module.process.service.CatalogService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class CatalogChannelTreeApi extends PrivateApiComponentBase {

	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/catalog/channel/tree";
	}

	@Override
	public String getName() {
		return "查询服务目录及通道树";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
	    @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id")
	})
	@Output({
		@Param(name="Return",explode = CatalogVo[].class,desc="服务目录及通道树")
	})
	@Description(desc = "查询服务目录及通道树")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
		List<String> channelTypeUuidList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
        if(CollectionUtils.isNotEmpty(channelTypeUuidList)) {
            List<ChannelVo> channelList = new ArrayList<>();
            if(channelTypeUuidList.contains("all")) {
                channelList = channelMapper.getChannelListForTree(null);
            }else {
                channelList = channelMapper.getChannelListByChannelTypeUuidList(channelTypeUuidList);
            }
            
            if(CollectionUtils.isNotEmpty(channelList)) {
                Map<String, CatalogVo> uuidKeyMap = new HashMap<>();
                CatalogVo rootCatalog = catalogService.buildRootCatalog();
                List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalog.getLft(), rootCatalog.getRht());
                if(CollectionUtils.isNotEmpty(catalogList)) {
                    //将虚拟的root节点加入到catalogList中
                    catalogList.add(rootCatalog);
                    for(CatalogVo catalogVo : catalogList) {
                        uuidKeyMap.put(catalogVo.getUuid(), catalogVo);         
                    }
                }

                for(ChannelVo channelVo : channelList) {
                    String parentUuid = channelVo.getParentUuid();
                    CatalogVo parent = uuidKeyMap.get(parentUuid);
                    if(parent != null) {
                        channelVo.setParent(parent);
                    }
                    
                    while(parent.getParent() == null) {
                        parentUuid = parent.getParentUuid();
                        if(CatalogVo.ROOT_PARENTUUID.equals(parentUuid)) {
                            break;
                        }
                        CatalogVo parentParent = uuidKeyMap.get(parentUuid);
                        if(parentParent == null) {
                            break;
                        }
                        parent.setParent(parentParent);
                        parent = parentParent;
                    }
                }
                return rootCatalog.getChildren();
            }
        }		
		return new ArrayList<>();
	}
	
}
