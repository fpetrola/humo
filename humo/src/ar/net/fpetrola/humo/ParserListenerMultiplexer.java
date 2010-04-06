package ar.net.fpetrola.humo;

public class ParserListenerMultiplexer implements ParserListener
{
    private ParserListener[] parserListeners;

    public ParserListenerMultiplexer(ParserListener... parserListener)
    {
        this.parserListeners = parserListener;
    }

    public void endProductionCreation(String name, String value)
    {
        for (ParserListener parserListener : parserListeners)
            parserListener.endProductionCreation(name, value);
    }

    public void startProductionCreation(String name)
    {
        for (ParserListener parserListener : parserListeners)
            parserListener.startProductionCreation(name);
    }
}
