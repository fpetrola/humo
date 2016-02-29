package org.fpetrola.humo.service.serverside;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.fpetrola.humo.HumoApplicationConfigurator;

import com.dragome.guia.GuiaServiceLocator;
import com.dragome.render.serverside.swing.SwingGuiaServiceFactory;
import com.dragome.render.serverside.swing.SwingTemplateLoadingStrategy;
import com.dragome.render.serverside.swing.SwingTemplateManager;
import com.dragome.services.CommandLineParametersHandler;
import com.dragome.services.ServiceLocator;
import com.dragome.services.interfaces.ParametersHandler;
import com.dragome.services.serverside.ServerReflectionServiceImpl;
import com.dragome.view.VisualActivity;

import ar.net.fpetrola.humo.HumoIDE;
import ar.net.fpetrola.humo.VisualTextPaneImpl;
import ar.net.fpetrola.humo.serverside.HumoSwingTemplate;

public class SwingCrudLauncher
{
    public static void main(String[] args)
    {
	new SwingCrudLauncher().run();
    }

    public void run()
    {
	ServiceLocator.getInstance().setConfigurator(new HumoApplicationConfigurator());
	ServiceLocator.getInstance().setReflectionService(new ServerReflectionServiceImpl());
	ServiceLocator.getInstance().setParametersHandler(new CommandLineParametersHandler());
	GuiaServiceLocator.getInstance().setServiceFactory(new SwingGuiaServiceFactory());
	ServiceLocator.getInstance().setLocalExecution(true);

	SwingTemplateManager.addRenderFor(VisualTextPaneImpl.class, SwingTextPaneRenderer.class);
	JFrame jFrame= new JFrame();
	JPanel panel= new HumoSwingTemplate().createEnvironment(jFrame);
	SwingTemplateLoadingStrategy.mainPanel= panel;

	jFrame.pack();
	jFrame.setVisible(true);

	VisualActivity activity= new HumoIDE();
	activity.onCreate();
    }
}
