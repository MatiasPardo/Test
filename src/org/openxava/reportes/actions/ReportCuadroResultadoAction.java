package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.UtilERP;
import org.openxava.contabilidad.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportCuadroResultadoAction extends ReportBaseConcatAction{

	private String tipoFormato;
			
	public String getTipoFormato() {
		return tipoFormato;
	}

	public void setTipoFormato(String tipoFormato) {
		this.tipoFormato = tipoFormato;
	}
	
	@Override
	public void execute() throws Exception {
		if (Is.equal(this.getTipoFormato(), "excel")){
			this.setFormat(JasperReportBaseAction.EXCEL);
		}
		else if (Is.equal(this.getTipoFormato(),"pdf")){
			this.setFormat(JasperReportBaseAction.PDF);
		}
		else{
			throw new ValidationException("Tipo de formato definida en la acción inválido");
		}
		
		if (this.getPreviousView().isKeyEditable()){
			throw new ValidationException("primero_grabar");
		}
				
		IParametrosReporteContable objeto = instanciarObjetoContable(this.getPreviousView().getKeyValues());
		Map<String, Object> parametros = this.crearParametros();	
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyy-MM-dd");
			
		
		EjercicioContable ejercicio = objeto.ejercicio();
		parametros.put("EJERCICIO", ejercicio.getId());
		parametros.put("EJERCICIO_CODIGO", ejercicio.getCodigo());
		parametros.put("EJERCICIO_NOMBRE", ejercicio.getNombre());
		parametros.put("EJERCICIO_DESDE", objeto.ejercicio().getDesde());
		parametros.put("EJERCICIO_HASTA", objeto.ejercicio().getHasta());
		
		Date fechaDesdeReporte = (Date)this.getView().getValue("desde");
		Date fechaHastaReporte = (Date)this.getView().getValue("hasta");
		if (fechaDesdeReporte == null || fechaHastaReporte == null){
			throw new ValidationException("Falta asignar fecha desde/hasta");
		}
		else{
			fechaDesdeReporte = UtilERP.trucarDateTime(fechaDesdeReporte);
			fechaHastaReporte = UtilERP.trucarDateTime(fechaHastaReporte);			
		}
		objeto.validarRangoFechas(fechaDesdeReporte, fechaHastaReporte);
		
		parametros.put("DESDE", formatoDate.format(objeto.getDesde()));
		parametros.put("HASTA", formatoDate.format(objeto.getHasta()));
		parametros.put("DESDEDATE", objeto.getDesde());
		parametros.put("HASTADATE", objeto.getHasta());
		
		this.addParameters(parametros);
					
		super.execute();
	}


	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "CuadroResultados.jrxml";
		String[] reportes = new String[1];
		reportes[0] = nombreReporte;
		return reportes;
	}
		
	private IParametrosReporteContable instanciarObjetoContable(Map<?, ?> key) {
		IParametrosReporteContable objeto;
		try {
			objeto = (IParametrosReporteContable)MapFacade.findEntity(getPreviousView().getModelName(), key);
		} catch (Exception e) {
			throw new ValidationException("No se pudo obtener los parámetros del reporte: " + e.toString());
		}
		return objeto;
	}

	
	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;
	}

	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
}
