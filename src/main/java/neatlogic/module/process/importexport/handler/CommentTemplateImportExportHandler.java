package neatlogic.module.process.importexport.handler;

import neatlogic.framework.importexport.core.ImportExportHandlerBase;
import neatlogic.framework.importexport.core.ImportExportHandlerType;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportPrimaryChangeVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.process.constvalue.ProcessImportExportHandlerType;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import neatlogic.module.process.service.ProcessCommentTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Component
public class CommentTemplateImportExportHandler extends ImportExportHandlerBase {

    @Resource
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Resource
    private ProcessCommentTemplateService processCommentTemplateService;

    @Override
    public ImportExportHandlerType getType() {
        return ProcessImportExportHandlerType.COMMENT_TEMPLATE;
    }

    @Override
    public boolean checkImportAuth(ImportExportVo importExportVo) {
        return true;
    }

    @Override
    public boolean checkExportAuth(Object primaryKey) {
        return true;
    }

    @Override
    public boolean checkIsExists(ImportExportBaseInfoVo importExportBaseInfoVo) {
        return commentTemplateMapper.getTemplateByName(importExportBaseInfoVo.getName()) != null;
    }

    @Override
    public Object getPrimaryByName(ImportExportVo importExportVo) {
        ProcessCommentTemplateVo template = commentTemplateMapper.getTemplateByName(importExportVo.getName());
        if (template == null) {
            throw new ProcessCommentTemplateNotFoundException(importExportVo.getName());
        }
        return template.getId();
    }

    @Override
    public Object importData(ImportExportVo importExportVo, List<ImportExportPrimaryChangeVo> primaryChangeList) {
        ProcessCommentTemplateVo template = importExportVo.getData().toJavaObject(ProcessCommentTemplateVo.class);
        ProcessCommentTemplateVo oldTemplate = commentTemplateMapper.getTemplateByName(template.getName());
        if (oldTemplate != null) {
            template.setId(oldTemplate.getId());
        } else {
            if (commentTemplateMapper.getTemplateById(template.getId()) != null) {
                template.setId(null);
            }
        }
        processCommentTemplateService.saveTemplate(template);
        return template.getId();
    }

    @Override
    protected ImportExportVo myExportData(Object primaryKey, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream) {
        Long id = (Long) primaryKey;
        ProcessCommentTemplateVo template = processCommentTemplateService.getTemplateById(id);
        if(template == null){
            throw new ProcessCommentTemplateNotFoundException(id);
        }
        ImportExportVo importExportVo = new ImportExportVo(this.getType().getValue(), primaryKey, template.getName());
        importExportVo.setDataWithObject(template);
        return importExportVo;
    }
}
