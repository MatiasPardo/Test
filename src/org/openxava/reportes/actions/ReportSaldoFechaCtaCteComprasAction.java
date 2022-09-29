package org.openxava.reportes.actions;

import java.util.Date;
import java.util.Map;

import org.openxava.actions.JasperReportBaseAction;
import org.openxava.validators.ValidationException;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportSaldoFechaCtaCteComprasAction extends ReportBaseAction{
	
	private Date fecha;
	
	@Override
	public void execute() throws Exception {
		this.setFecha((Date)this.getView().getValue("desde"));
		if (this.getFecha() == null){
			throw new ValidationException("Fecha no asignada");
		}
		this.setIdFiltroEmpresa(this.getView().getValueString("empresa.id"));
				
		this.setFormat(JasperReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		addMessage("listado_OK");
	}
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	@Override
	protected String getNombreReporte() {
		return "SaldoCtaCteProveedorFecha.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {		
		parametros.put("FECHA", this.getFecha());		
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
}
