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

package neatlogic.module.process.fulltextindex;

import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerBase;
import neatlogic.framework.fulltextindex.core.IFullTextIndexType;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexVo;
import neatlogic.framework.fulltextindex.dto.globalsearch.DocumentVo;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 创建工单分词索引
 *
 * @author lvzk
 * @since 2021/03/23
 */
@Service
public class ProcessTaskFullTextIndexHandler extends FullTextIndexHandlerBase {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    protected String getModuleId() {
        return "process";
    }

    @Override
    protected void myCreateIndex(FullTextIndexVo fullTextIndexVo) {
        //上报内容
        ProcessTaskContentVo startContentVo = null;
        ProcessTaskStepVo startStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(fullTextIndexVo.getTargetId());
        if (startStepVo != null) {
            List<ProcessTaskStepContentVo> processTaskStepContentList =
                    processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startStepVo.getId());
            for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
                if (ProcessTaskOperationType.PROCESSTASK_START.getValue().equals(processTaskStepContent.getType())) {
                    ProcessTaskContentVo processTaskContentVo = selectContentByHashMapper.getProcessTaskContentByHash(processTaskStepContent.getContentHash());
                    if (processTaskContentVo != null) {
                        fullTextIndexVo.addFieldContent("content", new FullTextIndexVo.WordVo(processTaskContentVo.getContent()));
                    }
                    break;
                }
            }
        }
        //标题、工单id
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(fullTextIndexVo.getTargetId());
        fullTextIndexVo.addFieldContent("title", new FullTextIndexVo.WordVo(processTaskVo.getTitle()));
        //fullTextIndexVo.addFieldContent("serial_number", new FullTextIndexVo.WordVo(processTaskVo.getSerialNumber()));
        //fullTextIndexVo.addFieldContent("id", new FullTextIndexVo.WordVo(processTaskVo.getId().toString()));
        //表单
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataVoList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(fullTextIndexVo.getTargetId());
        if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataVoList)) {
            for (ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataVoList) {
                if (StringUtils.isNotBlank(attributeDataVo.getData())) {
                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
                    if(handler != null) {
                        List<String> dataList = handler.indexFieldContentList(attributeDataVo.getData());
                        if (CollectionUtils.isNotEmpty(dataList)) {
                            for (String data : dataList) {
                                fullTextIndexVo.addFieldContent(attributeDataVo.getAttributeLabel(), new FullTextIndexVo.WordVo(handler.isNeedSliceWord(), data));//target_field 改为用表单的label，为了兼容换表单控件，只要名字相同也支持搜索
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void myMakeupDocument(DocumentVo documentVo) {

    }

    @Override
    public IFullTextIndexType getType() {
        return ProcessFullTextIndexType.PROCESSTASK;
    }

    @Override
    public void myRebuildIndex(FullTextIndexTypeVo fullTextIndexTypeVo) {

    }
}
