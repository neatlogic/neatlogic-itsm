/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.fulltextindex;

import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerBase;
import codedriver.framework.fulltextindex.core.IFullTextIndexType;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexTypeVo;
import codedriver.framework.fulltextindex.dto.fulltextindex.FullTextIndexVo;
import codedriver.framework.fulltextindex.dto.globalsearch.DocumentVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
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
        fullTextIndexVo.addFieldContent("serial_number", new FullTextIndexVo.WordVo(processTaskVo.getSerialNumber()));
        //表单
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataVoList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(fullTextIndexVo.getTargetId());
        if (CollectionUtils.isNotEmpty(processTaskFormAttributeDataVoList)) {
            for (ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataVoList) {
                if (StringUtils.isNotBlank(attributeDataVo.getData())) {
                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
                    List<String> dataList = handler.indexFieldContentList(attributeDataVo.getData());
                    for (String data : dataList) {
                        fullTextIndexVo.addFieldContent(attributeDataVo.getAttributeUuid(), new FullTextIndexVo.WordVo(handler.isNeedSliceWord(), data));
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
