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
package org.fpetrola.humo.service.serverside;

import java.util.Scanner;

import org.fpetrola.humo.service.ExamplesProviderService;

import ar.net.fpetrola.humo.HumoIDE;

public class ExamplesProviderServiceImpl implements ExamplesProviderService
{
    public String getExample(String name)
    {
	StringBuilder sourcecode= new StringBuilder(new Scanner(HumoIDE.class.getResourceAsStream("/" + name)).useDelimiter("\\Z").next());
	return sourcecode.toString();
    }
}
