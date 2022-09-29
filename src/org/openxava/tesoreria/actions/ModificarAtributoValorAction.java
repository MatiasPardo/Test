package org.openxava.tesoreria.actions;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;


public abstract class ModificarAtributoValorAction extends ViewBaseAction implements IChainAction{
	
	abstract protected void modificarAtributo(Valor valor);
	
	@Override
	public void execute() throws Exception {
		String error = null;
		try{			
			Valor valor = (Valor)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
			this.modificarAtributo(valor);
			this.commit();
		}		
		catch(Exception e){
			this.rollback();
			error = e.getMessage();
			if (error == null){
				error = e.toString();
			}
		}
		finally {
			this.closeDialog();
			if (!Is.emptyString(error)){
				addError(error);
			}
			else{
				addMessage("Operación Finalizada");
			}
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "ObjetoNegocio.editar";
	}
}
