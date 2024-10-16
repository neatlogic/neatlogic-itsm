package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskAuditFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.util.FormUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormAuditHandler implements IProcessTaskStepAuditDetailHandler {

    private final static Logger logger = LoggerFactory.getLogger(FormAuditHandler.class);

    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getType() {
        return ProcessTaskAuditDetailType.FORM.getValue();
    }

    @Override
    public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
        List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList =
            JSON.parseArray(processTaskStepAuditDetailVo.getOldContent(), ProcessTaskFormAttributeDataVo.class);
        if (oldProcessTaskFormAttributeDataList == null) {
            oldProcessTaskFormAttributeDataList = new ArrayList<>();
        }

        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList =
            JSON.parseArray(processTaskStepAuditDetailVo.getNewContent(), ProcessTaskFormAttributeDataVo.class);
        if (processTaskFormAttributeDataList == null) {
            processTaskFormAttributeDataList = new ArrayList<>();
        }
        Iterator<ProcessTaskFormAttributeDataVo> iterator = processTaskFormAttributeDataList.iterator();
        // 循环删除相同的
//        while (iterator.hasNext()) {
//            ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = iterator.next();
//            int index = oldProcessTaskFormAttributeDataList.indexOf(processTaskFormAttributeDataVo);
//
//            if (index != -1) {
//                iterator.remove();
//                oldProcessTaskFormAttributeDataList.remove(index);
//            } else {// 删除不能audit的表单组件
//                IFormAttributeHandler formHandler =
//                    FormAttributeHandlerFactory.getHandler(processTaskFormAttributeDataVo.getType());
//                if (formHandler != null && !formHandler.isAudit()) {
//                    iterator.remove();
//                }
//            }
//        }
        processTaskStepAuditDetailVo.setOldContent(null);
        processTaskStepAuditDetailVo.setNewContent(null);
        if (CollectionUtils.isEmpty(processTaskFormAttributeDataList)) {
            return 0;
        }
        Long processTaskId = processTaskFormAttributeDataList.get(0).getProcessTaskId();
        ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskForm == null || StringUtils.isBlank(processTaskForm.getFormContent())) {
            return 0;
        }
