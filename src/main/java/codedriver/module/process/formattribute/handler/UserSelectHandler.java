package codedriver.module.process.formattribute.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.FormHandlerBase;

@Component
public class UserSelectHandler extends FormHandlerBase {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public String getHandler() {
        return "formuserselect";
    }

    @Override
    public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
        return false;
    }

    @Override
    public Object valueConversionText(AttributeDataVo attributeDataVo, JSONObject configObj) {
        Object dataObj = attributeDataVo.getDataObj();
        if (dataObj != null) {
            boolean isMultiple = configObj.getBooleanValue("isMultiple");
            if (isMultiple) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(dataObj), String.class);
                if (CollectionUtils.isNotEmpty(valueList)) {
                    List<String> textList = new ArrayList<>();
                    for (String key : valueList) {
                        textList.add(parse(key));
                    }
                    return textList;
                }
                return valueList;
            } else {
                String value = (String)dataObj;
                if (StringUtils.isNotBlank(value)) {
                    return parse(value);
                }
            }
        }
        return dataObj;
    }

    @Override
    public Object textConversionValue(List<String> values, JSONObject config) {
        return null;
    }

    private String parse(String key) {
        if (key.contains("#")) {
            String[] split = key.split("#");
            if (GroupSearch.COMMON.getValue().equals(split[0])) {
                return UserType.getText(split[1]);
            } else if (GroupSearch.USER.getValue().equals(split[0])) {
                UserVo user = userMapper.getUserBaseInfoByUuid(split[1]);
                if (user != null) {
                    return user.getUserName();
                } else {
                    return split[1];
                }
            } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                TeamVo team = teamMapper.getTeamByUuid(split[1]);
                if (team != null) {
                    return team.getName();
                } else {
                    return split[1];
                }
            } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                RoleVo role = roleMapper.getRoleByUuid(split[1]);
                if (role != null) {
                    return role.getName();
                } else {
                    return split[1];
                }
            }
        }
        return key;
    }

    @Override
    public String getHandlerName() {
        return "用户选择器";
    }

    @Override
    public String getIcon() {
        return "ts-user";
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public String getDataType() {
        return "string";
    }

    @Override
    public boolean isConditionable() {
        return true;
    }

    @Override
    public boolean isShowable() {
        return true;
    }

    @Override
    public boolean isValueable() {
        return true;
    }

    @Override
    public boolean isFilterable() {
        return true;
    }

    @Override
    public boolean isExtendable() {
        return false;
    }

    @Override
    public String getModule() {
        return "process";
    }

    @Override
    public boolean isForTemplate() {
        return true;
    }

    @Override
    public boolean isAudit() {
        return true;
    }

    @Override
    public String getHandlerType(String model) {
        return "userselect";
    }
}
