package org.fpetrola.humo.service.serverside;

import java.util.Arrays;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.fpetrola.humo.HumoApplicationConfigurator;

import com.dragome.services.ServiceLocator;
import com.dragome.web.serverside.debugging.websocket.ClassTransformerDragomeWebSocketHandler;

public class SwingLauncher
{
    public static void main(String[] args) throws Exception
    {
	UIManager.setLookAndFeel(new NimbusLookAndFeel());
	ServiceLocator.getInstance().setConfigurator(new HumoApplicationConfigurator());
	System.setProperty("parameters", Arrays.toString(args));
	ClassTransformerDragomeWebSocketHandler.executeMethod(SwingLauncher.class.getPackage().getName() + ".SwingCrudLauncher", "run", null);
    }
}
