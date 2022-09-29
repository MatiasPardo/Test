package org.openxava.ventas.actions;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;

public class RemoveSelectedCuentaCorrienteVentaToTransaccionAction extends RemoveSelectedInCollectionAction implements IChainAction{

	public void execute() throws Exception {
		super.execute();
		
		if (this.getErrors().isEmpty()){
			try{
				IngresoValores transaccion = (IngresoValores) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
				transaccion.recalcularSaldoACobrar();;
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
