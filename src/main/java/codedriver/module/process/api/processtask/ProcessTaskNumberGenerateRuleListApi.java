package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskNumberGenerateRule;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskNumberGenerateRuleListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/numbergeneraterule/list";
    }

    @Override
    public String getName() {
        return "查询工单号生成规则列表";
    }

    @Override
    public String getConfig() {
        return null;
    }
    
    @Output({
        @Param(explode = ValueTextVo[].class, desc = "工单号生成规则列表")
    })
    @Description(desc = "查询工单号生成规则列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ValueTextVo> resultList = new ArrayList<>();
        for(ProcessTaskNumberGenerateRule rule : ProcessTaskNumberGenerateRule.values()) {
            resultList.add(new ValueTextVo(rule.getValue(), rule.getText()));
        }
        return resultList;
    }

}
