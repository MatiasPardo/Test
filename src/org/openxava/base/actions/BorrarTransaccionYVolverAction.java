package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.util.*;

public class BorrarTransaccionYVolverAction extends DeleteAction implements IChainAction{

	private String accionVolver = "";
	
	public String getAccionVolver() {
		if (Is.emptyString(accionVolver)){
			return "TransaccionGenerada.return";
		}
		else{
			return accionVolver;
		}
	}

	public void setAccionVolver(String accionVolver) {
		this.accionVolver = accionVolver;
	}



	@Override
	public String getNextAction() throws XavaException {
		if (this.getErrors().isEmpty()){
			return this.getAccionVolver();
		}
		else{
			return super.getNextAction();
		}
	}
	
}
