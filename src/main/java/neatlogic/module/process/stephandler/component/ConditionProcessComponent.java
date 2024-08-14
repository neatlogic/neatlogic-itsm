/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.ConditionParamContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dto.condition.ConditionConfigVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.dto.ProcessTaskStepRelVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.RelExpressionVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import neatlogic.framework.util.RunScriptUtil;
import neatlogic.framework.util.javascript.JavascriptUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ConditionProcessComponent extends ProcessStepHandlerBase {
    static Logger logger = LoggerFactory.getLogger(ConditionProcessComponent.class);

    public final static String FORM_EXTEND_ATTRIBUTE_TAG = "common";

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getName() {
        return ProcessStepHandlerType.CONDITION.getName();
    }

    @Override
    public String getType() {
        return ProcessStepHandlerType.CONDITION.getType();
    }

    @Override
    public JSONObject getChartConfig() {
        return new JSONObject() {
            {
                this.put("icon", "tsfont-question");
                this.put("shape", "L-triangle:R-triangle");
                this.put("width", 68);
                this.put("height", 68);
                this.put("rdy", 68 / 4);
            }
        };
    }

    @Override
    public ProcessStepMode getMode() {
        return ProcessStepMode.AT;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public String getHandler() {
        return ProcessStepHandlerType.CONDITION.getHandler();
    }

    @Override
    protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) {
        /* 设置已完成标记位 **/
        currentProcessTaskStepVo.setIsAllDone(true);
        return 0;
    }

    @Override
    protected Set<Long> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo,
                                  List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
        try {
            UserContext.init(SystemUser.SYSTEM);
            Set<Long> nextStepIdSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(nextStepIdList)) {
                List<ProcessTaskStepVo> nextStepList = processTaskMapper.getProcessTaskStepListByIdList(nextStepIdList);
                Map<String, ProcessTaskStepVo> processTaskStepMap = nextStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getProcessStepUuid, e -> e));
                Map<String, String> processStepNameMap = nextStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getProcessStepUuid, ProcessTaskStepVo::getName));
                ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
                String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
                if (StringUtils.isNotBlank(stepConfig)) {
                    String formTag = (String) JSONPath.read(stepConfig, "formTag");
                    JSONArray moveonConfigList = (JSONArray) JSONPath.read(stepConfig, "moveonConfigList");
                    if (CollectionUtils.isNotEmpty(moveonConfigList)) {
                        JSONObject conditionParamData = null;
                        JSONArray ruleList = new JSONArray();
                        for (int i = 0; i < moveonConfigList.size(); i++) {
                            JSONObject moveonConfig = moveonConfigList.getJSONObject(i);
                            JSONArray targetStepList = moveonConfig.getJSONArray("targetStepList");
                            if (CollectionUtils.isNotEmpty(targetStepList)) {
                                JSONObject ruleObj = new JSONObject();
                                String type = moveonConfig.getString("type");
                                boolean canRun = false;
                                if ("always".equals(type)) {// 直接流转
                                    canRun = true;
                                    ruleObj.putAll(moveonConfig);
                                    ruleObj.put("result", true);
                                } else if ("optional".equals(type)) {// 自定义
                                    JSONArray conditionGroupList = moveonConfig.getJSONArray("conditionGroupList");
                                    if (CollectionUtils.isNotEmpty(conditionGroupList)) {
                                        if (conditionParamData == null) {
                                            conditionParamData = ProcessTaskConditionFactory.getConditionParamData(ConditionProcessTaskOptions.values(), currentProcessTaskStepVo, formTag);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("conditionParamData = " + conditionParamData.toJSONString());
                                            }
                                        }
                                        ConditionConfigVo conditionConfigVo = null;
                                        try {
                                            ConditionParamContext.init(conditionParamData).setTranslate(true);
                                            conditionConfigVo = new ConditionConfigVo(moveonConfig);
                                            String script = conditionConfigVo.buildScript();
                                            /* 将参数名称、表达式、值的value翻译成对应text，目前条件步骤生成活动时用到**/
                                            translate(conditionConfigVo, currentProcessTaskStepVo.getProcessTaskId(), formTag);
                                            // ((false || true) || (true && false) || (true || false))
                                            canRun = RunScriptUtil.runScript(script);
                                            ruleObj.put("result", canRun);
                                        } catch (Exception e) {
                                            logger.error(e.getMessage(), e);
                                        } finally {
                                            ConditionParamContext.get().release();
                                            if (conditionConfigVo != null) {
                                                ruleObj.put("conditionGroupList", conditionConfigVo.getConditionGroupList());
                                                ruleObj.put("conditionGroupRelList", conditionConfigVo.getConditionGroupRelList());
                                            }
                                        }
                                    }
                                } else {
                                    ruleObj.putAll(moveonConfig);
                                    ruleObj.put("result", true);
                                }
                                List<String> targetStepNameList = new ArrayList<>();
                                for (int j = 0; j < targetStepList.size(); j++) {
                                    String targetStepUuid = targetStepList.getString(j);
                                    String stepName = processStepNameMap.get(targetStepUuid);
                                    if (StringUtils.isNotBlank(stepName)) {
                                        targetStepNameList.add(stepName);
                                    }
                                    // 符合条件
                                    if (canRun) {
                                        ProcessTaskStepVo stepVo = processTaskStepMap.get(targetStepUuid);
                                        if (stepVo != null) {
                                            nextStepIdSet.add(stepVo.getId());
                                        }
                                    }
                                }
                                ruleObj.put("type", type);
                                ruleObj.put("targetStepList", targetStepNameList);
                                ruleList.add(ruleObj);
                            }
                        }
                        currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.RULE.getParamName(), ruleList);
                    }
                }
            }

            return nextStepIdSet;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ProcessTaskException(e.getMessage());
        }
    }

    private void translate(ConditionConfigVo conditionConfigVo, Long processTaskId, String formTag) {
        List<ConditionGroupVo> conditionGroupList = conditionConfigVo.getConditionGroupList();
        if (CollectionUtils.isNotEmpty(conditionGroupList)) {
//            List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTag(processTaskId, ConditionProcessComponent.FORM_EXTEND_ATTRIBUTE_TAG);
            List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskIdAngTagNew(processTaskId, formTag);
            Map<String, FormAttributeVo> formAttributeVoMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
            for (ConditionGroupVo conditionGroup : conditionGroupList) {
                List<ConditionVo> conditionList = conditionGroup.getConditionList();
                if (CollectionUtils.isNotEmpty(conditionList)) {
                    for (ConditionVo condition : conditionList) {
                        if ("common".equals(condition.getType())) {
                            IConditionHandler conditionHandler = ProcessTaskConditionFactory.getHandler(condition.getName());
                            if (conditionHandler != null) {
                                Object valueList = conditionHandler.valueConversionText(condition.getValueList(), null);
                                condition.setValueList(valueList);
                                String name = conditionHandler.getDisplayName();
                                condition.setName(name);
                            }
                        } else if ("form".equals(condition.getType())) {
                            IConditionHandler conditionHandler = ProcessTaskConditionFactory.getHandler("form");
                            if (conditionHandler != null) {
                                FormAttributeVo formAttribute = formAttributeVoMap.get(condition.getName());
                                if (formAttribute != null) {
                                    JSONObject configObj = new JSONObject();
                                    configObj.put("formAttribute", formAttribute);
                                    Object valueList = conditionHandler.valueConversionText(condition.getValueList(), configObj);
                                    condition.setValueList(valueList);
                                    condition.setName(formAttribute.getLabel());
                                }
                            }
                        }
                        String expressionName = Expression.getExpressionName(condition.getExpression());
                        condition.setExpression(expressionName);
                    }
                }
            }
        }
    }

    @Override
    protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 1;
    }

    @Override
    protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        if (StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
            currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(),
                    currentProcessTaskStepVo.getError());
        }
        /* 处理历史记录 **/
        // String action = currentProcessTaskStepVo.getParamObj().getString("action");
        // AuditHandler.audit(currentProcessTaskStepVo, ProcessTaskAuditType.getProcessTaskAuditType(action));
        processStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.CONDITION);
        return 1;
    }

    @Override
    protected int myReapproval(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myReapprovalAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    public void makeupFlowJobStepVo(ProcessTaskStepVo flowJobStepVo) {
        if (flowJobStepVo.getRelList() != null && flowJobStepVo.getRelList().size() > 0) {
            for (ProcessTaskStepRelVo relVo : flowJobStepVo.getRelList()) {
                if (!StringUtils.isBlank(relVo.getCondition())) {
                    Pattern pattern;
                    Matcher matcher;
                    StringBuffer temp = new StringBuffer();
                    String regex = "\\$\\{([^}]+)}";
                    pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(relVo.getCondition());
                    List<String> stepAndKeyList = new ArrayList<>();
                    while (matcher.find()) {
                        matcher.appendReplacement(temp, "map[\"" + matcher.group(1) + "\"]");
                        if (!stepAndKeyList.contains(matcher.group(1))) {
                            stepAndKeyList.add(matcher.group(1));
                        }
                    }
                    matcher.appendTail(temp);

                    StringBuilder script = new StringBuilder();
                    script.append("function run(){");
                    script.append("var map = new Object();");

                    if (stepAndKeyList.size() > 0) {
                        List<RelExpressionVo> relExpressionList = new ArrayList<>();
                        for (String stepAndKey : stepAndKeyList) {
                            if (stepAndKey.contains(".") && stepAndKey.split("\\.").length == 2) {
                                String stepUid = stepAndKey.split("\\.")[0];
                                String key = stepAndKey.split("\\.")[1];
                                RelExpressionVo relExpressionVo = new RelExpressionVo();
                                relExpressionVo.setExpression("${" + stepUid + "." + key + "}");
                                List<String> valueList = new ArrayList<>(); // flowJobMapper.getFlowJobStepNodeParamValueByFlowJobIdUidKey(flowJobStepVo.getFlowJobId(),
                                // stepUid,
                                // key);
                                if (valueList.size() > 0) {
                                    if (valueList.size() > 1) {
                                        script.append("map[\"").append(stepUid).append(".").append(key).append("\"] = [");
                                        StringBuilder v = new StringBuilder("[");
                                        for (int i = 0; i < valueList.size(); i++) {
                                            String value = valueList.get(i);
                                            script.append("\"").append(value).append("\"");
                                            v.append("\"").append(value).append("\"");
                                            if (i < valueList.size() - 1) {
                                                script.append(",");
                                                v.append(",");
                                            }
                                        }
                                        v.append("]");
                                        script.append("];");
                                        relExpressionVo.setValue(v.toString());
                                    } else {
                                        script.append("map[\"").append(stepUid).append(".").append(key).append("\"] = \"").append(valueList.get(0)).append("\";");
                                        relExpressionVo.setValue("\"" + valueList.get(0) + "\"");
                                    }
                                }
                                relExpressionList.add(relExpressionVo);
                            }
                        }
                        relVo.setRelExpressionList(relExpressionList);
                    }
                    script.append("return ").append(temp).append(";");
                    script.append("}");
                    ScriptEngine se = JavascriptUtil.getEngine();
                    try {
                        se.eval(script.toString());
                        Invocable invocableEngine = (Invocable) se;
                        Object callbackvalue = invocableEngine.invokeFunction("run");
                        relVo.setScriptResult(callbackvalue.toString());
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        relVo.setError(ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public int getSort() {
        return 3;
    }

    @Override
    public Boolean isAllowStart() {
        return null;
    }

    @Override
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) {
        return 0;
    }

    @Override
    protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myStart(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) {
        return 0;
    }

    @Override
    protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) {
        return 0;
    }

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    /**
     * 回退输出路径数量
     * -1代表不限制
     *
     * @return
     */
    @Override
    public int getBackwardOutputQuantity() {
        return 0;
    }

    @Override
    public boolean disableAssign() {
        return true;
    }

}
