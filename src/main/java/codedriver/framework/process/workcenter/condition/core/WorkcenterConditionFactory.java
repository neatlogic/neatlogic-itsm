package codedriver.framework.process.workcenter.condition.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;

@RootComponent
public class WorkcenterConditionFactory extends ApplicationListenerBase{

	private static Map<String, IWorkcenterCondition> conditionComponentMap = new HashMap<>();
	
	public static IWorkcenterCondition getHandler(String name) {
		return conditionComponentMap.get(name);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IWorkcenterCondition> myMap = context.getBeansOfType(IWorkcenterCondition.class);
		for (Map.Entry<String, IWorkcenterCondition> entry : myMap.entrySet()) {
			IWorkcenterCondition column= entry.getValue();
			conditionComponentMap.put(column.getName(), column);
		}
	}
	
	public static Map<String, IWorkcenterCondition> getConditionComponentMap() {
		return conditionComponentMap;
	}

	@Override
	protected void myInit() {
		// TODO Auto-generated method stub
		
	}

}
