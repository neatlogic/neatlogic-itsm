package neatlogic.module.process.api.process;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTagGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTagMapper processTagMapper;

    @Override
    public String getToken() {
        return "process/tag/get";
    }

    @Override
    public String getName() {
        return "获取标签_下拉";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "默认值列表"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "list", explode = ValueTextVo[].class, desc = "流程列表")
    })
    @Description(desc = "获取标签_下拉")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        ProcessTagVo processTagVo = JSON.toJavaObject(jsonObj, ProcessTagVo.class);
		JSONArray defaultValue = processTagVo.getDefaultValue();
		if (CollectionUtils.isNotEmpty(defaultValue)) {
			List<String> tagNameList = defaultValue.toJavaList(String.class);
			List<ValueTextVo> valueTextList = new ArrayList<>();
			for (String tagName : tagNameList) {
				valueTextList.add(new ValueTextVo(tagName, tagName));
			}
			resultObj.put("list", valueTextList);
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

}
