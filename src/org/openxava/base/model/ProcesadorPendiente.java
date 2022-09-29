package org.openxava.base.model;

import java.util.*;

import org.openxava.hibernate.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

public class ProcesadorPendiente {
	
	public void generarTransaccionesAutomaticas(Transaccion origen, Collection<Transaccion> generadas, Messages errores){
		Collection<ConfiguracionCircuito> circuitos = new LinkedList<ConfiguracionCircuito>();
		ConfiguracionCircuito.buscarCircuitosAutomaticosPorOrigen(origen.getClass().getSimpleName(), circuitos);
		for(ConfiguracionCircuito circuito: circuitos){
			circuito = XPersistence.getManager().merge(circuito);
			String entidadDestino = circuito.getDestino().getEntidad();
			
			Pendiente pendiente = null;
			try{
				pendiente = origen.buscarPendienteParaProcesar(entidadDestino);				
			}
			catch(Exception e){
				// no se hace nada
			}
			if ((pendiente != null) && (!pendiente.getEjecutado())){
				List<Pendiente> pendientes = new LinkedList<Pendiente>();
				pendientes.add(pendiente);
				List<Transaccion> transaccionesGeneradas = new LinkedList<Transaccion>();
				try{
					origen.generarTransaccionesDestino(pendientes, transaccionesGeneradas);
					if (!transaccionesGeneradas.isEmpty()){
						this.commit();
						
						if (circuito.getConfirmaDestino()){
							// se confirma cada transacción generada
							for(Transaccion transaccion: transaccionesGeneradas){
								// se debe volver a instanciar despues de un commit
								Transaccion trParaConfirmar = XPersistence.getManager().find(transaccion.getClass(), transaccion.getId());
								try{
									trParaConfirmar.impactarTransaccion();
									this.commit();
									
									// se continua ejecutando los circuitos automáticos
									trParaConfirmar = XPersistence.getManager().find(trParaConfirmar.getClass(), trParaConfirmar.getId());
									this.generarTransaccionesAutomaticas(trParaConfirmar, generadas, errores);
									generadas.add(trParaConfirmar);
								}
								catch(Exception e){
									this.rollback();
									generadas.add(trParaConfirmar);
									errores.add("No se a podido confirmar " + trParaConfirmar.toString());
									if (e.getMessage() != null){
										errores.add(e.getMessage());
									}
									else{
										errores.add(e.toString());
									}
								}							
							}
						}	
					}
					else{
						errores.add(origen.toString() + " no ha podido cumplir el pendiente " + entidadDestino);
					}
				}
				catch(Exception e){
					this.rollback();
					errores.add(origen.toString() + " no ha podido cumplir el pendiente " + entidadDestino);
					if (e.getMessage() != null){
						errores.add(e.getMessage());
					}
					else{
						errores.add(e.toString());
					}
				}
			}
			
		}
	}
	
	private void commit(){
		XPersistence.commit();
		XHibernate.commit();
	}
	
	private void rollback(){
		XPersistence.rollback();
		XHibernate.rollback();
	}
}

