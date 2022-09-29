package org.openxava.inventario.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.inventario.model.*;
import org.openxava.model.*;

public class SeleccionDepositoComparativaStockAction extends TabBaseAction implements IChainActionWithArgv {

	private Collection<Deposito> depositos = null;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		Map [] selectedOnes = getSelectedKeys();
		this.depositos = new LinkedList<Deposito>();
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				Deposito deposito = (Deposito)MapFacade.findEntity(this.getTab().getModelName(), clave);
				this.depositos.add(deposito);
			}
			if (this.depositos.size() != 2){
				this.depositos = null;
				this.addError("Debe seleccionar dos depósitos para comparar");
			}
			this.getTab().deselectAll();			
		}
		else{
			this.addError("Debe seleccionar dos depósitos para comparar");
		}
	}
	
	@Override
	public String getNextAction() throws Exception {		
		if (this.depositos != null){
			return "Deposito.ReportComparativaDepositos";
		}
		else{
			return null;
		}		
	}

	@Override
	public String getNextActionArgv() throws Exception {
		String arg = null;
		this.getRequest().setAttribute("depositos", this.depositos);		
		return arg;
	}

}
