package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class GrabarTransaccionAction extends SaveAction implements IChainAction{
	
	//private static Log log = LogFactory.getLog(GrabarTransaccionAction.class);
	private boolean editarTransaccion = false;
	
	@Override
	public void execute() throws Exception {
		
		Messages errors = MapFacade.validate(getView().getModelName(), getView().getValues());
		if (errors.contains()) throw new ValidationException(errors);
		
		this.setRefreshAfter(true);
		this.setResetAfter(false);	
		
		super.execute();
		
		if (this.getErrors().isEmpty()){
			editarTransaccion = true;
			try{
				Transaccion tr = (Transaccion)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				tr.grabarTransaccion();
				this.commit();
				
				if (tr.refrescarColecciones()){
					this.getView().refreshCollections();
				}
			}
			catch(ValidationException e){
				this.rollback();
				
				addError(e.getMessage());
				this.getMessages().removeAll();
			}
			catch(Exception e){
				this.rollback();
				
				if (e.getMessage() != null){
					addError(e.getMessage());
				}
				else{
					addError(e.toString());
				}
				this.getMessages().removeAll();
			}
		}
		
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.editarTransaccion){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}

