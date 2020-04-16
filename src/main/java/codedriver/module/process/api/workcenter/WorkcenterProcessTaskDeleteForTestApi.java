package codedriver.module.process.api.workcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPool;

import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.workcenter.elasticsearch.core.WorkcenterEsHandlerBase;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;

@Transactional
@Service
public class WorkcenterProcessTaskDeleteForTestApi extends ApiComponentBase {

	@Autowired
	ProcessTaskMapper processTaskMapper;
	@Override
	public String getToken() {
		return "workcenter/test/delete";
	}

	@Override
	public String getName() {
		return "测试工单中心数据删除delete接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "测试工单中心数据删除delete接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		for(Integer i=jsonObj.getInteger("from");i<jsonObj.getInteger("to");i++) {
		MultiAttrsObjectPool  poll = ElasticSearchPoolManager.getObjectPool(WorkcenterEsHandlerBase.POOL_NAME);
		poll.checkout("techsure", null);
		poll.delete(i.toString());
		}
		return "OK";
	}

}
