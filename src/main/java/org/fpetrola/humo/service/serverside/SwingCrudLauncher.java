package org.fpetrola.humo.service.serverside;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.fpetrola.humo.HumoApplicationConfigurator;
import org.fpetrola.humo.service.ExamplesProviderService;

import com.dragome.guia.GuiaServiceLocator;
import com.dragome.render.serverside.swing.SwingGuiaServiceFactory;
import com.dragome.render.serverside.swing.SwingTemplateLoadingStrategy;
import com.dragome.render.serverside.swing.SwingTemplateManager;
import com.dragome.services.ServiceLocator;
import com.dragome.services.serverside.ServerReflectionServiceImpl;
import com.dragome.view.VisualActivity;
import com.dragome.web.config.DomHandlerApplicationConfigurator;

import ar.net.fpetrola.humo.HumoIDE;
import ar.net.fpetrola.humo.VisualTextPaneImpl;
import ar.net.fpetrola.humo.serverside.HumoSwingTemplate;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.InterfaceMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;

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
