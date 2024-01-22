package neatlogic.module.process.dependency.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dependency.core.FixedTableDependencyHandlerBase;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.dependency.dto.DependencyVo;
import neatlogic.framework.process.constvalue.ProcessFromType;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class EoaTemplate2ProcessStepDependencyHandler extends FixedTableDependencyHandlerBase {

    @Resource
    private ProcessMapper processMapper;

    @Override
    protected DependencyInfoVo parse(DependencyVo dependencyVo) {
        JSONObject config = dependencyVo.getConfig();
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        String stepUuid = dependencyVo.getTo();
        String processUuid = config.getString("processUuid");
        ProcessVo processVo = processMapper.getProcessBaseInfoByUuid(processUuid);
        if (processVo == null) {
            return null;
        }
        JSONObject processConfig = processVo.getConfig();
        if (MapUtils.isEmpty(processConfig)) {
            return null;
        }
        JSONObject processObj = processConfig.getJSONObject("process");
        if (MapUtils.isEmpty(processObj)) {
            return null;
        }
        JSONArray stepList = processObj.getJSONArray("stepList");
        if (CollectionUtils.isEmpty(stepList)) {
            return null;
        }
        for (int i = 0; i < stepList.size(); i++) {
            JSONObject stepObj = stepList.getJSONObject(i);
            if (MapUtils.isEmpty(stepObj)) {
                continue;
            }
            String uuid = stepObj.getString("uuid");
            if (!Objects.equals(uuid, stepUuid)) {
                continue;
            }
            JSONObject dependencyInfoConfig = new JSONObject();
            dependencyInfoConfig.put("processUuid", processUuid);
            dependencyInfoConfig.put("stepUuid", stepUuid);
            List<String> pathList = new ArrayList<>();
            pathList.add("流程管理");
            pathList.add(processVo.getName());
            String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/flow-edit?uuid=${DATA.processUuid}&stepUuid=${DATA.stepUuid}";
            return new DependencyInfoVo(stepUuid, dependencyInfoConfig, stepObj.getString("name"), pathList, urlFormat, this.getGroupName());
        }
        return null;
    }

    @Override
    public IFromType getFromType() {
        return ProcessFromType.EOATEMPLATE;
    }
}
