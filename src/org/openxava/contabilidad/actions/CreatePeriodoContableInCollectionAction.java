package org.openxava.contabilidad.actions;

import org.openxava.actions.*;

public class CreatePeriodoContableInCollectionAction extends CreateNewElementInCollectionAction{

	public void execute() throws Exception {
		super.execute();
		
		this.getCollectionElementView().setEditable(true);			
	}	
}
