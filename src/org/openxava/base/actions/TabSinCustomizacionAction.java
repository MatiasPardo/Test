package org.openxava.base.actions;

import org.openxava.actions.*;

public class TabSinCustomizacionAction extends TabBaseAction{
	
	@Override
	public void execute() throws Exception {
		this.getTab().restoreDefaultProperties();
		this.getTab().setCustomizeAllowed(false);		
	}
}
