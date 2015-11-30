package ar.net.fpetrola.humo.delegates;

public interface DelegateMultiplexer<DelegateType>
{
    void addDelegate(DelegateType aDelegate);
    void removeDelegate(DelegateType aDelegate);
    void setMainDelegate(DelegateType aDelegate);
    DelegateType getMainDelegate();
}
