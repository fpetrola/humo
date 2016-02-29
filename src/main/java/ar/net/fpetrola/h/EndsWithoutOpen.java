package ar.net.fpetrola.h;

final class EndsWithoutOpen implements OpenCloseChecker
{
    public boolean checkOpenClose(boolean openFound, boolean closeFound)
    {
	return !openFound && closeFound;
    }
}