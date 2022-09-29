package org.openxava.reportes.actions;

import java.util.*;

import org.openxava.inventario.model.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportComparativaDepositosAction extends ReportBaseAction{

	private Collection<Deposito> depositos = null;
	
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {
		this.depositos = (Collection<Deposito>)this.getRequest().getAttribute("depositos");
		if (this.depositos == null){
			throw new ValidationException("No hay depósitos seleccionados");
		}
		else if (this.depositos.size() != 2){
			throw new ValidationException("Debe seleccionar dos depósitos");
		}
		this.setFormat(ReportComparativaDepositosAction.EXCEL);
		super.execute();
	}
	
	@Override
	protected String getNombreReporte() {
		return "ComparativaDepositos.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		Integer i = 1;
		for(Deposito deposito: this.depositos){
			parametros.put("DEPOSITO" + i.toString(), deposito.getId());
			parametros.put("DEPOSITO" + i.toString() + "_NOMBRE", deposito.getNombre());
			parametros.put("DEPOSITO" + i.toString() + "_CODIGO", deposito.getCodigo());
			i++;
		}
		
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

}
