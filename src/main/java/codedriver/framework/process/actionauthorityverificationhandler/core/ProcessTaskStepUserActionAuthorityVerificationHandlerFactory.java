package codedriver.framework.process.actionauthorityverificationhandler.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.common.RootComponent;
@RootComponent
public class ProcessTaskStepUserActionAuthorityVerificationHandlerFactory extends ApplicationListenerBase {

	private static Map<String, IProcessTaskStepUserActionAuthorityVerificationHandler> handlerMap = new HashMap<>();
	
	public static IProcessTaskStepUserActionAuthorityVerificationHandler getHandler(String action) {
		return handlerMap.get(action);
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IProcessTaskStepUserActionAuthorityVerificationHandler> map = context.getBeansOfType(IProcessTaskStepUserActionAuthorityVerificationHandler.class);
		for(Entry<String, IProcessTaskStepUserActionAuthorityVerificationHandler> entry : map.entrySet()) {
			handlerMap.put(entry.getValue().getAction(), entry.getValue());
		}
	}

	@Override
	protected void myInit() {
		
	}

}
