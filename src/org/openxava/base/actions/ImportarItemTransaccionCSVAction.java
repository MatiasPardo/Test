package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.*;

public class ImportarItemTransaccionCSVAction extends GrabarTransaccionAction implements ILoadFileAction{

	boolean nullNextAction = false;
	
	@Override
	public void execute() throws Exception {
		
		if (getView().isEditable()){
			
			super.execute();
				
			if (this.getErrors().isEmpty()){
				this.commit(); // así ya esta en la base de datos la transacción
				
				Transaccion transaccion = (Transaccion)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				if (!transaccion.soloLectura()){
					nullNextAction = true;
					showDialog();
				}
				else{
					throw new ValidationException("No se puede modificar el comprobante");
				}
			}
			else{
				throw new ValidationException("No se puede importar desde CSV.");
			}
		}
		else{
			throw new ValidationException("No se puede modificar el comprobante");
		}
	}

	@Override
	public String[] getNextControllers() throws Exception {
		return new String [] { "ProcesarCSVItemTransaccion" };
	}

	@Override
	public String getCustomView() throws Exception {
		return "xava/editors/chooseFile";
	}

	@Override
	public boolean isLoadFile() {
		return true;
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.nullNextAction){
			return null;
		}
		else{
			return super.getNextAction();
		}
	}
}
