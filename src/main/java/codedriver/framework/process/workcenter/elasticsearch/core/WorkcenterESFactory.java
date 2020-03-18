package codedriver.framework.process.workcenter.elasticsearch.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.process.exception.workcenter.WorkcenterHandlerNotFoundException;

public class WorkcenterESFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, IWorkcenterESHandler> handlerMap = new HashMap<String, IWorkcenterESHandler>();
	
	public static IWorkcenterESHandler getAuthInstance(String handler) {
		return handlerMap.get(handler);
	}

	public static IWorkcenterESHandler getHandler(String handler) {
		if (!handlerMap.containsKey(handler) || handlerMap.get(handler) == null) {
			throw new WorkcenterHandlerNotFoundException(handler);
		}
		return handlerMap.get(handler);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IWorkcenterESHandler> myMap = context.getBeansOfType(IWorkcenterESHandler.class);
		for (Map.Entry<String, IWorkcenterESHandler> entry : myMap.entrySet()) {
			try {
				IWorkcenterESHandler handler = entry.getValue();
				handlerMap.put(handler.getHandler(), handler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
