package ar.net.fpetrola.humo;

public class DebuggingParserListener implements ParserListener
{
    private volatile boolean pause;
    private volatile boolean step;

    public void startProductionCreation(CharSequence aName)
    {
	performStep();
    }

    public void endProductionCreation(CharSequence aName, CharSequence aValue)
    {
	performStep();
    }

    private void performStep()
    {
	while (pause)
	    ;

	if (step)
	    pause= true;
    }

    public void stop()
    {
	pause= true;
	step= false;
    }

    public void continueExecution()
    {
	step= false;
	pause= false;
    }

    public void step()
    {
	step= true;
	pause= false;
    }
}
