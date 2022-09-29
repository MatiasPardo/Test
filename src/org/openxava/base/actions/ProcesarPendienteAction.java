package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;


public abstract class ProcesarPendienteAction extends TabBaseAction implements IChainAction{

	private List<Transaccion> transacciones = new LinkedList<Transaccion>();
	
	public List<Transaccion> getTransacciones(){
		return this.transacciones;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute() throws Exception {
		
		Map [] selectedOnes = getSelectedKeys(); 
		if (selectedOnes != null) {
			List<Pendiente> pendientes = new LinkedList<Pendiente>();
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				getPendiente(clave, pendientes);	
			}
			if (!pendientes.isEmpty()){
				Transaccion trOrigen = pendientes.iterator().next().origen();
				trOrigen.generarTransaccionesDestino(pendientes, getTransacciones());
				this.posGenerarTransacciones(getTransacciones());
				this.commit();
				
				if (getTransacciones().isEmpty()){
					this.addError("No se generó ningún comprobantes");
				}
				else{
					getTab().deselectAll();                                                  
					resetDescriptionsCache();
					if (getTransacciones().size() == 1){
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
						this.addMessage("Operación Finalizada");
						this.addMessage("Comprobantes generados " + new Integer(getTransacciones().size()).toString());
					}
				}
			}
			else{
				this.addError("No hay comprobantes seleccionados");
			}
			
		}				
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
	protected abstract void getPendiente(Map clave, List<Pendiente> pendientes);
	
	// Para agregar lógica en una acción particular 
	protected void posGenerarTransacciones(List<Transaccion> trgeneradas){		
	}
}
