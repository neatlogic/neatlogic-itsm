package neatlogic.module.process.workcenter.column.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.module.process.service.NewWorkcenterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskCurrentStepWorkerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getName() {
        return "currentstepworker";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpwch.processtaskcurrentstepworkercolumn.getdisplayname");
    }


    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSort() {
        return 7;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        JSONArray workerArray = JSONArray.parseArray(getValue(processTaskVo).toString());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < workerArray.size(); i++) {
            sb.append(workerArray.getJSONObject(i).getJSONObject("workerVo").getString("name")).append(";");
        }
        return sb.toString();
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<>();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONArray workerArray = new JSONArray();
        List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
        if (Arrays.asList(ProcessTaskStatus.RUNNING.getValue(), ProcessTaskStatus.HANG.getValue()).contains(processTaskVo.getStatus())) {
            for (ProcessTaskStepVo stepVo : stepVoList) {
                //查询其它步骤handler minorList
                newWorkcenterService.getStepTaskWorkerList(workerArray, stepVo);
            }
        }

        // 去重逻辑，保持原顺序
        Collection<JSONObject> uniqueObjects = workerArray.stream()
                .map(JSONObject.class::cast) // 转为 JSONObject
                .collect(Collectors.toMap(
                        obj -> obj.getString("workTypename") + obj.getJSONObject("workerVo").getString("uuid"), // 根据 uuid 作为键
                        obj -> obj, // 直接存储 JSONObject
                        (existing, replacement) -> existing, // 保留先出现的对象
                        LinkedHashMap::new // 使用 LinkedHashMap 保持顺序
                ))
                .values(); // 获取唯一值集合

        // 转回 JSONArray
        JSONArray distinctArray = new JSONArray();
        distinctArray.addAll(uniqueObjects);
        return distinctArray;
    }

}
