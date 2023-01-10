/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.matrix.handler;

import codedriver.framework.matrix.constvalue.MatrixAttributeType;
import codedriver.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import codedriver.framework.matrix.dto.MatrixAttributeVo;
import codedriver.framework.matrix.dto.MatrixDataVo;
import codedriver.framework.matrix.dto.MatrixFilterVo;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PrioritySearchVo;
import codedriver.framework.process.dto.PriorityVo;
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
                searchVo.setUuid(value);
            } else if ("dc1c6903648e417f88d52ad986b057a0".equals(uuid)) {
                searchVo.setName(value);
            }
        }
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
