package codedriver.framework.process.workcenter.elasticsearch.core;

import com.techsure.multiattrsearch.MultiAttrsObjectPool;

import codedriver.framework.elasticsearch.core.ElasticSearchPoolManager;
import codedriver.framework.elasticsearch.core.IElasticSearchHandler;

public abstract class WorkcenterEsHandlerBase implements IElasticSearchHandler {
	public final static String POOL_NAME = "processtask";
	protected MultiAttrsObjectPool objectPool;
	
	protected MultiAttrsObjectPool getObjectPool() {
		if(objectPool == null) {
			return ElasticSearchPoolManager.getObjectPool(POOL_NAME);
		}
		return objectPool;
	}
	
	public String getPoolName() {
		return POOL_NAME;
	}
}
