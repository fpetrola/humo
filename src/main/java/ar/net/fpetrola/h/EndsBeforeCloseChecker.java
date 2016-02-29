package ar.net.fpetrola.h;

public class EndsBeforeCloseChecker implements OpenCloseChecker
{
    public boolean checkOpenClose(boolean openFound, boolean closeFound)
    {
	return openFound && !closeFound;
    }
}
