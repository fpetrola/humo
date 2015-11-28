package ar.net.fpetrola.humo.delegates;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;

import com.dragome.guia.events.listeners.interfaces.ListenerMultiplexer;
import com.dragome.guia.helper.collections.CollectionHandler;
import com.dragome.guia.helper.collections.ItemInvoker;
import com.dragome.model.IndetifiableProxy;

public class ProxyBasedDelegateMultiplexer<T> implements InvocationHandler, DelegateMultiplexer<T>, IndetifiableProxy<EventListener>
{
    protected CollectionHandler<T> collectionHandler;
    protected Class<? extends EventListener> proxiedClass;
    private T mainDelegate;

    public ProxyBasedDelegateMultiplexer(Class<? extends EventListener> aClass)
    {
	proxiedClass= aClass;
	collectionHandler= new CollectionHandler<T>();
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable
    {
	Object[] result= new Object[1];
	
	if (method.getName().equals("addDelegate"))
	{
	    addDelegate((T) args[0]);
	}
	else if (method.getName().equals("removeDelegate"))
	{
	    removeDelegate((T) args[0]);
	}
	else if (method.getName().equals("setMainDelegate"))
	{
	    setMainDelegate((T) args[0]);
	}
	else
	{
	    collectionHandler.forAll(new ItemInvoker<T>()
	    {
		public void invoke(T item)
		{
		    try
		    {
			Object currentResult= method.invoke(item, args);
			if (mainDelegate == item)
			    result[0]= currentResult;
		    }
		    catch (Exception e)
		    {
			throw new RuntimeException(e);
		    }
		}
	    });
	}

	return result[0];
    }

    public void addDelegate(T aDelegate)
    {
	collectionHandler.add(aDelegate);
    }

    public void removeDelegate(T aDelegate)
    {
	collectionHandler.remove(aDelegate);
    }

    public void setMainDelegate(T aDelegate)
    {
	mainDelegate= aDelegate;
    }

    public static <T> T createDelegateMultiplexer(Class<T> aType)
    {
	ProxyBasedDelegateMultiplexer<T> invocationHandler= new ProxyBasedDelegateMultiplexer(aType);
	T delegateMultiplexer= (T) Proxy.newProxyInstance(ProxyBasedDelegateMultiplexer.class.getClassLoader(), new Class<?>[] { DelegateMultiplexer.class, aType }, invocationHandler);
	return delegateMultiplexer;
    }

    public Class<? extends EventListener> getProxiedClazz()
    {
	return proxiedClass;
    }

    public void setProxiedClazz(Class<? extends EventListener> clazz)
    {
	this.proxiedClass= clazz;
    }

}