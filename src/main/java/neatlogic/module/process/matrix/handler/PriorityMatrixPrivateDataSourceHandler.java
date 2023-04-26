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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixFilterVo;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dto.PrioritySearchVo;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriorityMatrixPrivateDataSourceHandler implements IMatrixPrivateDataSourceHandler {

    private final List<MatrixAttributeVo> matrixAttributeList = new ArrayList<>();

    private final Map<String , String> columnsMap = new HashMap<>();

    @Resource
    private PriorityMapper priorityMapper;

    @Override
    public String getUuid() {
        return UuidUtil.getCustomUUID(getLabel());
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
        JSONArray attributeDefinedList = new JSONArray();
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "uuid");
            jsonObj.put("label", "uuid");
            jsonObj.put("isPrimaryKey", 1);
            jsonObj.put("isSearchable", 0);
            attributeDefinedList.add(jsonObj);
        }
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "名称");
            jsonObj.put("label", "name");
            jsonObj.put("isPrimaryKey", 0);
            jsonObj.put("isSearchable", 1);
            attributeDefinedList.add(jsonObj);
        }
        if (matrixAttributeList.size() == 0) {
            this.setAttribute(matrixAttributeList , attributeDefinedList);

            for(MatrixAttributeVo matrixAttributeVo : matrixAttributeList){
                columnsMap.put(matrixAttributeVo.getLabel() , matrixAttributeVo.getUuid());
            }
        }
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
            if (columnsMap.get("uuid").equals(uuid)) {
                newFilterList.add(new MatrixFilterVo("uuid", filter.getExpression(), filter.getValueList()));
            } else if (columnsMap.get("name").equals(uuid)) {
                newFilterList.add(new MatrixFilterVo("name", filter.getExpression(), filter.getValueList()));
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
            data.put(columnsMap.get("uuid"), priorityVo.getUuid());
            data.put(columnsMap.get("name"), priorityVo.getName());
            dataList.add(data);
        }
        return dataList;
    }
}
