package neatlogic.module.process.importexport.handler;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.importexport.core.ImportExportHandlerBase;
import neatlogic.framework.importexport.core.ImportExportHandlerType;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportPrimaryChangeVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.process.constvalue.ProcessImportExportHandlerType;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.process.exception.processtask.task.TaskConfigNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Component
public class SubtaskPolicyImportExportHandler extends ImportExportHandlerBase {

    @Resource
    TaskMapper taskMapper;

    @Override
    public ImportExportHandlerType getType() {
        return ProcessImportExportHandlerType.SUBTASK_POLICY;
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
        return taskMapper.getTaskConfigByName(importExportBaseInfoVo.getName()) != null;
    }

    @Override
    public Object getPrimaryByName(ImportExportVo importExportVo) {
        TaskConfigVo taskConfig = taskMapper.getTaskConfigByName(importExportVo.getName());
        if (taskConfig == null) {
            throw new TaskConfigNotFoundException(importExportVo.getName());
        }
        return taskConfig.getId();
    }

    @Override
    public Object importData(ImportExportVo importExportVo, List<ImportExportPrimaryChangeVo> primaryChangeList) {
        TaskConfigVo taskConfig = importExportVo.getData().toJavaObject(TaskConfigVo.class);
        TaskConfigVo oldTaskConfig = taskMapper.getTaskConfigByName(taskConfig.getName());
        if (oldTaskConfig != null) {
            taskConfig.setId(oldTaskConfig.getId());
            taskConfig.setLcu(UserContext.get().getUserUuid());
            taskMapper.updateTaskConfig(taskConfig);
        } else {
            if (taskMapper.getTaskConfigById(taskConfig.getId()) != null) {
                taskConfig.setId(null);
            }
            taskConfig.setFcu(UserContext.get().getUserUuid());
            taskMapper.insertTaskConfig(taskConfig);
        }
        return taskConfig.getId();
    }

    @Override
    protected ImportExportVo myExportData(Object primaryKey, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream) {
        Long id = (Long) primaryKey;
        TaskConfigVo taskConfig = taskMapper.getTaskConfigById(id);
        if(taskConfig == null){
            throw new TaskConfigNotFoundException(id.toString());
        }
        ImportExportVo importExportVo = new ImportExportVo(this.getType().getValue(), primaryKey, taskConfig.getName());
        importExportVo.setDataWithObject(taskConfig);
        return importExportVo;
    }
}
