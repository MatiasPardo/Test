package org.openxava.reportes.actions;

import java.util.Date;
import java.util.Map;

import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.validators.ValidationException;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportIndicadoresAction extends ReportBaseAction{

private String parametroNombreReporte = "Indicadores";
	
	public String getParametroNombreReporte() {
		return parametroNombreReporte;
	}

	public void setParametroNombreReporte(String parametroNombreReporte) {
		this.parametroNombreReporte = parametroNombreReporte;
	}

	private Integer numero;
		
	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	
	@Override
	protected String getNombreReporte() {
		return this.getParametroNombreReporte() + this.getNumero().toString() + ".jrxml";
	}

	@Override
	protected void agregarParametros(Map<String, Object> parametros) {
		Date desde = (Date)this.getView().getValue("desde");
		Date hasta = (Date)this.getView().getValue("hasta");
		
		if (desde != null && hasta != null && desde.compareTo(hasta) > 0){
			throw new ValidationException("Fechas: hasta debe ser mayor a desde");
		}
		
		parametros.put("DESDE_DATE", desde);
		parametros.put("HASTA_DATE", hasta);		
	}

	@Override
	protected JRDataSource getDataSource() throws Exception {
		return null;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	protected TipoFormatoImpresion formatoImpresion(){
		return TipoFormatoImpresion.Excel;
	}
	
	@Override
	public void execute() throws Exception {
		if (this.getNumero() == null){
			throw new ValidationException("Parámetro Número no asignado");
		}
		super.execute();
	}
}
