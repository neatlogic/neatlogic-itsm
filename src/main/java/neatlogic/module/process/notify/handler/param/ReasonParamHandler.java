/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.notify.handler.param;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.MailServerVo;
import neatlogic.framework.dto.UrlInfoVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.core.NotifyHandlerType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.HtmlUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class ReasonParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.REASON.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        JSONObject paramObj = processTaskStepVo.getParamObj();
        if (MapUtils.isNotEmpty(paramObj)) {
            String content = paramObj.getString("content");
            if (StringUtils.isNotBlank(content)) {
                content = content.replace("<p>", "");
                content = content.replace("</p>", "");
                content = content.replace("<br>", "");
                List<UrlInfoVo> urlInfoVoList = HtmlUtil.getUrlInfoList(content, "<img src=\"", "\"");
                String homeUrl = "";
                String config = notifyConfigMapper.getConfigByType(NotifyHandlerType.EMAIL.getValue());
                if (StringUtils.isNotBlank(config)) {
                    MailServerVo mailServerVo = JSONObject.parseObject(config, MailServerVo.class);
                    if (mailServerVo != null) {
                        homeUrl = mailServerVo.getHomeUrl();
                        if (StringUtils.isBlank(homeUrl)) {
                            homeUrl = "";
                        }
                    }
                }
                content = HtmlUtil.urlReplace(content, urlInfoVoList, homeUrl);
            }
            return content;
        }
        return null;
    }
}
