package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class ConfirmarTransaccionSeleccionadasAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		@SuppressWarnings("rawtypes")
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes != null) {
			if (selectedOnes.length > 0){
				for (int i = 0; i < selectedOnes.length; i++) {
					@SuppressWarnings("rawtypes")
					Map clave = selectedOnes[i];
					Transaccion transaccion = (Transaccion)MapFacade.findEntity(this.getTab().getModelName(), clave);
					try{
						transaccion.confirmarTransaccion();
						this.commit();
						addMessage(transaccion.toString() + " confirmada");
						this.getTab().deselect(clave);
					}
					catch(Exception e){
						this.rollback();
						if (e.getMessage() != null){
							this.addError(e.getMessage());
						}
						else{
							this.addError(e.toString());
						}
					}
				}
			}
			else{
				this.addError("sin_seleccionar_comprobantes");
			}
		}
		else{
			this.addError("sin_seleccionar_comprobantes");
		}		
	}
}
