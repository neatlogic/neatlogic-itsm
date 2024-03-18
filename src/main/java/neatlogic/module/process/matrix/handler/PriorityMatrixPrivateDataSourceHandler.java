/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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

    {
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
        this.setAttribute(matrixAttributeList , attributeDefinedList);
        for(MatrixAttributeVo matrixAttributeVo : matrixAttributeList){
            columnsMap.put(matrixAttributeVo.getLabel() , matrixAttributeVo.getUuid());
        }
    }
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
