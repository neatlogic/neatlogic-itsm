package neatlogic.module.process.importexport.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.importexport.constvalue.FrameworkImportExportHandlerType;
import neatlogic.framework.importexport.core.ImportExportHandler;
import neatlogic.framework.importexport.core.ImportExportHandlerBase;
import neatlogic.framework.importexport.core.ImportExportHandlerFactory;
import neatlogic.framework.importexport.core.ImportExportHandlerType;
import neatlogic.framework.importexport.dto.ImportExportBaseInfoVo;
import neatlogic.framework.importexport.dto.ImportExportPrimaryChangeVo;
import neatlogic.framework.importexport.dto.ImportExportVo;
import neatlogic.framework.importexport.exception.DependencyNotFoundException;
import neatlogic.framework.importexport.exception.ImportExportHandlerNotFoundException;
import neatlogic.framework.process.constvalue.ProcessImportExportHandlerType;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import neatlogic.module.process.service.ProcessService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Component
public class ProcessImportExportHandler extends ImportExportHandlerBase {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessService processService;

    @Override
    public ImportExportHandlerType getType() {
        return ProcessImportExportHandlerType.PROCESS;
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
        return processMapper.getProcessByName(importExportBaseInfoVo.getName()) != null;
    }

    @Override
    public Object getPrimaryByName(ImportExportVo importExportVo) {
        ProcessVo process = processMapper.getProcessByName(importExportVo.getName());
        if (process == null) {
            throw new ProcessNotFoundException(importExportVo.getName());
        }
        return process.getUuid();
    }

    @Override
    public Object importData(ImportExportVo importExportVo, List<ImportExportPrimaryChangeVo> primaryChangeList) {
        ProcessVo process = importExportVo.getData().toJavaObject(ProcessVo.class);
        ProcessVo oldProcess = processMapper.getProcessByName(importExportVo.getName());
        boolean isChangeUuid = false;
        if (oldProcess != null) {
            process.setUuid(oldProcess.getUuid());
        } else {
            if (processMapper.getProcessByUuid(process.getUuid()) != null) {
                process.setUuid(null);
                isChangeUuid = true;
            }
        }
        importHandle(process, primaryChangeList, isChangeUuid);
        process.makeupConfigObj();
        processService.saveProcess(process);
        return process.getUuid();
    }

    /**
     * 导入处理，更新依赖组件的唯一标识
     * @param process
     * @param primaryChangeList
     * @param isChangeUuid
     */
    private void importHandle(ProcessVo process, List<ImportExportPrimaryChangeVo> primaryChangeList, boolean isChangeUuid) {
        dependencyHandle(IMPORT, process, null, null, primaryChangeList, isChangeUuid);
    }

    @Override
    protected ImportExportVo myExportData(Object primaryKey, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream) {
        String uuid = (String) primaryKey;
        ProcessVo process = processMapper.getProcessByUuid(uuid);
        if (process == null) {
            throw new ProcessNotFoundException(uuid);
        }
        exportHandle(process, dependencyList, zipOutputStream);
        ImportExportVo importExportVo = new ImportExportVo(this.getType().getValue(), primaryKey, process.getName());
        importExportVo.setDataWithObject(process);
        return importExportVo;
    }

    /**
     * 导出处理，先导出依赖组件
     * @param process
     * @param dependencyList
     * @param zipOutputStream
     */
    private void exportHandle(ProcessVo process, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream) {
        dependencyHandle(EXPORT, process, dependencyList, zipOutputStream, null, false);
    }

