package org.openxava.base.actions;

import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.model.MapFacade;

public class AnularTransaccionSeleccionadasAction extends TabBaseAction{
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
						transaccion.anularTransaccion();
						this.commit();
						addMessage(transaccion.toString() + " anulada");
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
