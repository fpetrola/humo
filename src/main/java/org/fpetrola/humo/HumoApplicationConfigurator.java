package org.fpetrola.humo;

import com.dragome.callbackevictor.CallbackEvictorConfigurator;
import com.dragome.commons.DragomeConfiguratorImplementor;
import com.dragome.commons.ExecutionHandler;
import com.dragome.methodlogger.MethodLoggerConfigurator;
import com.dragome.web.config.DomHandlerApplicationConfigurator;

import ar.net.fpetrola.humo.Controls;

@DragomeConfiguratorImplementor
public class HumoApplicationConfigurator extends DomHandlerApplicationConfigurator
{
    private CallbackEvictorConfigurator callbackEvictorConfigurator;
    private MethodLoggerConfigurator methodLoggerConfigurator;

    public HumoApplicationConfigurator()
    {
	String packageName= Controls.class.getPackage().getName();
	
	callbackEvictorConfigurator= new CallbackEvictorConfigurator();
	callbackEvictorConfigurator.setEnabled(true);
	callbackEvictorConfigurator.getIncludedPaths().add(packageName);

	methodLoggerConfigurator= new MethodLoggerConfigurator(packageName);
	methodLoggerConfigurator.setEnabled(true);

	init(callbackEvictorConfigurator, methodLoggerConfigurator);
	
    }

    public ExecutionHandler getExecutionHandler()
    {
	return callbackEvictorConfigurator.getExecutionHandler();
    }
}