    /**
     * 导出处理，先导出依赖组件
     * 导入处理，更新依赖组件的唯一标识
     * @param action
     * @param process
     * @param dependencyList
     * @param zipOutputStream
     * @param primaryChangeList
     * @param isChangeUuid
     */
    private void dependencyHandle(String action, ProcessVo process, List<ImportExportBaseInfoVo> dependencyList, ZipOutputStream zipOutputStream, List<ImportExportPrimaryChangeVo> primaryChangeList, boolean isChangeUuid) {
        Map<String, String> oldUuid2NewUuidMap = new HashMap<>();
        JSONObject config = process.getConfig();
        JSONObject processObj = config.getJSONObject("process");
        JSONArray slaList = processObj.getJSONArray("slaList");
        if (CollectionUtils.isNotEmpty(slaList)) {
            // 通知策略
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject slaObj = slaList.getJSONObject(i);
                if (MapUtils.isEmpty(slaObj)) {
                    continue;
                }
                if (isChangeUuid) {
                    String oldUuid = slaObj.getString("uuid");
                    String newUuid = UuidUtil.randomUuid();
                    slaObj.put("uuid", newUuid);
                    oldUuid2NewUuidMap.put(oldUuid, newUuid);
                }
                JSONArray notifyPolicyList = slaObj.getJSONArray("notifyPolicyList");
                if (CollectionUtils.isEmpty(notifyPolicyList)) {
                    continue;
                }
                for (int j = 0; j < notifyPolicyList.size(); j++) {
                    JSONObject notifyPolicyObj = notifyPolicyList.getJSONObject(j);
                    if (MapUtils.isEmpty(notifyPolicyObj)) {
                        continue;
                    }
                    JSONObject notifyPolicyConfig = notifyPolicyObj.getJSONObject("notifyPolicyConfig");
                    if (MapUtils.isEmpty(notifyPolicyConfig)) {
                        continue;
                    }
                    Long policyId = notifyPolicyConfig.getLong("policyId");
                    if (policyId == null) {
                        continue;
                    }
                    Integer isCustom = notifyPolicyConfig.getInteger("isCustom");
                    if (!Objects.equals(isCustom, 1)) {
                        continue;
                    }
                    if (action == IMPORT) {
                        Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, primaryChangeList);
                        if (newPrimaryKey != null) {
                            notifyPolicyConfig.put("policyId", newPrimaryKey);
                        }
                    } else if (action == EXPORT) {
                        doExportData(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, dependencyList, zipOutputStream);
                    }
                }
            }
        }
        JSONObject formConfig = processObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfig)) {
            // 表单
            String formUuid = formConfig.getString("uuid");
            if (StringUtils.isNotBlank(formUuid)) {
                if (action == IMPORT) {
                    Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.FORM, formUuid, primaryChangeList);
                    if (newPrimaryKey != null) {
                        formConfig.put("uuid", newPrimaryKey);
                    }
                } else if (action == EXPORT) {
                    doExportData(FrameworkImportExportHandlerType.FORM, formUuid, dependencyList, zipOutputStream);
                }
            }
        }
        JSONObject processConfig = processObj.getJSONObject("processConfig");
        if (MapUtils.isNotEmpty(processConfig)) {
            if (isChangeUuid) {
                String oldUuid = processConfig.getString("uuid");
                String newUuid = process.getUuid();
                processConfig.put("uuid", newUuid);
                oldUuid2NewUuidMap.put(oldUuid, newUuid);
            }
            // 通知策略
            // 集成
            JSONObject notifyPolicyConfig = processConfig.getJSONObject("notifyPolicyConfig");
            if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                Integer isCustom = notifyPolicyConfig.getInteger("isCustom");
                Long policyId = notifyPolicyConfig.getLong("policyId");
                if (Objects.equals(isCustom, 1) && policyId != null) {
                    if (action == IMPORT) {
                        Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, primaryChangeList);
                        if (newPrimaryKey != null) {
                            notifyPolicyConfig.put("policyId", newPrimaryKey);
                        }
                    } else if (action == EXPORT) {
                        doExportData(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, dependencyList, zipOutputStream);
                    }
                }
            }
            JSONObject actionConfig = processConfig.getJSONObject("actionConfig");
            if (MapUtils.isNotEmpty(actionConfig)) {
                JSONArray actionList = actionConfig.getJSONArray("actionList");
                if (CollectionUtils.isNotEmpty(actionList)) {
                    for (int i = 0; i < actionList.size(); i++) {
                        JSONObject actionObj = actionList.getJSONObject(i);
                        if (MapUtils.isEmpty(actionObj)) {
                            continue;
                        }
                        String integrationUuid = actionObj.getString("integrationUuid");
                        if (StringUtils.isBlank(integrationUuid)) {
                            continue;
                        }
                        if (action == IMPORT) {
                            Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, primaryChangeList);
                            if (newPrimaryKey != null) {
                                actionObj.put("integrationUuid", newPrimaryKey);
                            }
                        } else if (action == EXPORT) {
                            doExportData(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, dependencyList, zipOutputStream);
                        }
                    }
                }
            }
        }
        JSONObject scoreConfig = processObj.getJSONObject("scoreConfig");
        if (MapUtils.isNotEmpty(scoreConfig)) {
            // 评分模板
            Long scoreTemplateId = scoreConfig.getLong("scoreTemplateId");
            if (scoreTemplateId != null) {
                if (action == IMPORT) {
                    Object newPrimaryKey = getNewPrimaryKey(ProcessImportExportHandlerType.SCORE_TEMPLATE, scoreTemplateId, primaryChangeList);
                    if (newPrimaryKey != null) {
                        scoreConfig.put("scoreTemplateId", newPrimaryKey);
                    }
                } else if (action == EXPORT) {
                    doExportData(ProcessImportExportHandlerType.SCORE_TEMPLATE, scoreTemplateId, dependencyList, zipOutputStream);
                }
            }
        }
        JSONArray stepList = processObj.getJSONArray("stepList");
        if (CollectionUtils.isNotEmpty(stepList)) {
            // 通知策略
            // 集成
            // 回复模板
            // 子任务策略
            for (int i = 0; i < stepList.size(); i++) {
                JSONObject stepObj = stepList.getJSONObject(i);
                if (MapUtils.isEmpty(stepObj)) {
                    continue;
                }
                if (isChangeUuid) {
                    String oldUuid = stepObj.getString("uuid");
                    String newUuid = UuidUtil.randomUuid();
                    stepObj.put("uuid", newUuid);
                    oldUuid2NewUuidMap.put(oldUuid, newUuid);
                }
                JSONObject stepConfig = stepObj.getJSONObject("stepConfig");
                if (MapUtils.isEmpty(stepConfig)) {
                    continue;
                }
                JSONObject notifyPolicyConfig = stepConfig.getJSONObject("notifyPolicyConfig");
                if (MapUtils.isNotEmpty(notifyPolicyConfig)) {
                    Integer isCustom = notifyPolicyConfig.getInteger("isCustom");
                    Long policyId = notifyPolicyConfig.getLong("policyId");
                    if (Objects.equals(isCustom, 1) && policyId != null) {
                        if (action == IMPORT) {
                            Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, primaryChangeList);
                            if (newPrimaryKey != null) {
                                notifyPolicyConfig.put("policyId", newPrimaryKey);
                            }
                        } else if (action == EXPORT) {
                            doExportData(FrameworkImportExportHandlerType.NOTIFY_POLICY, policyId, dependencyList, zipOutputStream);
                        }
                    }
                }
                Long commentTemplateId = stepConfig.getLong("commentTemplateId");
                if (commentTemplateId != null) {
                    if (action == IMPORT) {
                        Object newPrimaryKey = getNewPrimaryKey(ProcessImportExportHandlerType.COMMENT_TEMPLATE, commentTemplateId, primaryChangeList);
                        if (newPrimaryKey != null) {
                            stepConfig.put("commentTemplateId", newPrimaryKey);
                        }
                    } else if (action == EXPORT) {
                        doExportData(ProcessImportExportHandlerType.COMMENT_TEMPLATE, commentTemplateId, dependencyList, zipOutputStream);
                    }
                }
                JSONObject actionConfig = stepConfig.getJSONObject("actionConfig");
                if (MapUtils.isNotEmpty(actionConfig)) {
                    JSONArray actionList = actionConfig.getJSONArray("actionList");
                    if (CollectionUtils.isNotEmpty(actionList)) {
                        for (int j = 0; j < actionList.size(); j++) {
                            JSONObject actionObj = actionList.getJSONObject(j);
                            if (MapUtils.isEmpty(actionObj)) {
                                continue;
                            }
                            String integrationUuid = actionObj.getString("integrationUuid");
                            if (StringUtils.isBlank(integrationUuid)) {
                                continue;
                            }
                            if (action == IMPORT) {
                                Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, primaryChangeList);
                                if (newPrimaryKey != null) {
                                    actionObj.put("integrationUuid", newPrimaryKey);
                                }
                            } else if (action == EXPORT) {
                                doExportData(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, dependencyList, zipOutputStream);
                            }
                        }
                    }
                }
                JSONObject taskConfig = stepConfig.getJSONObject("taskConfig");
                if (MapUtils.isNotEmpty(taskConfig)) {
                    JSONArray idList = taskConfig.getJSONArray("idList");
                    if (CollectionUtils.isNotEmpty(idList)) {
                        for (int j = 0; j < idList.size(); j++) {
                            Long subtaskPolicyId = idList.getLong(j);
                            if (subtaskPolicyId == null) {
                                continue;
                            }
                            if (action == IMPORT) {
                                Object newPrimaryKey = getNewPrimaryKey(ProcessImportExportHandlerType.SUBTASK_POLICY, subtaskPolicyId, primaryChangeList);
                                if (newPrimaryKey != null) {
                                    idList.set(j, newPrimaryKey);
                                }
                            } else if (action == EXPORT) {
                                doExportData(ProcessImportExportHandlerType.SUBTASK_POLICY, subtaskPolicyId, dependencyList, zipOutputStream);
                            }
                        }
                    }
                }
                String handler = stepObj.getString("handler");
                if (Objects.equals(handler, "automatic")) {
                    JSONObject automaticConfig = stepConfig.getJSONObject("automaticConfig");
                    if (MapUtils.isNotEmpty(automaticConfig)) {
                        JSONObject requestConfig = automaticConfig.getJSONObject("requestConfig");
                        if (MapUtils.isNotEmpty(requestConfig)) {
                            String integrationUuid = requestConfig.getString("integrationUuid");
                            if (StringUtils.isNotBlank(integrationUuid)) {
                                if (action == IMPORT) {
                                    Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, primaryChangeList);
                                    if (newPrimaryKey != null) {
                                        requestConfig.put("integrationUuid", newPrimaryKey);
                                    }
                                } else if (action == EXPORT) {
                                    doExportData(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, dependencyList, zipOutputStream);
                                }
                            }
                        }
                        JSONObject callbackConfig = automaticConfig.getJSONObject("callbackConfig");
                        if (MapUtils.isNotEmpty(callbackConfig)) {
                            JSONObject callbackConfigConfig = callbackConfig.getJSONObject("config");
                            if (MapUtils.isNotEmpty(callbackConfigConfig)) {
                                String integrationUuid = callbackConfigConfig.getString("integrationUuid");
                                if (StringUtils.isNotBlank(integrationUuid)) {
                                    if (action == IMPORT) {
                                        Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, primaryChangeList);
                                        if (newPrimaryKey != null) {
                                            callbackConfigConfig.put("integrationUuid", newPrimaryKey);
                                        }
                                    } else if (action == EXPORT) {
                                        doExportData(FrameworkImportExportHandlerType.INTEGRATION, integrationUuid, dependencyList, zipOutputStream);
                                    }
                                }
                            }
                        }
                    }
                } else if (Objects.equals(handler, "autoexec")) {
                    JSONObject autoexecConfig = stepConfig.getJSONObject("autoexecConfig");
                    if (MapUtils.isNotEmpty(autoexecConfig)) {
                        JSONArray configList = autoexecConfig.getJSONArray("configList");
                        if (CollectionUtils.isNotEmpty(configList)) {
                            for (int j = 0; j < configList.size(); j++) {
                                JSONObject configObj = configList.getJSONObject(j);
                                if (MapUtils.isEmpty(configObj)) {
                                    continue;
                                }
                                Long autoexecCombopId = configObj.getLong("autoexecCombopId");
                                if (autoexecCombopId == null) {
                                    continue;
                                }
                                if (action == IMPORT) {
                                    Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.AUTOEXEC_COMBOP, autoexecCombopId, primaryChangeList);
                                    if (newPrimaryKey != null) {
                                        configObj.put("autoexecCombopId", newPrimaryKey);
                                    }
                                } else if (action == EXPORT) {
                                    doExportData(FrameworkImportExportHandlerType.AUTOEXEC_COMBOP, autoexecCombopId, dependencyList, zipOutputStream);
                                }
                            }
                        }
                    }
                } else if (Objects.equals(handler, "cmdbsync")) {
                    JSONObject ciEntityConfig = stepConfig.getJSONObject("ciEntityConfig");
                    JSONArray configList = ciEntityConfig.getJSONArray("configList");
                    if (CollectionUtils.isNotEmpty(configList)) {
                        List<String> messageList = new ArrayList<>();
                        for (int j = 0; j < configList.size(); j++) {
                            JSONObject configObj = configList.getJSONObject(j);
                            if (MapUtils.isEmpty(configObj)) {
                                continue;
                            }
//                            Integer isStart = configObj.getInteger("isStart");
//                            if (!Objects.equals(isStart, 1)) {
//                                continue;
//                            }
                            Long ciId = configObj.getLong("ciId");
                            if (ciId == null) {
                                continue;
                            }
//                            if (action == IMPORT) {
//                                Object newPrimaryKey = getNewPrimaryKey(FrameworkImportExportHandlerType.CMDB_CI, ciId, primaryChangeList);
//                                if (newPrimaryKey != null) {
//                                    configObj.put("ciId", newPrimaryKey);
//                                }
//                            } else if (action == EXPORT) {
//                                doExportData(FrameworkImportExportHandlerType.CMDB_CI, ciId, dependencyList, zipOutputStream);
//                            }
                            String ciName = configObj.getString("ciName");
                            String ciLabel = configObj.getString("ciLabel");
                            // 配置项模型不做一起导入导出，只检查其是否存在
                            ImportExportHandler importExportHandler = ImportExportHandlerFactory.getHandler(FrameworkImportExportHandlerType.CMDB_CI.getValue());
                            if (importExportHandler == null) {
                                throw new ImportExportHandlerNotFoundException(FrameworkImportExportHandlerType.CMDB_CI.getText());
                            }
                            ImportExportBaseInfoVo importExportBaseInfoVo = new ImportExportBaseInfoVo();
                            importExportBaseInfoVo.setPrimaryKey(ciId);
                            importExportBaseInfoVo.setName(ciLabel + "(" + ciName + ")");
                            importExportBaseInfoVo.setType(FrameworkImportExportHandlerType.CMDB_CI.getValue());

                            try {
                                importExportHandler.checkIsExists(importExportBaseInfoVo);
                            } catch (ApiRuntimeException e) {
                                messageList.add(e.getMessage());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(messageList)) {
                            throw new DependencyNotFoundException(messageList);
                        }
                    }
                } else if (Objects.equals(handler, "dataconversion")) {
                    JSONObject ciEntityConfig = stepConfig.getJSONObject("dataConversionConfig");
                    JSONArray configList = ciEntityConfig.getJSONArray("configList");
                    if (CollectionUtils.isNotEmpty(configList)) {
                        List<String> messageList = new ArrayList<>();
                        for (int j = 0; j < configList.size(); j++) {
                            JSONObject configObj = configList.getJSONObject(j);
                            if (MapUtils.isEmpty(configObj)) {
                                continue;
                            }
                            Long ciId = configObj.getLong("ciId");
                            if (ciId == null) {
                                continue;
                            }
                            String ciName = configObj.getString("ciName");
                            String ciLabel = configObj.getString("ciLabel");
                            // 配置项模型不做一起导入导出，只检查其是否存在
                            ImportExportHandler importExportHandler = ImportExportHandlerFactory.getHandler(FrameworkImportExportHandlerType.CMDB_CI.getValue());
                            if (importExportHandler == null) {
                                throw new ImportExportHandlerNotFoundException(FrameworkImportExportHandlerType.CMDB_CI.getText());
                            }
                            ImportExportBaseInfoVo importExportBaseInfoVo = new ImportExportBaseInfoVo();
                            importExportBaseInfoVo.setPrimaryKey(ciId);
                            importExportBaseInfoVo.setName(ciLabel + "(" + ciName + ")");
                            importExportBaseInfoVo.setType(FrameworkImportExportHandlerType.CMDB_CI.getValue());

                            try {
                                importExportHandler.checkIsExists(importExportBaseInfoVo);
                            } catch (ApiRuntimeException e) {
                                messageList.add(e.getMessage());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(messageList)) {
                            throw new DependencyNotFoundException(messageList);
                        }
                    }
                } else if (Objects.equals(handler, "eoa")) {
                    JSONObject eoaConfig = stepConfig.getJSONObject("eoaConfig");
                    JSONArray eoaTemplateList = eoaConfig.getJSONArray("eoaTemplateList");
                    if (CollectionUtils.isNotEmpty(eoaTemplateList)) {
                        for (int j = 0; j < eoaTemplateList.size(); j++) {
                            JSONObject eoaTemplateObj = eoaTemplateList.getJSONObject(j);
                            if (MapUtils.isEmpty(eoaTemplateObj)) {
                                continue;
                            }
                            Long id = eoaTemplateObj.getLong("id");
                            if (id == null) {
                                continue;
                            }
                            if (action == IMPORT) {
                                Object newPrimaryKey = getNewPrimaryKey(ProcessImportExportHandlerType.EOA_TEMPLATE, id, primaryChangeList);
                                if (newPrimaryKey != null) {
                                    eoaTemplateObj.put("id", newPrimaryKey);
                                }
                            } else if (action == EXPORT) {
                                doExportData(ProcessImportExportHandlerType.EOA_TEMPLATE, id, dependencyList, zipOutputStream);
                            }
                        }
                    }
                }
            }
        }

        if (isChangeUuid) {
            JSONArray connectionList = processObj.getJSONArray("connectionList");
            if (CollectionUtils.isNotEmpty(connectionList)) {
                for (int i = 0; i < connectionList.size(); i++) {
                    JSONObject connectionObj = connectionList.getJSONObject(i);
                    if (MapUtils.isEmpty(connectionObj)) {
                        continue;
                    }
                    String oldUuid = connectionObj.getString("uuid");
                    String newUuid = UuidUtil.randomUuid();
                    connectionObj.put("uuid", newUuid);
                    oldUuid2NewUuidMap.put(oldUuid, newUuid);
                }
            }
            if (MapUtils.isNotEmpty(oldUuid2NewUuidMap)) {
                String configStr = config.toJSONString();
                for (Map.Entry<String, String> entry : oldUuid2NewUuidMap.entrySet()) {
                    configStr = configStr.replace(entry.getKey(), entry.getValue());
                }
                process.setConfig(configStr);
            }
        }
    }
}
