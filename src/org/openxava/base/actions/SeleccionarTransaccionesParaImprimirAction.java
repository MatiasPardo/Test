package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class SeleccionarTransaccionesParaImprimirAction  extends TabBaseAction implements IChainActionWithArgv{

	private List<Transaccion> transacciones = null;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		Map [] selectedOnes = getSelectedKeys();
		this.transacciones = new LinkedList<Transaccion>();
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				Transaccion tr = (Transaccion)MapFacade.findEntity(this.getTab().getModelName(), clave);
				this.transacciones.add(tr);
			}
			if (this.transacciones.isEmpty()){
				this.addError("No hay comprobantes seleccionados");
			}
			else{
				this.getTab().deselectAll();
			}
		}
		else{
			this.addError("No hay comprobantes seleccionados");
		}
	}

	@Override
	public String getNextAction() throws Exception {		
		if ((this.transacciones != null) && (!this.transacciones.isEmpty())){
			if (!this.concatenarImpresionMultiple()){
				return "Transacciones.ImpresionPDF";
			}
			else{
				return "Transacciones.ImpresionPDFJuntas";
			}
		}
		else{
			return null;
		}		
	}

	@Override
	public String getNextActionArgv() throws Exception {
		String arg = null;
		this.getRequest().setAttribute("transacciones", this.transacciones);		
		return arg;
	}

	private boolean concatenarImpresionMultiple(){
		return this.transacciones.get(0).configurador().getConcatenarImpresionMultiple();		
	}
}

