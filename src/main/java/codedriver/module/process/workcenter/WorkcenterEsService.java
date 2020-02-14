package codedriver.module.process.workcenter;

import static com.techsure.multiattrsearch.query.QueryBuilder.attr;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.MultiAttrsObjectPool;
import com.techsure.multiattrsearch.query.QueryBuilder;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Service
public class WorkcenterEsService {
	private MultiAttrsObjectPool objectPool;

	@PostConstruct
	public void init() {
		/*Map<String, String> esClusters = Config.ES_CLUSTERS;
		if (esClusters.isEmpty()) {
			throw new IllegalStateException("ES集群信息未配置，es.cluster.<cluster-name>=<ip:port>[,<ip:port>...]");
		}

		MultiAttrsSearchConfig config = new MultiAttrsSearchConfig();
		config.setPoolName(POOL_NAME);

		Map.Entry<String, String> cluster = esClusters.entrySet().iterator().next();
		config.addCluster(cluster.getKey(), cluster.getValue());
		if (esClusters.size() > 1) {
			logger.warn("multiple clusters available, only cluster {} was used (picked randomly) for testing",
					cluster.getKey());
		}

		objectPool = MultiAttrsSearch.getObjectPool(config);*/
	}

	public QueryBuilder createQueryBuilder(String tenantId) {
		return objectPool.createQueryBuilder().from(tenantId);
	}
	
	public QueryResult searchTask(WorkcenterVo workcenterVo){
		//TODO lvzk 条件解析拼成es api 的格式查询
		QueryBuilder.ConditionBuilder cond = null;
		cond = attr("title").contains("标题1");
        /*if (status != null) {
            cond = attr("status").eq(status);
        }
        if (!tags.isEmpty()) {
            cond = cond == null ? attr("tags").containsAny(tags) : cond.and().attr("tags").containsAny(tags);
        }
        if (title != null && !StringUtils.isBlank(title)) {
            cond = cond == null ? attr("title").contains(title) : cond.and().attr("title").contains(title);
        }*/
		QueryBuilder builder = createQueryBuilder(TenantContext.get().getTenantUuid())
             .select("title", "status", "created_at")
             .orderBy("created_time", false)
            .limit(workcenterVo.getCurrentPage(), workcenterVo.getPageSize());
	     if (cond != null) {
	         builder.where(cond);
	     }
	     QueryResult result = builder.build().execute();
	     return result;
	}
}
