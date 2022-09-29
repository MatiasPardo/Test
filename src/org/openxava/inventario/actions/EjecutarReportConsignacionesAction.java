package org.openxava.inventario.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.inventario.model.*;
import org.openxava.model.*;
import org.openxava.ventas.model.*;

public class EjecutarReportConsignacionesAction extends TabBaseAction implements IChainAction{

	private List<Cliente> clientes = null;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		Map [] selectedOnes = getSelectedKeys();
		this.clientes = new LinkedList<Cliente>();		
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				Cliente cliente = null;
				if (this.getTab().getModelName().equals(PendienteLiquidacionConsignacion.class.getSimpleName())){
					cliente = ((PendienteLiquidacionConsignacion)MapFacade.findEntity(this.getTab().getModelName(), clave)).getCliente();
				}
				else{
					cliente = ((ItemRemito)MapFacade.findEntity(this.getTab().getModelName(), clave)).getRemito().getCliente();
				}
				if (!this.clientes.contains(cliente)){
					this.clientes.add(cliente);
				}
			}
			if (this.clientes.isEmpty()){
				this.addError("Debe seleccionar al menos una consignación");
			}			
		}
		else{
			this.addError("No seleccionaron ninguna consignación");
		}
		
		if (this.getErrors().isEmpty()){
			this.getTab().deselectAll();			
		}
		
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			this.getRequest().setAttribute("clientes", this.clientes);
			return "PendienteLiquidacionConsignacionPorItemYPorCabecera.ReportConsignaciones";
		}
		else{
			return null;
		}
	}
}

