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

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepTagVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class TagListMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTagMapper processTagMapper;
    @Override
    public String getName() {
        return "tagList";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        JSONArray tagList = stepConfigObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagList)) {
            if (Objects.equals(action, "save")) {
                List<String> tagNameList = tagList.toJavaList(String.class);
                if (CollectionUtils.isNotEmpty(tagNameList)) {
                    ProcessStepTagVo processStepTagVo = new ProcessStepTagVo();
                    processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                    processStepTagVo.setProcessStepUuid(processStepVo.getUuid());
                    List<ProcessTagVo> processTagList = processTagMapper.getProcessTagByNameList(tagNameList);
                    for (ProcessTagVo processTagVo : processTagList) {
                        processStepTagVo.setTagId(processTagVo.getId());
                        tagNameList.remove(processTagVo.getName());
                        processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                        processMapper.insertProcessStepTag(processStepTagVo);
                    }
                    if (CollectionUtils.isNotEmpty(tagNameList)) {
                        for (String tagName : tagNameList) {
                            ProcessTagVo processTagVo = new ProcessTagVo(tagName);
                            processTagMapper.insertProcessTag(processTagVo);
                            processStepTagVo.setTagId(processTagVo.getId());
                            processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                            processMapper.insertProcessStepTag(processStepTagVo);
                        }
                    }
                }
            } else if (Objects.equals(action, "delete")) {
                processMapper.deleteProcessStepTagByProcessStepUuid(processStepVo.getUuid());
            }
        }
    }
}
