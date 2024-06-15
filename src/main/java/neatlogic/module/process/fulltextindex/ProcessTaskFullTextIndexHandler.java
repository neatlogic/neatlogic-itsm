/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.fulltextindex;

import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerBase;
import neatlogic.framework.fulltextindex.core.IFullTextIndexType;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import neatlogic.framework.fulltextindex.dto.fulltextindex.FullTextIndexVo;
import neatlogic.framework.fulltextindex.dto.globalsearch.DocumentVo;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
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
                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getHandler());
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
        fullTextIndexTypeVo.setPageSize(100);
        List<Long> processTaskIdList = processTaskMapper.getNotIndexProcessTaskIdList(fullTextIndexTypeVo);
        while (CollectionUtils.isNotEmpty(processTaskIdList)) {
            for (Long processTaskId : processTaskIdList) {
                this.createIndex(processTaskId, true);
            }
            processTaskIdList = processTaskMapper.getNotIndexProcessTaskIdList(fullTextIndexTypeVo);
        }
    }
}
