/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.dto.UrlInfoVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import codedriver.framework.util.HtmlUtil;
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
public class ReasonParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.REASON.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        JSONObject paramObj = processTaskStepVo.getParamObj();
        if (MapUtils.isNotEmpty(paramObj)) {
            String reason = paramObj.getString("content");
            if (StringUtils.isNotBlank(reason)) {
                List<UrlInfoVo> urlInfoVoList = HtmlUtil.getUrlInfoList(reason, "<img src=\"", "\"");
                if (CollectionUtils.isNotEmpty(urlInfoVoList)) {
                    reason = HtmlUtil.urlReplace(reason, urlInfoVoList);
                    return reason;
                }
            }
        }
        return null;
    }
}
