/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.dao.mapper.catalog;

import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.process.crossover.ICatalogCrossoverMapper;
import neatlogic.framework.process.dto.CatalogVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CatalogMapper extends ICatalogCrossoverMapper {

	List<CatalogVo> getCatalogList(CatalogVo catalogVo);

	CatalogVo getCatalogByUuid(String uuid);
	
	int checkCatalogIsExists(String catalogUuid);

	List<String> getHasActiveChannelCatalogUuidList(List<String> channelUuidList);

	int checkCatalogNameIsRepeat(CatalogVo catalogVo);

	List<CatalogVo> getCatalogListForTree(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<AuthorityVo> getCatalogAuthorityListByCatalogUuid(String uuid);
	
	List<String> getAuthorizedCatalogUuidList(
			@Param("userUuid")String userUuid, 
			@Param("teamUuidList")List<String> teamUuidList, 
			@Param("roleUuidList")List<String> roleUuidList, 
			@Param("action") String action,
			@Param("catalogUuid") String catalogUuid
			);

	List<String> getAuthorizedCatalogUuidListByCatalogUuidList(
			@Param("userUuid")String userUuid,
			@Param("teamUuidList")List<String> teamUuidList,
			@Param("roleUuidList")List<String> roleUuidList,
			@Param("action") String action,
			@Param("isActive") Integer isActive,
			@Param("catalogUuidList") List<String> catalogUuidList
			);

//	String getCatalogLockByUuid(String uuid);

	List<CatalogVo> getCatalogListByParentUuid(String parentUuid);

//	int checkCatalogIsExistsByLeftRightCode(@Param("uuid")String uuid, @Param("lft") Integer lft, @Param("rht") Integer rht);

//	int getCatalogCount(CatalogVo catalogVo);

	int getCatalogCountOnLock();
	/**
	 * 
	* @Time:2020年7月7日
	* @Description: 根据左右编码查出目录及所有上级目录
	* @param lft 左编码
	* @param rht 右编码
	* @return List<CatalogVo>
	 */
	List<CatalogVo> getAncestorsAndSelfByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<String> getUpwardUuidListByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<String> getDownwardUuidListByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);
	/**
	 * 根据父uuid获取授权的子服务目录列表
	 * @param uuid
	 * @return List<CatalogVo>
	 */
    List<CatalogVo> getAuthorizedCatalogList(
            @Param("userUuid")String userUuid,
            @Param("teamUuidList")List<String> teamUuidList,
            @Param("roleUuidList")List<String> roleUuidList,
			@Param("action") String action,
            @Param("parentUuid") String parentUuid,
            @Param("uuid") String uuid
    );

    Integer getMaxRhtCode();
    
    /**
     * 
    * @Time:2020年7月20日
    * @Description: 判断左右编码是否全部正确，符合下列条件的才正确
    * 1.左右编码不能为null
    * 2.左编码不能小于2，右编码不能小于3
    * 3.子节点的左编码大于父节点的左编码，子节点的右编码小于父节点的右编码
    * 4.没有子节点的节点左编码比右编码小1
    * @return int 返回左右编码不正确的个数
     */
//    int checkLeftRightCodeIsWrong();

    List<String> getCatalogUuidListByLftRht(@Param("lft") Integer lft, @Param("rht")Integer rht);

	String getParentUuidByUuid(String uuid);

	List<CatalogVo> getCatalogByName(String name);

	List<CatalogVo> getCatalogListByUuidList(List<String> uuidList);

	int insertCatalog(CatalogVo catalogVo);

	int insertCatalogAuthority(@Param("authorityVo")AuthorityVo authorityVo,@Param("catalogUuid")String catalogUuid);

//	int updateCatalogParentUuidByUuid(CatalogVo catalogVo);

	int updateCatalogLeftRightCode(@Param("uuid") String uuid, @Param("lft") int lft, @Param("rht") int rht);

//	int batchUpdateCatalogLeftRightCodeByLeftRightCode(@Param("lft") Integer lft, @Param("rht") Integer rht, @Param("step") int step);

//	int batchUpdateCatalogLeftCode(@Param("minCode") Integer minCode, @Param("step") int step);

//	int batchUpdateCatalogRightCode(@Param("minCode") Integer minCode, @Param("step") int step);

	int updateCatalogByUuid(CatalogVo catalogVo);
	
	int deleteCatalogByUuid(String uuid);

	int deleteCatalogAuthorityByCatalogUuid(String uuid);
}