//        String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskForm.getFormContentHash());
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormUuid(processTaskForm.getFormUuid());
        formVersionVo.setFormName(processTaskForm.getFormName());
        formVersionVo.setFormConfig(JSONObject.parseObject(processTaskForm.getFormContent()));
        String mainSceneUuid = formVersionVo.getFormConfig().getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        List<FormAttributeVo> mainSceneFormAttributeList = formVersionVo.getFormAttributeList();
        if (CollectionUtils.isEmpty(mainSceneFormAttributeList)) {
            return 0;
        }
        // 判断是否修改了表单数据
        if (!FormUtil.isModifiedFormData(mainSceneFormAttributeList, processTaskFormAttributeDataList, oldProcessTaskFormAttributeDataList)) {
            // 表单未修改，返回值为0，表示不用显示表单内容
            return 0;
        }
        List<ProcessTaskAuditFormAttributeDataVo> auditFormAttributeDataList = new ArrayList<>();
        Map<String, ProcessTaskFormAttributeDataVo> newProcessTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
        Map<String, ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataMap = oldProcessTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
        Map<String, JSONObject> attributeConfigMap = new HashMap<>();
        Map<String, String> attributeLabelMap = new HashMap<>();
        for (FormAttributeVo formAttributeVo : mainSceneFormAttributeList) {
            String attributeUuid = formAttributeVo.getUuid();
            attributeLabelMap.put(attributeUuid, formAttributeVo.getLabel());
            attributeConfigMap.put(attributeUuid, formAttributeVo.getConfig());
            ProcessTaskFormAttributeDataVo newProcessTaskFormAttributeDataVo = newProcessTaskFormAttributeDataMap.get(attributeUuid);
            ProcessTaskFormAttributeDataVo oldProcessTaskFormAttributeDataVo = oldProcessTaskFormAttributeDataMap.get(attributeUuid);
            if (oldProcessTaskFormAttributeDataVo == null && newProcessTaskFormAttributeDataVo == null) {
                // 这里auditFormAttributeDataVo为null，不用setModified(0)
                continue;
            }
            ProcessTaskAuditFormAttributeDataVo auditFormAttributeDataVo = new ProcessTaskAuditFormAttributeDataVo();
            auditFormAttributeDataVo.setAttributeUuid(attributeUuid);
            auditFormAttributeDataVo.setAttributeKey(formAttributeVo.getKey());
            auditFormAttributeDataVo.setAttributeLabel(formAttributeVo.getLabel());
            auditFormAttributeDataVo.setHandler(formAttributeVo.getHandler());
            auditFormAttributeDataList.add(auditFormAttributeDataVo);
            // 在此之前如果该属性的值，在数据库中没有对应的旧数据
            if (oldProcessTaskFormAttributeDataVo == null) {
                // 现在要保存该属性的值为null，则将该属性值保存到数据库中，但不标记为已修改
                if (newProcessTaskFormAttributeDataVo.getDataObj() == null) {
                    auditFormAttributeDataVo.setModified(0);
                } else {
                    // 现在要保存该属性的值不为null，则将该属性值保存到数据库中，但标记为已修改
                    auditFormAttributeDataVo.setModified(1);
                    auditFormAttributeDataVo.setDataObj(newProcessTaskFormAttributeDataVo.getDataObj());
//                    auditFormAttributeDataVo.setSort(newProcessTaskFormAttributeDataVo.getSort());
                }
            }
            // 如果现在接口参数中没有该属性值，则表示不修改该属性值
            else if (newProcessTaskFormAttributeDataVo == null) {
                // 将该属性旧值添加到stepFormAttributeDataMap，记录该步骤流转时该属性值
                auditFormAttributeDataVo.setDataObj(oldProcessTaskFormAttributeDataVo.getDataObj());
//                auditFormAttributeDataVo.setSort(oldProcessTaskFormAttributeDataVo.getSort());
                auditFormAttributeDataVo.setModified(0);
            } else {
                auditFormAttributeDataVo.setDataObj(newProcessTaskFormAttributeDataVo.getDataObj());
//                auditFormAttributeDataVo.setSort(newProcessTaskFormAttributeDataVo.getSort());
                if (Objects.equals(oldProcessTaskFormAttributeDataVo.getDataObj(), newProcessTaskFormAttributeDataVo.getDataObj())) {
                    // 如果新表单属性值与旧表单属性值相同，就不用replace更新数据了
                    oldProcessTaskFormAttributeDataMap.remove(attributeUuid);
                    newProcessTaskFormAttributeDataMap.remove(attributeUuid);
                    auditFormAttributeDataVo.setModified(0);
                } else {
                    auditFormAttributeDataVo.setModified(1);
                }
            }
            // 删除不能audit的表单组件
            IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
            if (formHandler == null || !formHandler.isAudit()) {
                oldProcessTaskFormAttributeDataMap.remove(attributeUuid);
                newProcessTaskFormAttributeDataMap.remove(attributeUuid);
            }
        }
        processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(auditFormAttributeDataList));
        Map<String, String> oldContentMap = new HashMap<>();
        for (Map.Entry<String, ProcessTaskFormAttributeDataVo> entry : oldProcessTaskFormAttributeDataMap.entrySet()) {
            ProcessTaskFormAttributeDataVo attributeDataVo = entry.getValue();
            IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(attributeDataVo.getHandler());
            if (handler != null) {
                String result = null;
                Object value = handler.valueConversionText(attributeDataVo,
                        attributeConfigMap.get(attributeDataVo.getAttributeUuid()));
                if (value != null) {
                    if (value instanceof String) {
                        result = (String) value;
                    } else if (value instanceof List) {
                        List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                        result = String.join("、", valueList);
                    } else {
                        result = value.toString();
                    }
                }
                oldContentMap.put(attributeDataVo.getAttributeUuid(), result);
            } else {
                oldContentMap.put(attributeDataVo.getAttributeUuid(), attributeDataVo.getData());
            }
        }
        JSONArray contentList = new JSONArray();
        for (Map.Entry<String, ProcessTaskFormAttributeDataVo> entry : newProcessTaskFormAttributeDataMap.entrySet()) {
            ProcessTaskFormAttributeDataVo attributeDataVo = entry.getValue();
            JSONObject content = new JSONObject();
            content.put("label", attributeLabelMap.get(attributeDataVo.getAttributeUuid()));
            String oldContent = oldContentMap.get(attributeDataVo.getAttributeUuid());
            if (oldContent != null) {
                content.put("oldContent", oldContent);
            }
            String newContent = null;
            IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(attributeDataVo.getHandler());
            if (handler != null) {
                String result = null;
                Object value = handler.valueConversionText(attributeDataVo,
                        attributeConfigMap.get(attributeDataVo.getAttributeUuid()));
                if (value != null) {
                    if (value instanceof String) {
                        result = (String) value;
                    } else if (value instanceof List) {
                        List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                        result = String.join("、", valueList);
                    } else {
                        result = value.toString();
                    }
                }
                content.put("newContent", result);
                newContent = result;
            } else {
                content.put("newContent", attributeDataVo.getData());
                newContent = attributeDataVo.getData();
            }
            // }
            if(oldContent != null && newContent != null){
                if(oldContent.equals(newContent)){
                    content.remove("oldContent");
                    content.put("changeType", "new");
                }else{
                    content.put("changeType", "update");
                }
            }else if(oldContent != null){
                content.put("changeType", "clear");
            }else if(newContent != null){
                content.put("changeType", "new");
            }
            contentList.add(content);
        }
        processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(contentList));
        return 1;
    }

}
