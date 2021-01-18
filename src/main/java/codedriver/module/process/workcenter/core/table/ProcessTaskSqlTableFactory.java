package codedriver.module.process.workcenter.core.table;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Map;

@RootComponent
public class ProcessTaskSqlTableFactory extends ApplicationListenerBase{

	public static Map<String, ISqlTable> tableComponentMap = new HashMap<>();
	
	public static ISqlTable getHandler(String name) {
		return tableComponentMap.get(name);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, ISqlTable> myMap = context.getBeansOfType(ISqlTable.class);
		for (Map.Entry<String, ISqlTable> entry : myMap.entrySet()) {
			ISqlTable table= entry.getValue();
			tableComponentMap.put(table.getName(), table);
		}
	}

	@Override
	protected void myInit() {
		// TODO Auto-generated method stub
		
	}

}
