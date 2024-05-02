package neatlogic.module.process.importexport.handler;

import neatlogic.framework.importexport.core.ImportExportHandlerBase;
import neatlogic.framework.importexport.core.ImportExportHandlerType;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportPrimaryChangeVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.process.constvalue.ProcessImportExportHandlerType;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.score.ScoreTemplateVo;
import neatlogic.framework.process.exception.score.ScoreTemplateNotFoundException;
import neatlogic.module.process.service.ScoreTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Component
public class ScoreTemplateImportExportHandler extends ImportExportHandlerBase {

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Resource
    private ScoreTemplateService scoreTemplateService;

    @Override
    public ImportExportHandlerType getType() {
        return ProcessImportExportHandlerType.SCORE_TEMPLATE;
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
        return scoreTemplateMapper.getScoreTemplateByName(importExportBaseInfoVo.getName()) != null;
    }

    @Override
    public Object getPrimaryByName(ImportExportVo importExportVo) {
        ScoreTemplateVo template = scoreTemplateMapper.getScoreTemplateByName(importExportVo.getName());
        if (template == null) {
            throw new ScoreTemplateNotFoundException(importExportVo.getName());
        }
        return template.getId();
    }

    @Override
    public Object importData(ImportExportVo importExportVo, List<ImportExportPrimaryChangeVo> primaryChangeList) {
        ScoreTemplateVo template = importExportVo.getData().toJavaObject(ScoreTemplateVo.class);
        ScoreTemplateVo oldTemplate = scoreTemplateMapper.getScoreTemplateByName(importExportVo.getName());
        if (oldTemplate != null) {
            template.setId(oldTemplate.getId());
        } else {
            if (scoreTemplateMapper.checkScoreTemplateExistsById(template.getId()) != null) {
                template.setId(null);
            }
        }
        scoreTemplateService.saveScoreTemplate(template);
        return template.getId();
    }

    @Override
    protected ImportExportVo myExportData(Object primaryKey, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream) {
        Long id = (Long) primaryKey;
        ScoreTemplateVo template = scoreTemplateMapper.getScoreTemplateById(id);
        if(template == null) {
            throw new ScoreTemplateNotFoundException(id);
        }
        ImportExportVo importExportVo = new ImportExportVo(this.getType().getValue(), primaryKey, template.getName());
        importExportVo.setDataWithObject(template);
        return importExportVo;
    }
}
