package codedriver.module.process.audithandler.handler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FocusUserAuditHandler implements IProcessTaskStepAuditDetailHandler {

    @Resource
    private UserMapper userMapper;

    @Override
    public String getType() {
        return ProcessTaskAuditDetailType.FOCUSUSER.getValue();
    }

    private String parse(String content) {
        List<Map<String, String>> resultList = new ArrayList<>();
        List<String> focusUserUuidList = new ArrayList<>();
        if (content.startsWith("[") && content.endsWith("]")) {
            focusUserUuidList = JSON.parseArray(content, String.class);
        } else {
            focusUserUuidList.add(content);
        }
        for (String uuid : focusUserUuidList) {
            UserVo userVo = userMapper.getUserBaseInfoByUuid(uuid.split("#")[1]);
            if (userVo != null) {
                Map<String, String> userMap = new HashMap<>();
                userMap.put("type", GroupSearch.USER.getValue());
                userMap.put("value", userVo.getUuid());
                userMap.put("text", userVo.getUserName());
                resultList.add(userMap);
            }
        }
        if (CollectionUtils.isNotEmpty(resultList)) {
            return JSON.toJSONString(resultList);
        }
        return null;
    }

    @Override
    public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
        String oldContent = processTaskStepAuditDetailVo.getOldContent();
        if (StringUtils.isNotBlank(oldContent)) {
            processTaskStepAuditDetailVo.setOldContent(parse(oldContent));
        }
        String newContent = processTaskStepAuditDetailVo.getNewContent();
        if (StringUtils.isNotBlank(newContent)) {
            processTaskStepAuditDetailVo.setNewContent(parse(newContent));
        }
        return 1;
    }
}
