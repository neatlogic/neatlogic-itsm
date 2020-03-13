package codedriver.module.process.api.workcenter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.process.workcenter.WorkcenterEsHandler;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Transactional
@Service
public class workcenterSearchTestApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "workcenter/test/search";
	}

	@Override
	public String getName() {
		return "测试工单中心search接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试工单中心search接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		QueryResult result =  WorkcenterEsHandler.searchTask(new WorkcenterVo());
		if (!result.getData().isEmpty()) {
            Map<String, String> titles = new HashMap<>(result.getData().size());
            for (MultiAttrsObject el : result.getData()) {
                titles.put(el.getId(), el.getString("title"));
                System.out.println(el.getString("title"));
            }
            return titles;
        }
		return "OK";
	}

}
