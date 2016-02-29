package ar.net.fpetrola.humo;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import com.dragome.commons.javascript.ScriptHelper;
import com.dragome.guia.GuiaServiceLocator;
import com.dragome.guia.components.interfaces.VisualComponent;
import com.dragome.guia.events.listeners.interfaces.InputListener;
import com.dragome.helpers.DragomeEntityManager;
import com.dragome.model.interfaces.ValueChangeEvent;
import com.dragome.model.interfaces.ValueChangeHandler;
import com.dragome.render.canvas.interfaces.Canvas;
import com.dragome.render.html.renderers.AbstractHTMLComponentRenderer;
import com.dragome.render.html.renderers.MergeableElement;

import ar.net.fpetrola.h.HTMLHumoTextDocumentListener;
import ar.net.fpetrola.humo.delegates.DelegateMultiplexer;

public class HTMLVisualTextPaneRenderer extends AbstractHTMLComponentRenderer<VisualTextPaneImpl<HumoTextDocumentImpl>>
{
    private Map<HumoTextDocument, HTMLHumoTextDocumentListener> listeners= new HashMap<>();

    public Canvas<Element> render(VisualTextPaneImpl<HumoTextDocumentImpl> aVisualTextPane)
    {
	Canvas<Element> canvas= GuiaServiceLocator.getInstance().getTemplateManager().getCanvasFactory().createCanvas();

	canvas.setContent(new MergeableElement()
	{
	    public void mergeWith(final Element textFieldElement)
	    {
		final String id= DragomeEntityManager.add(aVisualTextPane);

		aVisualTextPane.addValueChangeHandler(new ValueChangeHandler<HumoTextDocumentImpl>()
		{

		    public void onValueChange(ValueChangeEvent<HumoTextDocumentImpl> event)
		    {
			HumoTextDocument value= event.getValue();

			DelegateMultiplexer<HumoTextDocument> delegateMultiplexer= (DelegateMultiplexer<HumoTextDocument>) value;

			HTMLHumoTextDocumentListener htmlDocumentListener= listeners.get(value);
			if (htmlDocumentListener == null)
			{
			    htmlDocumentListener= new HTMLHumoTextDocumentListener(delegateMultiplexer.getMainDelegate(), textFieldElement);
			    listeners.put(value, htmlDocumentListener);
			    delegateMultiplexer.addDelegate(htmlDocumentListener);
			}
		    }
		});
		
		aVisualTextPane.addListener(InputListener.class, new InputListener()
		{
			public void inputPerformed(VisualComponent visualComponent)
			{
				ScriptHelper.put("textFieldElement", textFieldElement, this);
				String value= (String) ScriptHelper.eval("textFieldElement.node.value", this);
				System.out.println(value);
			}
		});

		
//		addListeners(aVisualTextPane, textFieldElement);

	    }
	});

	return canvas;
    }
}
