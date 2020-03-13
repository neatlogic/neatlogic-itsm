package codedriver.framework.process.audithandler.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;
@RootComponent
public class ProcessTaskStepAuditDetailHandlerFactory extends ApplicationListenerBase{

	private static Map<String, IProcessTaskStepAuditDetailHandler> handlerMap = new HashMap<>();
	
	public static IProcessTaskStepAuditDetailHandler getHandler(String type) {
		return handlerMap.get(type);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IProcessTaskStepAuditDetailHandler> map = context.getBeansOfType(IProcessTaskStepAuditDetailHandler.class);
		for(Entry<String, IProcessTaskStepAuditDetailHandler> entry  : map.entrySet()) {
			IProcessTaskStepAuditDetailHandler handler = entry.getValue();
			handlerMap.put(handler.getType(), handler);
		}
	}

	@Override
	protected void myInit() {
		
	}

}
