package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import net.sf.jasperreports.engine.*;

import org.openxava.actions.*;
import org.openxava.base.model.UtilERP;
import org.openxava.contabilidad.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ReportBalanceSumaSaldosAction extends ReportBaseAction{

	private String tipoFormato;
	
	public String getTipoFormato() {
		return tipoFormato;
	}

	public void setTipoFormato(String tipoFormato) {
		this.tipoFormato = tipoFormato;
	}
	
	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			throw new ValidationException("No se puede imprimir el comprobante. Falta grabarlo");
		}
		else{
			Messages errors = MapFacade.validate(getView().getModelName(), getView().getValues());
			if (errors.contains()) throw new ValidationException(errors);
		}
		
		if (Is.equal(this.getTipoFormato(), "excel")){
			this.setFormat(JasperReportBaseAction.EXCEL);
		}
		else if (Is.equal(this.getTipoFormato(),"pdf")){
			this.setFormat(JasperReportBaseAction.PDF);
		}
		else{
			throw new ValidationException("Tipo de formato definida en la acción inválido");
		}
		
		super.execute();
	}
	
	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

	@Override
	protected String getNombreReporte(){
		String nombreReporte = "BalanceSumaSaldos.jrxml";
		if (Is.equal(this.getTipoFormato(), "excel")){
			nombreReporte = "BalanceSumaSaldosExcel.jrxml";
		}
		return nombreReporte;
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		IParametrosReporteContable objeto;
		try {
			objeto = (IParametrosReporteContable)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
		} catch (Exception e) {
			throw new ValidationException("No se pudo obtener los parámetros del reporte: " + e.toString());
		}
		Date fechaDesdeReporte = (Date)this.getView().getValue("fechaDesde");
		Date fechaHastaReporte = (Date)this.getView().getValue("fechaHasta");
		if (fechaDesdeReporte == null || fechaHastaReporte == null){
			throw new ValidationException("Falta asignar fecha desde/hasta en reporte");
		}
		else{
			fechaDesdeReporte = UtilERP.trucarDateTime(fechaDesdeReporte);
			fechaHastaReporte = UtilERP.trucarDateTime(fechaHastaReporte);			
		}
		objeto.validarRangoFechas(fechaDesdeReporte, fechaHastaReporte);
		
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyy-MM-dd");
		
		parametros.put("DESDE", formatoDate.format(fechaDesdeReporte));
		parametros.put("HASTA", formatoDate.format(fechaHastaReporte));
		parametros.put("DESDEDATE", fechaDesdeReporte);
		parametros.put("HASTADATE", fechaHastaReporte);
		parametros.put("PERIODO_DESDEDATE", objeto.getHasta());
		parametros.put("PERIODO_HASTADATE", objeto.getHasta());
		parametros.put("EJERCICIO_ID", objeto.ejercicio().getId());
		parametros.put("EJERCICIO_CODIGO", objeto.ejercicio().getCodigo());
		parametros.put("EJERCICIO_NOMBRE", objeto.ejercicio().getNombre());
		parametros.put("EJERCICIO_DESDE", objeto.ejercicio().getDesde());
		parametros.put("EJERCICIO_HASTA", objeto.ejercicio().getHasta());		
	}
}
