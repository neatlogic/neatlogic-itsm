package codedriver.module.process.formattribute.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;

@RootComponent
public class FormAttributeHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, IFormAttributeHandler> handlerMap = new HashMap<>();


	public static IFormAttributeHandler getHandler(String type) {
		return handlerMap.get(type);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IFormAttributeHandler> myMap = context.getBeansOfType(IFormAttributeHandler.class);
		for (Map.Entry<String, IFormAttributeHandler> entry : myMap.entrySet()) {
			IFormAttributeHandler handler = entry.getValue();
			if (handler.getType() != null) {
				handlerMap.put(handler.getType(), handler);
			}
		}
	}
}
