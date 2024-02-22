package neatlogic.module.process.workerpolicy.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormWorkerPolicyHandler implements IWorkerPolicyHandler {

    @Override
    public String getType() {
        return WorkerPolicy.FORM.getValue();
    }

    @Override
    public String getName() {
        return WorkerPolicy.FORM.getText();
    }

    @Override
    public int isOnlyOnceExecute() {
        return 0;
    }

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo,
                                                 ProcessTaskStepVo currentProcessTaskStepVo) {
        List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
        if (MapUtils.isNotEmpty(workerPolicyVo.getConfigObj())) {
            /** 选择的表单属性uuid **/
            JSONArray attributeUuidArray = workerPolicyVo.getConfigObj().getJSONArray("attributeUuidList");
            if (CollectionUtils.isNotEmpty(attributeUuidArray)) {
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(e -> e.getAttributeUuid(), e -> e));
                List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
                List<String> attributeUuidList = attributeUuidArray.toJavaList(String.class);
                for (String attributeUuid : attributeUuidList) {
                    FormAttributeVo formAttributeVo = formAttributeMap.get(attributeUuid);
                    if (formAttributeVo == null) {
                        continue;
                    }
                    ProcessTaskFormAttributeDataVo processTaskFormAttributeData = processTaskFormAttributeDataMap.get(attributeUuid);
                    if (processTaskFormAttributeData == null) {
                        continue;
                    }
                    Object dataObj = processTaskFormAttributeData.getDataObj();
                    if (dataObj == null) {
                        continue;
                    }
                    /** 只有表单属性类型为用户选择器才生效 **/
                    if (FormHandler.FORMUSERSELECT.getHandler().equals(processTaskFormAttributeData.getType())) {
                        IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(FormHandler.FORMUSERSELECT.getHandler());
                        if (handler != null) {
                            JSONObject detailedData = handler.getDetailedData(processTaskFormAttributeData, formAttributeVo.getConfigObj());
                            JSONArray valueList = detailedData.getJSONArray("valueList");
                            if (CollectionUtils.isNotEmpty(valueList)) {
                                List<String> dataList = valueList.toJavaList(String.class);
                                for (String value : dataList) {
                                    /** 校验属性值是否合法，只有是当前存在的用户、组、角色才合法 **/
                                    if (value.contains("#")) {
                                        String[] split = value.split("#");
                                        if (GroupSearch.USER.getValue().equals(split[0])) {
                                            if (userMapper.checkUserIsExists(split[1]) == 0) {
                                                continue;
                                            }
                                        } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                            if (teamMapper.checkTeamIsExists(split[1]) == 0) {
                                                continue;
                                            }
                                        } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                            if (roleMapper.checkRoleIsExists(split[1]) == 0) {
                                                continue;
                                            }
                                        } else {
                                            continue;
                                        }
                                        processTaskStepWorkerList.add(
                                                new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(),
                                                        currentProcessTaskStepVo.getId(), split[0], split[1],
                                                        ProcessUserType.MAJOR.getValue()));
                                    }
                                }
                            }
                        }
                    } else if (FormHandler.FORMSELECT.getHandler().equals(processTaskFormAttributeData.getType())) {
                        IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(FormHandler.FORMSELECT.getHandler());
                        if (handler != null) {
                            JSONObject detailedData = handler.getDetailedData(processTaskFormAttributeData, formAttributeVo.getConfigObj());
                            JSONArray valueList = detailedData.getJSONArray("valueList");
                            if (CollectionUtils.isNotEmpty(valueList)) {
                                List<String> dataList = valueList.toJavaList(String.class);
                                for (String value : dataList) {
                                    if (StringUtils.isNotBlank(value)) {
                                        if (userMapper.checkUserIsExists(value) > 0) {
                                            processTaskStepWorkerList.add(
                                                    new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(),
                                                            currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(),
                                                            value, ProcessUserType.MAJOR.getValue()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return processTaskStepWorkerList;
    }
}
