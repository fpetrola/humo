package ar.net.fpetrola.humo;

import org.w3c.dom.Element;

import com.dragome.guia.GuiaServiceLocator;
import com.dragome.helpers.DragomeEntityManager;
import com.dragome.model.interfaces.ValueChangeEvent;
import com.dragome.model.interfaces.ValueChangeHandler;
import com.dragome.render.canvas.interfaces.Canvas;
import com.dragome.render.html.renderers.AbstractHTMLComponentRenderer;
import com.dragome.render.html.renderers.MergeableElement;

import ar.net.fpetrola.humo.delegates.DelegateMultiplexer;

public class HTMLVisualTextPaneRenderer extends AbstractHTMLComponentRenderer<VisualTextPaneImpl<HumoTextDocumentImpl>>
{
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
			HumoTextDocumentImpl value= event.getValue();

			DelegateMultiplexer<HumoTextDocument> delegateMultiplexer= (DelegateMultiplexer<HumoTextDocument>) value;
			delegateMultiplexer.addDelegate(new HTMLHumoTextDocumentListener(delegateMultiplexer.getMainDelegate(), textFieldElement));

			setElementInnerHTML(textFieldElement, value.getText());
		    }
		});

		String value= aVisualTextPane.getValue().getText();
		textFieldElement.setAttribute("onchange", "EventDispatcher.setText(this.id, this.value);stopEvent(event);");

		setElementInnerHTML(textFieldElement, value);
	    }
	});

	return canvas;
    }
}
