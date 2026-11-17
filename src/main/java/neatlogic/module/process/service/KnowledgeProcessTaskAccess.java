package neatlogic.module.process.service;

import neatlogic.framework.knowledge.crossover.IKnowledgeProcessTaskAccess;
import neatlogic.framework.knowledge.dao.mapper.KnowledgeDocumentMapper;
import neatlogic.framework.knowledge.dto.KnowledgeDocumentInvokeVo;
import neatlogic.framework.knowledge.exception.KnowledgeDocumentRepeatInvokeException;
import neatlogic.framework.knowledge.generation.KnowledgeGenerationSources;
import neatlogic.framework.process.constvalue.KnowledgeSyncSource;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import org.springframework.stereotype.Service;

/** 为知识模块提供来源工单的查看权限校验。 */
@Service
public class KnowledgeProcessTaskAccess implements IKnowledgeProcessTaskAccess {
    @Resource private ProcessTaskMapper processTaskMapper;
    @Resource private KnowledgeDocumentMapper knowledgeDocumentMapper;

    /** 汇总关联前使用与工单详情相同的查看权限。 */
    @Override
    public Map<String, List<Long>> listOrigins(Long taskId) {
        new ProcessAuthManager.TaskOperationChecker(taskId, ProcessTaskOperationType.PROCESSTASK_VIEW)
                .build().checkAndNoPermissionThrowException();
        return KnowledgeGenerationSources.relatedOrigins(KnowledgeSyncSource.PROCESSTASK.getValue(), taskId);
    }

    /** 工单行锁串行化手动转换，随后通过当前读检查先前事务提交的关联。 */
    @Override
    public void checkConversion(Long taskId) {
        if (processTaskMapper.getProcessTaskLockById(taskId) == null) {
            throw new ProcessTaskNotFoundException(taskId);
        }
        KnowledgeDocumentInvokeVo invoke = new KnowledgeDocumentInvokeVo(taskId, KnowledgeSyncSource.PROCESSTASK.getValue());
        Long documentId = knowledgeDocumentMapper.lockDocumentIdByOrigin(invoke);
        if (documentId != null) {
            throw new KnowledgeDocumentRepeatInvokeException(invoke.getSource(), taskId, documentId);
        }
    }
}
