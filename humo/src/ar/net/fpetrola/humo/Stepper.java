package ar.net.fpetrola.humo;

public class Stepper
{
    protected volatile boolean pause;

    public void continueExecution()
    {
	pause= false;
    }

    protected void pause()
    {
	pause= true;
	while (pause == true)
	    ;
    }
}
