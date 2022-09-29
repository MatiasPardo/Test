package org.openxava.tesoreria.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;

public class ConfirmarTransferirValoresAction extends TabBaseAction {

	@Override
	public void execute() throws Exception {
		String id = this.getView().getValueString("tesoreria.id");
		if (!Is.emptyString(id)){
			Integer transferidos = 0;
			for (Map<?, ?> key: getTab().getSelectedKeys()) { 
				Valor valor = (Valor) MapFacade.findEntity("Valor", key);
				Tesoreria tesoreria = (Tesoreria)XPersistence.getManager().find(Tesoreria.class, id);
				try{
					TransferenciaFinanzas tr = valor.crearTransferenciaValores(tesoreria);
					tr.confirmarTransaccion();
					this.commit();
					transferidos++;					
				}
				catch(Exception e){
					this.rollback();
					this.addError(e.getMessage() + ": " + valor.toString());
				}
			}
			if (this.getErrors().isEmpty()){
				addMessage("ejecucion_OK");
			}
			else{
				if (transferidos > 0){
					addWarning("Se pudieron transferir correctamente " + transferidos.toString() + " items");
				}
			}
		}
		else{		
			addError("Falta asignar la caja");
		}	
		this.closeDialog();
		this.getTab().deselectAll();
	}
}
