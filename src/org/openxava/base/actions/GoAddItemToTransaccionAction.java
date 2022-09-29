package org.openxava.base.actions;

import org.openxava.actions.*;

public class GoAddItemToTransaccionAction extends GoAddElementsToCollectionAction{

	@Override
	public String getNextController() { 
		return "AgregarItemTransaccion"; 
	} 
}
