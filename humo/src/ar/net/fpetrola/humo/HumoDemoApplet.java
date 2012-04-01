package ar.net.fpetrola.humo;

import java.applet.Applet;

public class HumoDemoApplet extends Applet
{
    public void init()
    {
	try
	{
	    HumoTester.main(new String[] { "/prueba+de+objetos2.humo" });
	}
	catch (Exception e)
	{
	    throw new RuntimeException(e);
	}
    }
}
