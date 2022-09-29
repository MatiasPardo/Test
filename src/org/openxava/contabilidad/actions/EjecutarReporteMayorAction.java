package org.openxava.contabilidad.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.model.*;
import org.openxava.tab.impl.IXTableModel;

public class EjecutarReporteMayorAction extends TabBaseAction implements IChainActionWithArgv{

	private List<CuentaContable> cuentas = null;

	private boolean tabCompleto = false;
		
	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		this.cuentas = new LinkedList<CuentaContable>();
		if (!this.isTabCompleto()){
			Map [] selectedOnes = getSelectedKeys();			
			if (selectedOnes != null) {
				for (int i = 0; i < selectedOnes.length; i++) {
					Map clave = selectedOnes[i];
					CuentaContable cuenta = (CuentaContable)MapFacade.findEntity(this.getTab().getModelName(), clave);
					this.cuentas.add(cuenta);					
				}
				if (this.cuentas.isEmpty()){
					this.addError("Debe seleccionar al menos una cuenta");
				}			
			}
			else{
				this.addError("No hay comprobantes seleccionados");
			}
		}
		else{
			IXTableModel table = this.getTab().getAllDataTableModel();
			for (int row=0; row < table.getRowCount(); row++) {
				Map<?, ?> key = (Map<?, ?>)table.getObjectAt(row);
				CuentaContable cuenta = XPersistence.getManager().find(CuentaContable.class, key.get("id"));
				this.cuentas.add(cuenta);
			}
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.cuentas.isEmpty()){
			return null;
		}
		else{
			return "CuentaContable.ReportMayor";
		}
	}

	@Override
	public String getNextActionArgv() throws Exception {
		if (!this.cuentas.isEmpty()){
			this.getRequest().setAttribute("cuentas", this.cuentas);
			this.getRequest().setAttribute("fechaDesde", this.getView().getValue("desde"));
			this.getRequest().setAttribute("fechaHasta", this.getView().getValue("hasta"));
		}
		return null;
	}

	public boolean isTabCompleto() {
		return tabCompleto;
	}

	public void setTabCompleto(boolean tabCompleto) {
		this.tabCompleto = tabCompleto;
	}
}


