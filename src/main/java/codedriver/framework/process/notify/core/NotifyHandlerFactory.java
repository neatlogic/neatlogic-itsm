package codedriver.framework.process.notify.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;
import codedriver.framework.common.dto.ValueTextVo;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-09 10:04
 **/
@RootComponent
public class NotifyHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, INotifyHandler> notifyHandlerMap = new HashMap<>();

	private static List<ValueTextVo> notifyHandlerTypeList = new ArrayList<>();

	public static List<ValueTextVo> getNotifyHandlerTypeList() {
		return notifyHandlerTypeList;
	}

	public static INotifyHandler getHandler(String handler) {
		if (notifyHandlerMap.containsKey(handler)) {
			return notifyHandlerMap.get(handler);
		}
		return null;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, INotifyHandler> myMap = context.getBeansOfType(INotifyHandler.class);
		for (Map.Entry<String, INotifyHandler> entry : myMap.entrySet()) {
			INotifyHandler plugin = entry.getValue();
			if (plugin.getId() != null) {
				notifyHandlerMap.put(plugin.getId(), plugin);
				notifyHandlerTypeList.add(new ValueTextVo(plugin.getId(), plugin.getName()));
			}
		}
	}
}
