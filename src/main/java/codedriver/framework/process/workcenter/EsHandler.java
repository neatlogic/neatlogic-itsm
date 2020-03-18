package codedriver.framework.process.workcenter;

import com.techsure.multiattrsearch.MultiAttrsObjectPool;
import com.techsure.multiattrsearch.MultiAttrsQuery;
import com.techsure.multiattrsearch.query.QueryBuilder;
import com.techsure.multiattrsearch.query.QueryParser;
import com.techsure.multiattrsearch.query.QueryResult;

public class EsHandler{

	/**
	 *  创建查询器
	 * @param tenantId
	 * @return
	 */
	public QueryBuilder createQueryBuilder(MultiAttrsObjectPool objectPool,String tenantId) {
		return objectPool.createQueryBuilder().from(tenantId);
	}
	
	/**
	 *   搜索sql
	 * @param 
	 * @return QueryResult
	 */
	public static QueryResult searchSql(MultiAttrsObjectPool objectPool,String sql){
        QueryParser parser = objectPool.createQueryParser();
        MultiAttrsQuery query = parser.parse(sql);
        QueryResult result = query.execute();
        return result;
	}
	

}
