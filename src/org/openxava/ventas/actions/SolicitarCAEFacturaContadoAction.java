package org.openxava.ventas.actions;

import org.openxava.validators.*;

public class SolicitarCAEFacturaContadoAction extends SolicitarCAEAction{
	@Override
	public void execute() throws Exception {
		super.execute();
		
		try{
			if (this.getErrors().isEmpty()){
				this.commit();
			}
		}
		catch(Exception ex){
			this.rollback();
			if (ex instanceof ValidationException){
				addErrors(((ValidationException)ex).getErrors());
			}
			else{
				if (ex.getMessage() != null){
					addError(ex.getMessage());
				}
				else{
					addError(ex.toString());
				}
			}
		}
		
		
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "FacturaVentaContado.GenerarReciboCobranza";
		}
		else{
			return super.getNextAction();
		}
	}
}

