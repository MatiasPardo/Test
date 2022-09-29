package org.openxava.base.actions;

import org.openxava.actions.*;

public class GrabarObjetoNegocioAction extends SaveAction implements IChainAction{

	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "ObjetoNegocio.editar";
		}
		else{
			return null;
		}
	}

}
