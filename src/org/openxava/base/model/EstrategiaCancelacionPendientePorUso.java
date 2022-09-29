package org.openxava.base.model;

import java.util.*;

import org.openxava.validators.*;

public class EstrategiaCancelacionPendientePorUso implements IEstrategiaCancelacionPendiente{

	private Collection<Pendiente> pendientes = new LinkedList<Pendiente>();
	
	public Collection<Pendiente> getPendientes(){
		return this.pendientes;
	}
	
	@Override
	public void cancelarPendientes() {
		org.openxava.util.Messages messages = new org.openxava.util.Messages();
		if (this.getPendientes().isEmpty()){
			throw new ValidationException("No se puede aplicar la estrategia de cancelación porque no hay pendientes");
		}
		for(Pendiente pend: this.getPendientes()){
			if (pend.getCumplido()){
				messages.add("Ya esta cumplido " + pend.toString());
			}
			else{
				pend.cumplir();
			}
		}
		if (!messages.isEmpty()){
			throw new ValidationException(messages);
		}
	}

	@Override
	public void liberarPendientes(){
		org.openxava.util.Messages messages = new org.openxava.util.Messages();
		for(Pendiente pend: this.getPendientes()){
			if (pend.getCumplido()){
				pend.anularCumplimiento();				
			}
			else{
				messages.add("Ya esta liberado " + pend.toString());
			}
		}
		if (!messages.isEmpty()){
			throw new ValidationException(messages);
		}
	}

	public void pendientesProcesados(Collection<Pendiente> list){
		list.addAll(this.getPendientes());		
	}
	
}
