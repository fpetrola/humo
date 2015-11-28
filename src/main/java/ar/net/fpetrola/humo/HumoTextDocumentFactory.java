package ar.net.fpetrola.humo;

import ar.net.fpetrola.humo.delegates.DelegateMultiplexer;
import ar.net.fpetrola.humo.delegates.ProxyBasedDelegateMultiplexer;

public class HumoTextDocumentFactory
{
    public static HumoTextDocument createTextDocument()
    {
	HumoTextDocument humoTextDocument= ProxyBasedDelegateMultiplexer.createDelegateMultiplexer(HumoTextDocument.class);
	DelegateMultiplexer<HumoTextDocument> delegateMultiplexer= (DelegateMultiplexer<HumoTextDocument>) humoTextDocument;
	
	HumoTextDocumentImpl mainDelegate= new HumoTextDocumentImpl();

	delegateMultiplexer.addDelegate(mainDelegate);
	
	delegateMultiplexer.setMainDelegate(mainDelegate);
	
	return humoTextDocument;
    }
}
