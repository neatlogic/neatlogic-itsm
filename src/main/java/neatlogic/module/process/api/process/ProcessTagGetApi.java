package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTagGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTagMapper processTagMapper;

    @Override
    public String getToken() {
        return "process/tag/get";
    }

    @Override
    public String getName() {
        return "nmpap.processtaggetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpap.processtaggetapi.input.param.desc.keyword"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "nmpap.processtaggetapi.input.param.desc.defaultvalue"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpap.processtaggetapi.input.param.desc.needpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpap.processtaggetapi.input.param.desc.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpap.processtaggetapi.input.param.desc.currentpage")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "list", explode = ValueTextVo[].class, desc = "nmpap.processtaggetapi.output.param.desc.list")
    })
    @Description(desc = "nmpap.processtaggetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        ProcessTagVo processTagVo = JSON.toJavaObject(jsonObj, ProcessTagVo.class);
        JSONArray defaultValue = processTagVo.getDefaultValue();
        if (CollectionUtils.isNotEmpty(defaultValue)) {
            resultObj.put("list", getDefaultValueResult(defaultValue));
        } else {
            int rowNum = processTagMapper.getProcessTagCount(processTagVo);
            processTagVo.setRowNum(rowNum);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", processTagVo.getPageSize());
            resultObj.put("currentPage", processTagVo.getCurrentPage());
            resultObj.put("pageCount", processTagVo.getPageCount());
            if (rowNum > 0) {
                resultObj.put("list", processTagMapper.getProcessTagForSelect(processTagVo));
            } else {
                resultObj.put("list", new ArrayList<>());
            }
        }
        return resultObj;
    }

    /**
     * 处理默认值
     *
     * @param defaultValue 默认值
     */
    private List<?> getDefaultValueResult(JSONArray defaultValue) {
        List<ValueTextVo> valueTextList = new ArrayList<>();
        if (defaultValue == null || defaultValue.isEmpty()) {
            return Collections.emptyList();
        }

        Object first = null;
        for (Object o : defaultValue) {
            if (o != null) {
                first = o;
                break;
            }
        }

        if (first == null) {
            return Collections.emptyList();
        }

        if (first instanceof String) {
            List<String> tagNameList = defaultValue.toJavaList(String.class);

            for (String tagName : tagNameList) {
                valueTextList.add(new ValueTextVo(tagName, tagName));
            }
        }

        if (first instanceof Number) {
            List<Long> tagIdList = defaultValue.toJavaList(Long.class);
            List<ProcessTagVo> tagVos = processTagMapper.getProcessTagByIdList(tagIdList);
            if (CollectionUtils.isNotEmpty(tagVos)) {
                for (ProcessTagVo tagVo : tagVos) {
                    valueTextList.add(new ValueTextVo(tagVo.getId(), tagVo.getName()));
                }

            }
        }

        return valueTextList;
    }

}
