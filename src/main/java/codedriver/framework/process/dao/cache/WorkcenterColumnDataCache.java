package codedriver.framework.process.dao.cache;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

public class WorkcenterColumnDataCache {
	private static CacheManager CACHE_MANAGER;

	private synchronized static Ehcache getCache() {
		TenantContext tenantContext = TenantContext.get();
		String tenant = null;
		String cacheName = StringUtils.EMPTY;
		if (tenantContext != null) {
			tenant = tenantContext.getTenantUuid();
		}
		if (StringUtils.isNotBlank(tenant)) {
			cacheName = tenant+":WorkcenterColumnCache";
		}
		if (CACHE_MANAGER == null) {
			CacheConfiguration cacheConfiguration = new CacheConfiguration();
			cacheConfiguration.setName(cacheName);
			cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
			cacheConfiguration.setMaxEntriesLocalHeap(1000);
			cacheConfiguration.internalSetTimeToIdle(300);
			cacheConfiguration.internalSetTimeToLive(600);
			Configuration config = new Configuration();
			config.addCache(cacheConfiguration);
			CACHE_MANAGER = CacheManager.newInstance(config);
		}
		if (!CACHE_MANAGER.cacheExists(cacheName)) {
			CACHE_MANAGER.addCache(cacheName);
		}
		return CACHE_MANAGER.getEhcache(cacheName);
	}

	public static void addItem(String name,String displayName) {
		getCache().put(new Element(name, displayName));
	}

	public static Object getItem(String name) {
		Element cachedElement = getCache().get(name);
		if (cachedElement == null) {
			return null;
		}
		return cachedElement.getObjectValue();
	}

}
