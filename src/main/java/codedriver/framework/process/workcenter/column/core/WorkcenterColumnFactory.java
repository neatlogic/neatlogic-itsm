package codedriver.framework.process.workcenter.column.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;

@RootComponent
public class WorkcenterColumnFactory extends ApplicationListenerBase{

	public static Map<String, IWorkcenterColumn> columnComponentMap = new HashMap<>();
	
	public static IWorkcenterColumn getHandler(String name) {
		name = name.toUpperCase();
		return columnComponentMap.get(name);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IWorkcenterColumn> myMap = context.getBeansOfType(IWorkcenterColumn.class);
		for (Map.Entry<String, IWorkcenterColumn> entry : myMap.entrySet()) {
			IWorkcenterColumn column= entry.getValue();
			columnComponentMap.put(column.getName().toUpperCase(), column);
		}
	}

	@Override
	protected void myInit() {
		// TODO Auto-generated method stub
		
	}

}
