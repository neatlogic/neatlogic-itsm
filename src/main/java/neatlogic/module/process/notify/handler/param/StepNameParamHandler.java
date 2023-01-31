/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class StepNameParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.STEPNAME.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        String name = processTaskStepVo.getName();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        Long id = processTaskStepVo.getId();
        if (id != null) {
            ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(id);
            if (stepVo != null) {
                return stepVo.getName();
            }
        }
        JSONObject paramObj = processTaskStepVo.getParamObj();
        if (MapUtils.isNotEmpty(paramObj)) {
            JSONArray nameArray = paramObj.getJSONArray("nameList");
            if (CollectionUtils.isNotEmpty(nameArray)) {
                List<String> idList = nameArray.toJavaList(String.class);
                return String.join("、", idList);
            }
        }
        return null;
    }
}
