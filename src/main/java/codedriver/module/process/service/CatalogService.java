package codedriver.module.process.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.process.dto.CatalogVo;

public interface CatalogService {
	/**
	 * 
	* @Time:2020年7月7日
	* @Description: 判断是否需要重建左右编码
	* @return boolean
	 */
	public boolean checkLeftRightCodeIsExists();
	/**
	 * 
	* @Time:2020年7月7日
	* @Description: 重建左右编码
	* @param parentUuid 父级uuid
	* @param parentLft 父级左编码
	* @return Integer
	 */
	public Integer rebuildLeftRightCode(String parentUuid, int parentLft);
	/**
	 * 
	* @Time:2020年7月7日
	* @Description: 查出当前用户有上报权限的所有服务，根据服务是否激活，服务是否授权，服务的所有上级目录是否都授权来判断
	* @return List<String> 返回有上报权限的所有服务集合
	 */
	public List<String> getCurrentUserAuthorizedChannelUuidList();
	/**
	 * 
	* @Time:2020年7月7日
	* @Description: 判断当前用户是否有channelUuid服务的上报权限，根据服务是否激活，服务是否授权，服务的所有上级目录是否都授权来判断
	* @param channelUuid
	* @return boolean
	 */
	public boolean channelIsAuthority(String channelUuid);
	
	/**
	 * 获取服务目录底下的服务目录&&服务
	 * @param catalog
	 * @return JSONArray
	 */
	public JSONArray getCatalogChannelByCatalogUuid(CatalogVo catalog);
}
