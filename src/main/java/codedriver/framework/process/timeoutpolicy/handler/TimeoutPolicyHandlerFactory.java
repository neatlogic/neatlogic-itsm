package codedriver.framework.process.timeoutpolicy.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;

@RootComponent
public class TimeoutPolicyHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {

	private static Map<String, ITimeoutPolicyHandler> componentMap = new HashMap<String, ITimeoutPolicyHandler>();

	public static ITimeoutPolicyHandler getHandler(String name) {
		if (!componentMap.containsKey(name) || componentMap.get(name) == null) {
			throw new RuntimeException("找不到类型为：" + name + "的超时策略");
		}
		return componentMap.get(name);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, ITimeoutPolicyHandler> myMap = context.getBeansOfType(ITimeoutPolicyHandler.class);
		for (Map.Entry<String, ITimeoutPolicyHandler> entry : myMap.entrySet()) {
			ITimeoutPolicyHandler component = entry.getValue();
			if (component.getType() != null) {
				componentMap.put(component.getType(), component);
			}
		}
	}
}
