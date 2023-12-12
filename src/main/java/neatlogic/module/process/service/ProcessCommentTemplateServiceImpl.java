package neatlogic.module.process.service;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateAuthVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.process.exception.commenttemplate.ProcessCommentTemplateNameRepeatException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessCommentTemplateServiceImpl implements ProcessCommentTemplateService{

    @Resource
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public ProcessCommentTemplateVo getTemplateById(Long id) {
        ProcessCommentTemplateVo vo = commentTemplateMapper.getTemplateById(id);
        if(vo == null){
            return null;
        }
        if (ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(vo.getType())) {
            List<String> authList = new ArrayList<>();
            List<ProcessCommentTemplateAuthVo> authVoList = commentTemplateMapper.getProcessCommentTemplateAuthListByCommentTemplateId(id);
            for (ProcessCommentTemplateAuthVo authVo : authVoList) {
                authList.add(authVo.getType() + "#" + authVo.getUuid());
            }
            vo.setAuthList(authList);
        }
        return vo;
    }

    @Override
    public void saveTemplate(ProcessCommentTemplateVo template) {
        if (commentTemplateMapper.checkTemplateNameIsRepeat(template) > 0) {
            throw new ProcessCommentTemplateNameRepeatException(template.getName());
        }
        ProcessCommentTemplateVo oldTemplate = commentTemplateMapper.getTemplateById(template.getId());
        if (oldTemplate != null) {
            template.setType(oldTemplate.getType());
            template.setLcu(UserContext.get().getUserUuid(true));
            commentTemplateMapper.updateTemplate(template);
            commentTemplateMapper.deleteTemplateAuthority(template.getId());
        } else {
            template.setFcu(UserContext.get().getUserUuid(true));
            commentTemplateMapper.insertTemplate(template);
        }
        List<ProcessCommentTemplateAuthVo> list = new ArrayList<>();
        List<String> authList = template.getAuthList();
        if (ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(template.getType()) && CollectionUtils.isNotEmpty(authList)) {
            for (String auth : authList) {
                String[] split = auth.split("#");
                if (GroupSearch.getGroupSearch(split[0]) != null) {
                    ProcessCommentTemplateAuthVo authVo = new ProcessCommentTemplateAuthVo();
                    authVo.setCommentTemplateId(template.getId());
                    authVo.setType(split[0]);
                    authVo.setUuid(split[1]);
                    list.add(authVo);
                }
            }
        } else if (ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(template.getType())) {
            ProcessCommentTemplateAuthVo auth = new ProcessCommentTemplateAuthVo();
            auth.setCommentTemplateId(template.getId());
            auth.setType(GroupSearch.USER.getValue());
            auth.setUuid(UserContext.get().getUserUuid());
            list.add(auth);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            commentTemplateMapper.batchInsertAuthority(list);
        }
    }
}
