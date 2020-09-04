package codedriver.module.process.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.util.UUIDUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:07
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixAttributeSaveApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/save";
    }

    @Override
    public String getName() {
        return "矩阵属性保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param( name = "matrixAttributeList", desc = "属性数据列表", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param( name = "matrixAttributeList[x].uuid", desc = "属性uuid", type = ApiParamType.STRING),
        @Param( name = "matrixAttributeList[x].name", desc = "属性名", type = ApiParamType.STRING),
        @Param( name = "matrixAttributeList[x].type", desc = "类型", type = ApiParamType.STRING),
        @Param( name = "matrixAttributeList[x].isRequired", desc = "是否必填", type = ApiParamType.ENUM, rule = "0,1"),
        @Param( name = "matrixAttributeList[x].sort", desc = "排序", type = ApiParamType.INTEGER),
        @Param( name = "matrixAttributeList[x].config", desc = "配置信息", type = ApiParamType.JSONOBJECT)
    })
    @Description( desc = "矩阵属性保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
    	if(matrixVo == null) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
    	if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
    		List<ProcessMatrixAttributeVo> attributeVoList = JSON.parseArray(jsonObj.getString("matrixAttributeList"), ProcessMatrixAttributeVo.class);
        	List<ProcessMatrixAttributeVo> oldMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
            boolean dataExist = CollectionUtils.isNotEmpty(oldMatrixAttributeList);
            if (dataExist){
                attributeMapper.deleteAttributeByMatrixUuid(matrixUuid);
            }
            if (CollectionUtils.isNotEmpty(attributeVoList)){
                //有数据
                if (dataExist){
                    //数据对比
                    //删除数据
                    //调整表
                	List<String> oldAttributeUuidList = oldMatrixAttributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
                    List<String> addAttributeUuidList = new ArrayList<>();
                    List<String> existedAttributeUuidList = new ArrayList<>();
                    for(ProcessMatrixAttributeVo attributeVo : attributeVoList) {
                    	attributeVo.setMatrixUuid(matrixUuid);
                    	if (oldAttributeUuidList.contains(attributeVo.getUuid())){
                            attributeMapper.insertMatrixAttribute(attributeVo);
                            existedAttributeUuidList.add(attributeVo.getUuid());
                        }else {
                        	//过滤新增属性uuid
                            attributeMapper.insertMatrixAttribute(attributeVo);
                            addAttributeUuidList.add(attributeVo.getUuid());
                        }
                    }
                    
                    //添加新增字段
                    for(String attributeUuid : addAttributeUuidList) {
                    	attributeMapper.addMatrixDynamicTableColumn(attributeUuid, matrixUuid);
                    }
                    //找出需要删除的属性uuid列表
                    oldAttributeUuidList.removeAll(existedAttributeUuidList);
                    for(String attributeUuid : oldAttributeUuidList) {
                    	attributeMapper.dropMatrixDynamicTableColumn(attributeUuid, matrixUuid);
                    }
                }else {
                    for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                    	attributeVo.setMatrixUuid(matrixUuid);
                        attributeVo.setUuid(UUIDUtil.getUUID());
                        attributeMapper.insertMatrixAttribute(attributeVo);
                    }
                    attributeMapper.createMatrixDynamicTable(attributeVoList, matrixUuid);
                }
            }else {
                //无数据
                if (dataExist){
                    // 删除动态表
                    attributeMapper.dropMatrixDynamicTable(matrixUuid);
                }
            }
    	}else {
    		throw new MatrixExternalException("矩阵外部数据源没有保存属性操作");
    	}
    	
        return null;
    }
}
