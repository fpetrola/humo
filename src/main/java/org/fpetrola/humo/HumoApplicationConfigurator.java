package org.fpetrola.humo;

import com.dragome.commons.DragomeConfiguratorImplementor;
import com.dragome.methodlogger.MethodLoggerConfigurator;
import com.dragome.web.config.DomHandlerApplicationConfigurator;

import ar.net.fpetrola.humo.Controls;

@DragomeConfiguratorImplementor
public class HumoApplicationConfigurator extends DomHandlerApplicationConfigurator
{
    private MethodLoggerConfigurator methodLoggerConfigurator;

    public HumoApplicationConfigurator()
    {
	methodLoggerConfigurator= new MethodLoggerConfigurator(Controls.class.getPackage().getName());
	methodLoggerConfigurator.setEnabled(true);

	init(methodLoggerConfigurator);
    }
}