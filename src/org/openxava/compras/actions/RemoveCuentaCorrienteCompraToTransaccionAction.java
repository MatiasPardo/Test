package org.openxava.compras.actions;

import org.openxava.actions.IChainAction;
import org.openxava.actions.RemoveElementFromCollectionAction;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.PagoProveedores;

public class RemoveCuentaCorrienteCompraToTransaccionAction extends RemoveElementFromCollectionAction implements IChainAction{
	
	public void execute() throws Exception {
		super.execute();
		
		if (this.getErrors().isEmpty()){
			try{
				PagoProveedores transaccion = (PagoProveedores) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				transaccion.recalcularSaldoAPagar();
				transaccion.grabarTransaccion();
				this.commit();
			}
			catch(Exception e){
				this.rollback();
				this.getMessages().removeAll();
				if (e.getMessage() != null){
					addError(e.getMessage());
				}
				else{
					addError(e.toString());
				}
			}
		}
	}	
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}
