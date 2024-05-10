package neatlogic.module.process.condition.handler;

import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessTaskOwnerRoleCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public String getName() {
        return "ownerrole";
    }

    @Override
    public String getDisplayName() {
        return "上报人角色";
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.USERSELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType configType) {
        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.USERSELECT.toString());
        config.put("multiple", true);
        config.put("initConfig", new JSONObject() {
            {
                this.put("excludeList", new JSONArray() {{

                }});
                this.put("groupList", new JSONArray() {
                    {
                        this.add(GroupSearch.ROLE.getValue());
                    }
                });
                this.put("includeList", new JSONArray() {

                });
            }
        });
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 11;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                RoleVo roleVo = roleMapper.getRoleByUuid(value.toString().substring(5));
                if (roleVo != null) {
                    return roleVo.getName();
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    RoleVo roleVo = roleMapper.getRoleByUuid(valueStr.substring(5));
                    if (roleVo != null) {
                        textList.add(roleVo.getName());
                    } else {
                        textList.add(valueStr);
                    }
                }
                return String.join("、", textList);
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            String owner = processTaskVo.getOwner();
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(owner);
            return authenticationInfoVo.getRoleUuidList().stream().map(o -> GroupSearch.ROLE.getValuePlugin() + o).collect(Collectors.toList());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getConditionParamDataForHumanization(ProcessTaskStepVo processTaskStepVo) {
        List<String> roleUuidList = (List<String>) getConditionParamData(processTaskStepVo);
        if (CollectionUtils.isEmpty(roleUuidList)) {
            return null;
        }
        List<String> roleNameList = new ArrayList<>();
        for (String roleUuid : roleUuidList) {
            RoleVo roleVo = roleMapper.getRoleByUuid(roleUuid.substring(5));
            if (roleVo != null) {
                roleNameList.add(roleVo.getName());
            }
        }
        return roleNameList;
    }

}
