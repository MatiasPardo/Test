package org.openxava.base.actions;

import org.openxava.actions.IChainAction;
import org.openxava.actions.TabBaseAction;

public class DeseleccionarYCancelarAction extends TabBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		this.getTab().deselectAll();		
	}

	@Override
	public String getNextAction() throws Exception {
		return "Close.close";
	}

}
