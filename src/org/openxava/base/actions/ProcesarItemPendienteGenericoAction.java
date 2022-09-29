package org.openxava.base.actions;

import java.util.*;
import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.util.*;

public abstract class ProcesarItemPendienteGenericoAction extends TabBaseAction implements IChainAction{

	private List<Transaccion> transacciones = new LinkedList<Transaccion>();
	
	public List<Transaccion> getTransacciones(){
		return this.transacciones;
	}
	
	protected abstract void getItemsPendientes(List<IItemPendiente> items);
	
	protected abstract void antesMostrarTransacciones();
		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute() throws Exception {
		List<IItemPendiente> itemsPendientes = new LinkedList<IItemPendiente>();
		this.getItemsPendientes(itemsPendientes);			
		if (!itemsPendientes.isEmpty()){
			Transaccion trOrigen = itemsPendientes.get(0).getPendiente().origen();
			trOrigen.generarTransaccionesDestinoDesdeItems(itemsPendientes, getTransacciones());
			
			this.posGenerarTransacciones();
			
			this.commit();
			
			if (getTransacciones().isEmpty()){
				this.addError("No se generó ningún comprobantes");
			}
			else{
				this.antesMostrarTransacciones();
				if (Is.equalAsString(this.getNextAction(), "Transaccion.editar")){
					// Se muestra la transacción generada
					Transaccion transaccionGenerada = getTransacciones().iterator().next();
					this.showNewView();
					Map key = new HashMap();
					key.put("id", transaccionGenerada.getId());
					getView().setModelName(transaccionGenerada.getClass().getSimpleName());
					getView().setValues(key);
					getView().findObject();                               
		            getView().setKeyEditable(false);
		            
		            String[] controladores = new String[1];
		            controladores[0] = "TransaccionGenerada";			            
		            this.setControllers(controladores);
		            
				}
				else{
					this.addMessage("ejecucion_OK");
					this.addMessage("Comprobantes generados " + new Integer(getTransacciones().size()).toString());
				}
			}
		}
		else{
			this.addError("No hay comprobantes");
		}
	}

	protected void posGenerarTransacciones() {
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
}

