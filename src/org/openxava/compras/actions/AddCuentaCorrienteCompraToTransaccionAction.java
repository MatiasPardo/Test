package org.openxava.compras.actions;

import org.openxava.base.actions.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.PagoProveedores;

public class AddCuentaCorrienteCompraToTransaccionAction extends AddItemToTransaccionAction{
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
}
