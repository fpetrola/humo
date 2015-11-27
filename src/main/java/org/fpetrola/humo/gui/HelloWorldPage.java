/*******************************************************************************
 * Copyright (c) 2011-2014 Fernando Petrola
 * 
 *  This file is part of Dragome SDK.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package org.fpetrola.humo.gui;

import org.fpetrola.humo.service.HelloWorldService;

import com.dragome.guia.GuiaVisualActivity;
import com.dragome.guia.components.VisualButtonImpl;
import com.dragome.guia.components.VisualLabelImpl;
import com.dragome.guia.components.interfaces.VisualButton;
import com.dragome.guia.components.interfaces.VisualComponent;
import com.dragome.guia.components.interfaces.VisualLabel;
import com.dragome.guia.events.listeners.interfaces.ClickListener;
import com.dragome.web.annotations.PageAlias;

@PageAlias(alias= "hello-world")
public class HelloWorldPage extends GuiaVisualActivity
{
	HelloWorldService helloWorldService= serviceFactory.createSyncService(HelloWorldService.class);

	public void build()
	{
		final VisualLabel<String> label= new VisualLabelImpl<String>("message");
		final VisualButton button= new VisualButtonImpl("button", new ClickListener()
		{
		    public void clickPerformed(VisualComponent aVisualComponent)
		    {
			label.setValue(helloWorldService.getGreetingsFor("World"));
		    }
		});
		
		mainPanel.addChild(label);
		mainPanel.addChild(button);
	}
}
