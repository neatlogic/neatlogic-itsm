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

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.crossover.IPriorityCrossoverMapper;
import neatlogic.framework.process.dto.PrioritySearchVo;
import neatlogic.framework.process.dto.PriorityVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PriorityMapper extends IPriorityCrossoverMapper {

	int searchPriorityCount(PriorityVo priorityVo);

	List<PriorityVo> searchPriorityList(PriorityVo priorityVo);

	int searchPriorityCountForMatrix(PrioritySearchVo searchVo);

	List<PriorityVo> searchPriorityListForMatrix(PrioritySearchVo searchVo);

	List<ValueTextVo> searchPriorityListForSelect(PriorityVo priorityVo);

	int checkPriorityIsExists(String uuid);

	PriorityVo getPriorityByUuid(String uuid);

	List<PriorityVo> getPriorityByUuidList(List<String> uuidList);

    PriorityVo getPriorityByName(String objValue);

	int checkPriorityNameIsRepeat(PriorityVo priorityVo);

	Integer getMaxSort();

	int checkPriorityIsInvoked(String uuid);

	int insertPriority(PriorityVo priorityVo);

	int updatePriority(PriorityVo priorityVo);
	/**
	 * 
	* @date 2020年7月8日
	* @description 从fromSort到toSort之间（fromSort和toSort）的序号加一
	* @param fromSort
	* @param toSort
	* @return int
	 */
	int updateSortIncrement(@Param("fromSort")Integer fromSort, @Param("toSort")Integer toSort);
	/**
	 * 
	* @date 2020年7月8日
	* @description 从fromSort到toSort之间（fromSort和toSort）的序号减一
	* @param fromSort
	* @param toSort
	* @return int
	 */
	int updateSortDecrement(@Param("fromSort")Integer fromSort, @Param("toSort")Integer toSort);

	int deletePriorityByUuid(String uuid);
}
