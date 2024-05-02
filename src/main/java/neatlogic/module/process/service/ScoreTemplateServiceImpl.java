package neatlogic.module.process.service;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.score.ScoreTemplateDimensionVo;
import neatlogic.framework.process.dto.score.ScoreTemplateVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScoreTemplateServiceImpl implements ScoreTemplateService {

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public void saveScoreTemplate(ScoreTemplateVo template) {
        ScoreTemplateVo oldTemplate = scoreTemplateMapper.checkScoreTemplateExistsById(template.getId());
        if (oldTemplate != null){
            template.setLcu(UserContext.get().getUserUuid(true));
            scoreTemplateMapper.updateScoreTemplate(template);
            scoreTemplateMapper.deleteScoreTemplateDimension(template.getId());
        }else {
            template.setFcu(UserContext.get().getUserUuid(true));
            scoreTemplateMapper.insertScoreTemplate(template);
        }
        List<ScoreTemplateDimensionVo> dimensionList = template.getDimensionList();
        for(ScoreTemplateDimensionVo vo : dimensionList){
            vo.setScoreTemplateId(template.getId());
            vo.setId(null);
            scoreTemplateMapper.insertScoreTemplateDimension(vo);
        }
    }
}
