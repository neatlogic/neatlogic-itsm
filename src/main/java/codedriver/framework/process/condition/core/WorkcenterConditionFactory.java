package codedriver.framework.process.condition.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;

@RootComponent
public class WorkcenterConditionFactory extends ApplicationListenerBase{

	private static Map<String, IProcessTaskCondition> conditionComponentMap = new HashMap<>();
	
	public static IProcessTaskCondition getHandler(String name) {
		return conditionComponentMap.get(name);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IProcessTaskCondition> myMap = context.getBeansOfType(IProcessTaskCondition.class);
		for (Map.Entry<String, IProcessTaskCondition> entry : myMap.entrySet()) {
			IProcessTaskCondition column= entry.getValue();
			conditionComponentMap.put(column.getName(), column);
		}
	}
	
	public static Map<String, IProcessTaskCondition> getConditionComponentMap() {
		return conditionComponentMap;
	}

	@Override
	protected void myInit() {
		// TODO Auto-generated method stub
		
	}

}
