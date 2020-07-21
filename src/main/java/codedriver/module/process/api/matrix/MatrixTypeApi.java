package codedriver.module.process.api.matrix;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 16:52
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixTypeApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "matrix/type";
    }

    @Override
    public String getName() {
        return "矩阵类型返回接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({ @Param( name = "typeList", desc = "矩阵类型返回列表", explode = ValueTextVo[].class)})
    @Description(desc = "矩阵类型返回接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<ValueTextVo> typeList = new ArrayList<>();
        for (ProcessMatrixType type : ProcessMatrixType.values()){
        	typeList.add(new ValueTextVo(type.getValue(), type.getName()));
        }
        returnObj.put("typeList", typeList);
        return returnObj;
    }
}
