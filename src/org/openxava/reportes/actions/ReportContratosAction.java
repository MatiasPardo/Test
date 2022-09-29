package org.openxava.reportes.actions;

import java.util.Map;

import org.openxava.base.model.TipoFormatoImpresion;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportContratosAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "Contratos.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	@Override
	protected TipoFormatoImpresion formatoImpresion(){
		return TipoFormatoImpresion.Excel;
	}

}
