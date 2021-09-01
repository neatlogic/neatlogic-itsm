package codedriver.module.process.fulltextindex;

import codedriver.framework.fulltextindex.core.FullTextIndexHandlerBase;
import codedriver.framework.fulltextindex.core.IFullTextIndexType;
import codedriver.framework.fulltextindex.dto.FullTextIndexVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 创建工单分词索引
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
                    if(processTaskContentVo != null) {
                        fullTextIndexVo.addFieldContent("content", new FullTextIndexVo.WordVo(processTaskContentVo.getContent()));
                    }
                    break;
                }
            }
        }
        //标题、工单id
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(fullTextIndexVo.getTargetId());
        fullTextIndexVo.addFieldContent("title",new FullTextIndexVo.WordVo(processTaskVo.getTitle()));
        fullTextIndexVo.addFieldContent("serial_number",new FullTextIndexVo.WordVo(processTaskVo.getSerialNumber()));
    }

    @Override
    public IFullTextIndexType getType() {
        return ProcessFullTextIndexType.PROCESSTASK;
    }

    @Override
    public void rebuildIndex(Boolean isRebuildAll) {

    }
}
