package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class AccionesPosConfirmarTransaccionAction extends ViewBaseAction implements IChainActionWithArgv{

	private Collection<Transaccion> imprimir = new LinkedList<Transaccion>();
	
	@Override
	public void execute() throws Exception {
		try{
			Transaccion transaccion = (Transaccion) MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			
			Collection<Transaccion> transaccionesAutomaticas = new LinkedList<Transaccion>();
			Messages erroresTrAutomaticas = new Messages();
			ProcesadorPendiente procesador = new ProcesadorPendiente();
			procesador.generarTransaccionesAutomaticas(transaccion, transaccionesAutomaticas, erroresTrAutomaticas);
			
			Collection<Transaccion> trVerificarImpresion = new LinkedList<Transaccion>();
			trVerificarImpresion.add(transaccion);
			for(Transaccion trAutomatica: transaccionesAutomaticas){
				addMessage(trAutomatica.toString());
				trVerificarImpresion.add(trAutomatica);				
			}
			if (!erroresTrAutomaticas.isEmpty()){
				addErrors(erroresTrAutomaticas);
			}
			
			for(Transaccion tr: trVerificarImpresion){
				if (tr.cerrado()){
					if (tr.configurador().getImpresionAutomatica()){
						imprimir.add(tr);							
					}
				}
			}
		}	
		catch(Exception ex){
			this.rollback();
			if (ex instanceof ValidationException){
				addErrors(((ValidationException)ex).getErrors());
			}
			else{
				if (ex.getMessage() != null){
					addError(ex.getMessage());
				}
				else{
					addError(ex.toString());
				}
			}
			this.getMessages().removeAll();
		}
	}

	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
	
	@Override
	public String getNextActionArgv() throws Exception {
		String arg = null;
		if (!this.imprimir.isEmpty()){
			arg = "trNextAction=Transacciones.ImpresionPDF";
			this.getRequest().setAttribute("transacciones", this.imprimir);
		}		
		return arg;		
	}	

}

