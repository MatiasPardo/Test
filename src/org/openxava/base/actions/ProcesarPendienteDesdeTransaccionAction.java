package org.openxava.base.actions;

import java.util.*;

import javax.ejb.*;

import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


public class ProcesarPendienteDesdeTransaccionAction extends ProcesarPendienteAction{
	
	private List<Transaccion> transacciones = new LinkedList<Transaccion>();
	
	private String tipoTransaccionDestino = null;
	
	public String getTipoTransaccionDestino() {
		return tipoTransaccionDestino;
	}

	public void setTipoTransaccionDestino(String tipoTransaccionDestino) {
		this.tipoTransaccionDestino = tipoTransaccionDestino;
	}

	public List<Transaccion> getTransacciones(){
		return this.transacciones;
	}

	@Override
	public void execute() throws Exception {
		
		if (Is.emptyString(this.getTipoTransaccionDestino())){
			throw new ValidationException("Falta definir los parámetros destino");
		}
		
		super.execute();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void getPendiente(Map clave, List<Pendiente> pendientes) {
		Transaccion transaccion;
		try {
			transaccion = (Transaccion)MapFacade.findEntity(this.getTab().getModelName(), clave);
		} catch (ObjectNotFoundException e) {
			throw new ValidationException("No se encontró el comprobante de clave " + clave.toString());
		} catch (Exception e) {
			throw new ValidationException("Error al buscar el comprobante " + clave.toString());	
		}
		
		Pendiente pendiente = transaccion.buscarPendienteParaProcesar(this.getTipoTransaccionDestino());
		if (pendiente != null){
			pendientes.add(pendiente);
		}
	}
}
