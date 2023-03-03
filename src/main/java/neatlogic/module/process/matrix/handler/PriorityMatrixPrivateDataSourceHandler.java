/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.matrix.handler;

import neatlogic.framework.matrix.constvalue.MatrixAttributeType;
import neatlogic.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixFilterVo;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dto.PrioritySearchVo;
import neatlogic.framework.process.dto.PriorityVo;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class PriorityMatrixPrivateDataSourceHandler implements IMatrixPrivateDataSourceHandler {

    private final List<MatrixAttributeVo> matrixAttributeList = new ArrayList<>();

    {
        MatrixAttributeVo matrixAttributeVo = new MatrixAttributeVo();
        matrixAttributeVo.setMatrixUuid(getUuid());
        matrixAttributeVo.setUuid("b83b6711416847f7a78b1946607efb8c");
        matrixAttributeVo.setName("uuid");
        matrixAttributeVo.setLabel("uuid");
        matrixAttributeVo.setType(MatrixAttributeType.INPUT.getValue());
        matrixAttributeVo.setIsRequired(0);
        matrixAttributeVo.setIsDeletable(0);
        matrixAttributeVo.setSort(0);
        matrixAttributeVo.setPrimaryKey(1);
        matrixAttributeVo.setIsSearchable(0);
        matrixAttributeList.add(matrixAttributeVo);
    }

    {
        MatrixAttributeVo matrixAttributeVo = new MatrixAttributeVo();
        matrixAttributeVo.setMatrixUuid(getUuid());
        matrixAttributeVo.setUuid("dc1c6903648e417f88d52ad986b057a0");
        matrixAttributeVo.setName("名称");
        matrixAttributeVo.setLabel("name");
        matrixAttributeVo.setType(MatrixAttributeType.INPUT.getValue());
        matrixAttributeVo.setIsRequired(0);
        matrixAttributeVo.setIsDeletable(0);
        matrixAttributeVo.setSort(1);
        matrixAttributeVo.setPrimaryKey(0);
        matrixAttributeVo.setIsSearchable(1);
        matrixAttributeList.add(matrixAttributeVo);
    }

    @Resource
    private PriorityMapper priorityMapper;

    @Override
    public String getUuid() {
        return "1218cb14a0e94202b4f01eb640bc85cb";
    }

    @Override
    public String getName() {
        return "优先级";
    }

    @Override
    public String getLabel() {
        return "priority";
    }

    @Override
    public List<MatrixAttributeVo> getAttributeList() {
        return matrixAttributeList;
    }


    @Override
    public List<Map<String, String>> searchTableData(MatrixDataVo dataVo) {
        List<PriorityVo> priorityList = new ArrayList<>();
        JSONArray defaultValue = dataVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> uuidList = defaultValue.toJavaList(String.class);
            priorityList = priorityMapper.getPriorityByUuidList(uuidList);
        } else {
            PrioritySearchVo searchVo = matrixDataVoConvertSearchCondition(dataVo);
            int rowNum = priorityMapper.searchPriorityCountForMatrix(searchVo);
            if (rowNum > 0) {
                dataVo.setRowNum(rowNum);
                priorityList = priorityMapper.searchPriorityListForMatrix(searchVo);
            }
        }
        return priorityListConvertDataList(priorityList);
    }

    private PrioritySearchVo matrixDataVoConvertSearchCondition(MatrixDataVo dataVo) {
        PrioritySearchVo searchVo = new PrioritySearchVo();
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        if (CollectionUtils.isEmpty(filterList)) {
            return searchVo;
        }
        List<MatrixFilterVo> newFilterList = new ArrayList<>();
        for (MatrixFilterVo filter : filterList) {
            String uuid = filter.getUuid();
            if (StringUtils.isBlank(uuid)) {
                continue;
            }
            List<String> valueList = filter.getValueList();
            if (CollectionUtils.isEmpty(valueList)) {
                continue;
            }
            String value = valueList.get(0);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            if ("b83b6711416847f7a78b1946607efb8c".equals(uuid)) {
                newFilterList.add(new MatrixFilterVo("uuid", filter.getExpression(), filter.getValueList()));
//                searchVo.setUuid(value);
            } else if ("dc1c6903648e417f88d52ad986b057a0".equals(uuid)) {
                newFilterList.add(new MatrixFilterVo("name", filter.getExpression(), filter.getValueList()));
//                searchVo.setName(value);
            }
        }
        searchVo.setFilterList(newFilterList);
        return searchVo;
    }

    /**
     * 将List<PriorityVo>转换成List<Map<String, String>>
     *
     * @param priorityList 优先级列表
     * @return 输出列表
     */
    private List<Map<String, String>> priorityListConvertDataList(List<PriorityVo> priorityList) {
        List<Map<String, String>> dataList = new ArrayList<>();
        for (PriorityVo priorityVo : priorityList) {
            Map<String, String> data = new HashMap<>();
            data.put("uuid", priorityVo.getUuid());
            data.put("b83b6711416847f7a78b1946607efb8c", priorityVo.getUuid());
            data.put("dc1c6903648e417f88d52ad986b057a0", priorityVo.getName());
            dataList.add(data);
        }
        return dataList;
    }
}
