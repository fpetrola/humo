package ar.net.fpetrola.humo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

public class ThreadSafeActionListener implements ActionListener
{
    private ActionListener actionListener;

    public ThreadSafeActionListener(ActionListener actionListener)
    {
	this.actionListener= actionListener;
    }

    public void actionPerformed(final ActionEvent e)
    {
	SwingUtilities.invokeLater(new Runnable()
	{
	    public void run()
	    {
		actionListener.actionPerformed(e);
	    }
	});
    }
}
