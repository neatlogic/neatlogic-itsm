/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
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
