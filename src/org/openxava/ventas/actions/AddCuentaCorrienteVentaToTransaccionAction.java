package org.openxava.ventas.actions;

import org.openxava.base.actions.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;

public class AddCuentaCorrienteVentaToTransaccionAction  extends AddItemToTransaccionAction{
	public void execute() throws Exception {
		super.execute();
		
		if (this.getErrors().isEmpty()){
			try{
				IngresoValores transaccion = (IngresoValores) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				transaccion.recalcularSaldoACobrar();
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
