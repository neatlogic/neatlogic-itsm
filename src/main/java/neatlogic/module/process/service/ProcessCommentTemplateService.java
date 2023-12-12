package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessCommentTemplateVo;

public interface ProcessCommentTemplateService {

    ProcessCommentTemplateVo getTemplateById(Long id);

    void saveTemplate(ProcessCommentTemplateVo template);
}
