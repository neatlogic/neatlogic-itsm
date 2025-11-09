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

package neatlogic.module.process.notify.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.notify.core.NotifyPolicyHandlerBase;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.module.process.notify.constvalue.TimedTaskTriggerType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务通知策略处理器
 * @author: linbq
 * @since: 2021/4/8 18:15
 **/
@Service
public class TimedTaskNotifyPolicyHandler extends NotifyPolicyHandlerBase {
    @Override
    public String getName() {
        return "定时任务";
    }

    @Override
    protected List<NotifyTriggerVo> myNotifyTriggerList() {
        List<NotifyTriggerVo> returnList = new ArrayList<>();
        for (TimedTaskTriggerType type : TimedTaskTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(type));
        }
        return returnList;
    }

    @Override
    protected List<ConditionParamVo> mySystemParamList() {
        return null;
    }

    @Override
    protected List<ConditionParamVo> mySystemConditionOptionList() {
        return null;
    }

    @Override
    protected void myAuthorityConfig(JSONObject config) {

    }

    /**
     * 绑定权限，每种handler对应不同的权限
     */
    @Override
    public String getAuthName() {
        return null;
    }

    @Override
    public boolean isPublic(){
        return false;
    }
}
