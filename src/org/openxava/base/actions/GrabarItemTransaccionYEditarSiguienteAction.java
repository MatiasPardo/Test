package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class GrabarItemTransaccionYEditarSiguienteAction extends SaveElementInCollectionAction implements IChainAction, IChainActionWithArgv{
	
	private int nextEditingRow = Integer.MIN_VALUE;
	private int collectionSize = 0;
		
	@Override
	public void execute() throws Exception {
		if (!getCollectionElementView().isKeyEditable() && !getCollectionElementView().getParent().isKeyEditable()){
			Transaccion tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			tr.verificarEstadoParaModificarTr();
		}
		
		this.nextEditingRow = this.getCollectionElementView().getCollectionEditingRow() + 1;
		this.collectionSize = this.getCollectionElementView().getCollectionSize();
		super.execute();
		
		try{
			Transaccion tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
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
	}

	@Override
	public String getNextAction() throws Exception {
		String nextAction = null;
		if(!this.getErrors().isEmpty()){
			nextAction = "Transaccion.editar";
		}
		else{
			if (editarProximo()){
				nextAction = "ItemTransaccion.editNext";
			}
			else{
				if (!this.isCloseDialogDisallowed()){
					// se tiene que refrescar toda la transacción después de grabar y cerrar un item
					nextAction = "Transaccion.editar";
				}
			}
		}
		return nextAction;
	}
	
	@Override
	public String getNextActionArgv() throws Exception {
		if (editarProximo() && this.getErrors().isEmpty()){
			return "row=" + Integer.toString(nextEditingRow);
		}
		else{
			return null;
		}
	}
	
	@Override
	public boolean isCloseDialogDisallowed() {
		if (editarProximo()){
			return true;
		}
		else{
			return super.isCloseDialogDisallowed();
		}
	}
	
	protected boolean editarProximo(){
		boolean editar = false;
		if (this.nextEditingRow > 0){
			if (this.nextEditingRow <= (this.collectionSize - 1)){
				editar = true;
			}
		}
		return editar;
	}
	
	
}
