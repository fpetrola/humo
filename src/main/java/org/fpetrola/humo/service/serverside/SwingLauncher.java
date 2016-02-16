package org.fpetrola.humo.service.serverside;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.fpetrola.humo.HumoApplicationConfigurator;

import com.dragome.commons.DragomeConfiguratorImplementor;
import com.dragome.services.ServiceLocator;
import com.dragome.web.config.DomHandlerApplicationConfigurator;
import com.dragome.web.serverside.debugging.websocket.ClassTransformerDragomeWebSocketHandler;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;

public class SwingLauncher
{
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(new NimbusLookAndFeel());
		ServiceLocator.getInstance().setConfigurator(new HumoApplicationConfigurator());
		ClassTransformerDragomeWebSocketHandler.executeMethod(SwingLauncher.class.getPackage().getName() + ".SwingCrudLauncher", "run", null);
	}
}
