/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.audithandler.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FormAttributeListAuditHandler implements IProcessTaskStepAuditDetailHandler {
    @Override
    public String getType() {
        return ProcessTaskAuditDetailType.FORM_ATTRIBUTE_LIST.getValue();
    }

    @Override
    public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
        String newContent = processTaskStepAuditDetailVo.getNewContent();
        if (StringUtils.isBlank(newContent)) {
            processTaskStepAuditDetailVo.setNewContent(null);
            return 0;
        }
        JSONArray jsonArray = JSON.parseArray(newContent);
        if (CollectionUtils.isEmpty(jsonArray)) {
            processTaskStepAuditDetailVo.setNewContent(null);
            return 0;
        }
        return 1;
    }
}
