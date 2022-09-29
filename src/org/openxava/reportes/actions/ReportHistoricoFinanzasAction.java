package org.openxava.reportes.actions;

import java.util.Map;

import org.openxava.actions.JasperReportBaseAction;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportHistoricoFinanzasAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "HistoricoFinanzas.jrxml";
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
	public void execute() throws Exception {
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {		
	}
}
