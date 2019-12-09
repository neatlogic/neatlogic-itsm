package codedriver.framework.process.datacube;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;

@RootComponent
public class DataCubeFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static Map<String, IDataCubeHandler> componentMap = new HashMap<String, IDataCubeHandler>();

	public static IDataCubeHandler getComponent(String type) {
		if (!componentMap.containsKey(type) || componentMap.get(type) == null) {
			throw new RuntimeException("找不到类型为：" + type + "的流程组件");
		}
		return componentMap.get(type);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IDataCubeHandler> myMap = context.getBeansOfType(IDataCubeHandler.class);
		for (Map.Entry<String, IDataCubeHandler> entry : myMap.entrySet()) {
			IDataCubeHandler component = entry.getValue();
			if (component.getType() != null) {
				componentMap.put(component.getType(), component);
			}
		}
	}
}
