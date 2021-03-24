package codedriver.module.process.fulltextindex;

import codedriver.framework.fulltextindex.core.FullTextIndexHandlerBase;
import codedriver.framework.fulltextindex.core.IFullTextIndexType;
import codedriver.framework.fulltextindex.dto.FullTextIndexVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
import codedriver.framework.process.fulltextindex.FullTextIndexType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 创建工单分词索引
 * @author lvzk
 * @since 2021/03/23
 */
@Service
public class ProcessTaskFormFullTextIndexHandler extends FullTextIndexHandlerBase {
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
        //表单
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataVoList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(fullTextIndexVo.getTargetId());
        if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataVoList)){
            for (ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataVoList){
                if(StringUtils.isNotBlank(attributeDataVo.getData())) {
                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
                    List<String> dataList = handler.indexFieldContentList(attributeDataVo.getData());
                    for (String data : dataList) {
                        fullTextIndexVo.addFieldContent(attributeDataVo.getAttributeUuid(),  new FullTextIndexVo.WordVo(handler.isNeedSliceWord(),data));
                    }
                }
            }
        }
    }

    @Override
    public IFullTextIndexType getType() {
        return FullTextIndexType.PROCESSTASK_FORM;
    }

    @Override
    public void rebuildIndex(Boolean isRebuildAll) {

    }
}
