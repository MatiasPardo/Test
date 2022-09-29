package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.apache.commons.lang3.time.*;
import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportMayorContableAction extends ReportBaseConcatAction{

	private Collection<CuentaContable> cuentas = null;
	
	private Date desde = null;
	
	private Date hasta = null;
	
	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "Mayor.jrxml";
		String[] reportes = new String[this.cuentas.size()];
		for(int i = 0; i < this.cuentas.size(); i++){
			reportes[i] = nombreReporte;
		}
		return reportes;
	}

	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		this.cuentas = (Collection<CuentaContable>)this.getRequest().getAttribute("cuentas");
		this.desde = (Date)this.getRequest().getAttribute("fechaDesde");
		this.desde = DateUtils.truncate(this.desde, Calendar.DATE);
		this.hasta = (Date)this.getRequest().getAttribute("fechaHasta");
		this.hasta = DateUtils.truncate(this.hasta, Calendar.DATE);
		
		this.validarParametrosRequest();
		
		this.getRequest().removeAttribute("cuenta");
		this.getRequest().removeAttribute("fechaDesde");
		this.getRequest().removeAttribute("fechaHasta");
		
		for(CuentaContable cuenta: this.cuentas){
			Map<String, Object> parametros = this.crearParametros();
			parametros.put("CUENTACONTABLE", cuenta.getId());
			parametros.put("CUENTACONTABLE_CODIGO", cuenta.getCodigo());
			parametros.put("CUENTACONTABLE_NOMBRE", cuenta.getNombre());
			parametros.put("SALDOINICIAL", cuenta.saldoInicialFecha(this.desde));
			parametros.put("DESDEDATE", this.desde);
			parametros.put("HASTADATE", this.hasta);
			SimpleDateFormat formatoDate = new SimpleDateFormat("yyyy-MM-dd");
			parametros.put("DESDE", formatoDate.format(this.desde));
			parametros.put("HASTA", formatoDate.format(this.hasta));
			this.addParameters(parametros);
		}
		
		this.setFormat(JasperMultipleReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		addMessage("Listado Finalizado");
	}

	private void validarParametrosRequest(){
		if (this.cuentas == null){
			throw new ValidationException("Parámetro cuentas vacío");
		}
		else if (this.cuentas.isEmpty()){
			throw new ValidationException("Debe seleccionar al menos 1 cuenta contable");
		}
		
		if (this.desde == null){
			throw new ValidationException("Parámetro fechaDesde vacío");
		}
		
		if (this.hasta == null){
			throw new ValidationException("Parámetro fechaHasta vacío");
		}
		if (desde.compareTo(hasta) > 0){
			throw new ValidationException("Fecha desde no puede ser superior a fecha hasta");
		}
	}	
}


