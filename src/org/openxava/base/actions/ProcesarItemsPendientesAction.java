package org.openxava.base.actions;

import java.util.List;

import org.openxava.base.model.IItemPendiente;
import org.openxava.validators.ValidationException;

public class ProcesarItemsPendientesAction extends ProcesarItemPendienteGenericoAction{

	@Override
	protected void getItemsPendientes(List<IItemPendiente> items) {
		@SuppressWarnings("unchecked")
		List<IItemPendiente> list = (List<IItemPendiente>)this.getRequest().getAttribute("ItemsPendientes");
		if (list != null){
			items.addAll(list);
		}
		else{
			throw new ValidationException("Falta asigar el parámetro ItemsPendientes");
		}
	}

	@Override
	protected void antesMostrarTransacciones() {		
	}
	
	@Override
	public String getNextAction() throws Exception {
		return null;		
	}
}

