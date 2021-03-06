/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.integration.maven.webinspect.standalone.scan;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fortify.client.webinspect.api.WebInspectMacroAPI;
import com.fortify.client.webinspect.connection.WebInspectAuthenticatingRestConnection;
import com.fortify.client.wie.api.WIEMacroAPI;
import com.fortify.client.wie.connection.WIEAuthenticatingRestConnection;
import com.fortify.integration.maven.webinspect.WIEConnectionRetrieverMaven;
import com.fortify.util.rest.json.JSONList;
import com.fortify.util.rest.json.JSONMap;
import com.google.common.collect.Lists;

/**
 * Mojo for creating a WebInspect stand-alone scan and optionally running it
 * 
 * @author Ruud Senden
 *
 */
@Mojo(name = "wiCreateScanFromWIEMacros", defaultPhase = LifecyclePhase.NONE, requiresProject = false)
public class WICreateScanFromWIEMacrosMojo extends WICreateScanMojo {
	/**
     * Root URL of the WebInspect Enterprise scan API instance to be used
     */
    @Parameter(property = "com.fortify.wie.connection", required = true)
    private WIEConnectionRetrieverMaven wieConnRetriever;
    
    protected WIEAuthenticatingRestConnection getWIEConnection() {
    	return wieConnRetriever.getConnection();
    }

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		uploadMacrosFromWIEtoWebInspect();
		super.execute();
	}

	private void uploadMacrosFromWIEtoWebInspect() {
		WIEAuthenticatingRestConnection wie = getWIEConnection();
		WebInspectAuthenticatingRestConnection wi = getWebInspectConnection();
		
		List<String> macroNames = Lists.newArrayList(getWorkflowMacros());
		macroNames.add(getLoginMacro());
		
		JSONList macros = wie.api(WIEMacroAPI.class).queryMacros().names(macroNames.toArray(new String[]{})).build().getAll();
		for ( JSONMap macro : macros.asValueType(JSONMap.class) ) {
			byte[] macroData = wie.api(WIEMacroAPI.class).getMacroData(macro.get("id", String.class));
			wi.api(WebInspectMacroAPI.class).uploadMacro(macro.get("name", String.class), macroData);
		}
		
	}
}
