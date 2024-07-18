package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.constvalue.CatalogChannelAuthorityAction;
import neatlogic.framework.process.dto.CatalogVo;

import java.util.List;

public interface CatalogService {
    /**
     * @return Integer
     * @Description: 重建左右编码
     */
    void rebuildLeftRightCode();

    /**
     * @return List<String> 返回有上报权限的所有服务集合
     * @Description: 查出当前用户有上报权限的所有服务，根据服务是否激活，服务是否授权，服务的所有上级目录是否都授权来判断
     */
    List<String> getCurrentUserAuthorizedChannelUuidList();

    /**
     * @param channelUuid 通道uuid
     * @param userUuid    用户uuid
     * @param action      授权类型
     * @return 是否合法
     * @Description: 判断当前用户是否有channelUuid服务的上报权限，根据服务是否激活，服务是否授权，服务的所有上级目录是否都授权来判断
     */
    boolean channelIsAuthority(String channelUuid, String userUuid, CatalogChannelAuthorityAction action);

    /**
     * 获取服务目录底下的服务目录&&服务
     *
     * @return JSONArray
     */
    JSONObject getCatalogChannelByCatalogUuid(CatalogVo catalog, Boolean isNeedChannel);

    /**
     * @return
     * @Description: 构造一个虚拟的root节点
     */
    CatalogVo buildRootCatalog();

    List<String> getChannelRelationTargetChannelUuidList(String channelUuid, Long channelTypeRelationId);

    /**
     * @param catalogUuid 目录uuid
     * @return 目录列表
     * @Description: 根据父服务目录uuid 获取 存在服务且有权限的 子服务目录
     */
    List<CatalogVo> getCatalogByCatalogParentUuid(String catalogUuid);
}
