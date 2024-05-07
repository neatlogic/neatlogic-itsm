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

package neatlogic.module.process.matrix.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixFilterVo;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskTeamTagUserTypeMatrixPrivateDataSourceHandler implements IMatrixPrivateDataSourceHandler {

    private final List<MatrixAttributeVo> matrixAttributeList = new ArrayList<>();
    private final Map<String , String> columnsMap = new HashMap<>();
    {
        JSONArray attributeDefinedList = new JSONArray();
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "名称");
            jsonObj.put("label", "text");
            jsonObj.put("isPrimaryKey", 0);
            jsonObj.put("isSearchable", 1);
            attributeDefinedList.add(jsonObj);
        }
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "值");
            jsonObj.put("label", "value");
            jsonObj.put("isPrimaryKey", 1);
            jsonObj.put("isSearchable", 1);
            attributeDefinedList.add(jsonObj);
        }
        this.setAttribute(matrixAttributeList , attributeDefinedList);
        for(MatrixAttributeVo matrixAttributeVo : matrixAttributeList){
            columnsMap.put(matrixAttributeVo.getLabel() , matrixAttributeVo.getUuid());
        }
    }
    @Override
    public String getUuid() {
        return UuidUtil.getCustomUUID(getLabel());
    }

    @Override
    public String getName() {
        return "工单分组标签用户类型";
    }

    @Override
    public String getLabel() {
        return "ProcessTaskTeamTagUserType";
    }

    @Override
    public List<MatrixAttributeVo> getAttributeList() {
        return matrixAttributeList;
    }

    @Override
    public List<Map<String, String>> searchTableData(MatrixDataVo dataVo) {
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        List<Map<String, String>> dataList = new ArrayList<>();

        List<ProcessUserType> processUserTypes = Arrays.asList(ProcessUserType.OWNER,ProcessUserType.WORKER);
        if (CollectionUtils.isEmpty(filterList)) {
            for (ProcessUserType type : processUserTypes) {
                Map<String, String> data = new HashMap<>();
                data.put("uuid", UuidUtil.getCustomUUID(getLabel() + type.getValue()));
                data.put(columnsMap.get("value"), type.getValue());
                data.put(columnsMap.get("text"), type.getText());
                dataList.add(data);
            }
        }else{
            for (ProcessUserType type : ProcessUserType.values()) {
                if(checkFilter(type , filterList)){
                    Map<String, String> data = new HashMap<>();
                    data.put("uuid", UuidUtil.getCustomUUID(getLabel() + type.getValue()));
                    data.put(columnsMap.get("value"), type.getValue());
                    data.put(columnsMap.get("text"), type.getText());
                    dataList.add(data);
                }
            }
        }
        return dataList;
    }

    private Boolean checkFilter(ProcessUserType type , List<MatrixFilterVo> filterList){
        boolean isContain = false ;
        flag:for (MatrixFilterVo filter : filterList) {
            for(String value:filter.getValueList()){
                if(type.getText().contains(value) || type.getValue().contains(value)){
                    isContain = true ;
                    break flag;
                }
            }
        }
        return isContain;
    }
}
