package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.validators.ValidationException;


public class GrabarItemTransaccionAction extends SaveElementInCollectionAction implements IChainAction{
	
	@Override
	public void execute() throws Exception{
		if (!getCollectionElementView().isKeyEditable() && !getCollectionElementView().getParent().isKeyEditable()){
			Transaccion tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			tr.verificarEstadoParaModificarTr();
		}
		
		ItemTransaccionView itemTRView = null;
		if (this.isCloseDialogDisallowed()){
			itemTRView = ItemTransaccion.itemTransaccionView(this.getCollectionElementView());
			itemTRView.copiarValoresItemsPrevioGrabar(this.getCollectionElementView());
		}
		
		super.execute();
		
		Transaccion tr = null;
		try{
			tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			tr.grabarTransaccion();
			this.commit();
		}
		catch(Exception e){
			this.rollback();
			if (e.getMessage() != null){
				addError(e.getMessage());
			}
			else{
				addError(e.toString());
			}
		}
		
		if (this.isCloseDialogDisallowed() && (this.getErrors().isEmpty())){
			// sigue generando items
			try{
				itemTRView.copiarValoresCabecera(this.getView(), this.getCollectionElementView(), tr);
			}
			catch(Exception e){
				throw new ValidationException("Error al copiar valores de la cabecera a los items: " + e.toString());
			}
			
		}
	}
		
	@Override
	public String getNextAction() throws Exception{
		if (!this.getErrors().isEmpty()){
			return "Transaccion.editar";
		}
		else{
			if (this.isCloseDialogDisallowed()){
				return null;
			}
			else{
				// se tiene que refrescar toda la transacción después de grabar y cerrar un item
				return "Transaccion.editar";
			}
		}
	}
}
