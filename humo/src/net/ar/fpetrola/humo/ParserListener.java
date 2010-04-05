package net.ar.fpetrola.humo;

public interface ParserListener
{
    void startProductionCreation(String aName);
    void endProductionCreation(String aName, String aValue);
}
