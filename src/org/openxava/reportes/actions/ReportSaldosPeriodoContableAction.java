package org.openxava.reportes.actions;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openxava.actions.JasperReportBaseAction;
import org.openxava.contabilidad.model.IParametrosReporteContable;
import org.openxava.contabilidad.model.PeriodoContable;
import org.openxava.model.MapFacade;
import org.openxava.validators.ValidationException;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportSaldosPeriodoContableAction extends ReportBaseAction{

	@Override
	protected String getNombreReporte() {
		return "SaldosPeriodoContable.jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		IParametrosReporteContable objeto;
		try {
			objeto = (IParametrosReporteContable)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
		} catch (Exception e) {
			throw new ValidationException("No se pudo obtener los parámetros del reporte: " + e.toString());
		}
		parametros.put("ID", objeto.ejercicio().getId());
		parametros.put("CODIGO", objeto.ejercicio().getCodigo());
		parametros.put("NOMBRE", objeto.ejercicio().getNombre());
		parametros.put("DESDE_DATE", objeto.ejercicio().getDesde());
		parametros.put("HASTA_DATE", objeto.ejercicio().getHasta());
		
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyy-MM-dd");
		parametros.put("DESDE", formatoDate.format(objeto.ejercicio().getDesde()));
		parametros.put("HASTA", formatoDate.format(objeto.ejercicio().getHasta()));
		
		List<PeriodoContable> periodos = new LinkedList<PeriodoContable>();
		objeto.ejercicio().periodosOrdenados(periodos);
		Integer i = 1;
		for(PeriodoContable periodo: periodos){
			parametros.put("PERIODO" + i.toString() + "_NOMBRE", periodo.getNombre());
			parametros.put("PERIODO" + i.toString() + "_ID", periodo.getId());
			i++;
		}
	}

	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			throw new ValidationException("No se puede imprimir el comprobante. Falta grabarlo");
		}
		this.setFormat(JasperReportBaseAction.EXCEL);
		
		super.execute();
	}
		
	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}

}
