package ar.net.fpetrola.humo;

import com.dragome.commons.ExecutionHandler;

public class Stepper
{
    protected volatile boolean pause;
    private ExecutionHandler executionHandler;

    public Stepper(ExecutionHandler executionHandler)
    {
	this.executionHandler= executionHandler;
    }

    public void continueExecution()
    {
//	if (pause)
	{
	    pause= false;
	    executionHandler.continueExecution();
	}
    }

    protected void pause()
    {
//	if (!pause)
	{
	    pause= true;
	    executionHandler.suspendExecution();
	    //	while (pause == true)
	    //	    ;
	}
    }
}
