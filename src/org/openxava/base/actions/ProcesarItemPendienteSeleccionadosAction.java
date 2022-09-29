package org.openxava.base.actions;

import java.lang.reflect.*;
import java.util.*;

import javax.ejb.*;

import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.Is;
import org.openxava.validators.*;

public class ProcesarItemPendienteSeleccionadosAction extends ProcesarItemPendienteGenericoAction {
	
	private String metodoInstanciarItemPendiente = null;
			
	public String getMetodoInstanciarItemPendiente() {
		return metodoInstanciarItemPendiente;
	}

	public void setMetodoInstanciarItemPendiente(String metodoInstanciarItemPendiente) {
		this.metodoInstanciarItemPendiente = metodoInstanciarItemPendiente;
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.getTransacciones().size() == 1){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void agregarItemPendiente(Map clave, List<IItemPendiente> items){
		try {
			Object object = MapFacade.findEntity(this.getTab().getModelName(), clave);
			IItemPendiente itemPendiente = null;
			if (!Is.emptyString(this.getMetodoInstanciarItemPendiente())){
				Method method = object.getClass().getMethod(this.getMetodoInstanciarItemPendiente());
				itemPendiente = (IItemPendiente)method.invoke(object);
				items.add(itemPendiente);
			}
			else if (object instanceof IItemPendiente){
				items.add((IItemPendiente)object);
			}
			else{
				throw new ValidationException("Funcionalidad definir un método de instanciación del pendiente");
			}
		} catch (ObjectNotFoundException e) {
			throw new ValidationException("No se encontró el comprobante de clave " + clave.toString());
		} catch (Exception e) {
			throw new ValidationException("Error al buscar el comprobante " + clave.toString());	
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void getItemsPendientes(List<IItemPendiente> items) {
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				agregarItemPendiente(clave, items);	
			}			
		}	
		
	}

	@Override
	protected void antesMostrarTransacciones() {
		getTab().deselectAll();                                                  
		resetDescriptionsCache();		
	}	
}
