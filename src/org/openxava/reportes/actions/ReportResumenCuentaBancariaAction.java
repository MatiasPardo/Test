package org.openxava.reportes.actions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.model.MapFacade;
import org.openxava.tab.Tab;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import net.sf.jasperreports.engine.JRDataSource;

public class ReportResumenCuentaBancariaAction extends ReportBaseConcatAction{

	private Date desde = null;
	
	private Date hasta = null;

	private List<CuentaBancaria> cuentas = new LinkedList<CuentaBancaria>();
	
	private TipoFormatoImpresion formatoImpresion = null;
	
	@Inject
	private Tab tab;
	
	public Tab getTab() {
		return tab;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}

	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "ResumenCuentaBancariaPdf.jrxml";
		if (Is.equal(this.formatoImpresion, TipoFormatoImpresion.Excel)){
			nombreReporte = "ResumenCuentaBancaria.jrxml";
		}
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
	public void execute() throws Exception {
		
		this.desde = (Date)this.getView().getValue("desde");
		this.desde = DateUtils.truncate(this.desde, Calendar.DATE);
		this.hasta = (Date)this.getView().getValue("hasta");
		this.hasta = DateUtils.truncate(this.hasta, Calendar.DATE);
		
		if (this.desde == null){
			throw new ValidationException("Parámetro desde vacío");
		}
		if (this.hasta == null){
			throw new ValidationException("Parámetro hasta vacío");
		}
		if (desde.compareTo(hasta) > 0){
			throw new ValidationException("Fecha desde no puede ser superior a fecha hasta");
		}
		
		@SuppressWarnings("rawtypes")
		Map [] selectedOnes = this.getTab().getSelectedKeys();
		this.cuentas = new LinkedList<CuentaBancaria>();
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map<?, ?> clave = selectedOnes[i];
				CuentaBancaria cuenta = (CuentaBancaria)MapFacade.findEntity(this.getTab().getModelName(), clave);
				this.cuentas.add(cuenta);
			}
		}
		
		if (this.cuentas.isEmpty()){
			throw new ValidationException("Falta seleccionar las cuentas bancarias");
		}
		
		for(CuentaBancaria cuenta: this.cuentas){
			Map<String, Object> parametros = this.crearParametros();
			parametros.put("CUENTABANCARIA", cuenta.getId());
			parametros.put("CUENTABANCARIA_CODIGO", cuenta.getCodigo());
			parametros.put("CUENTABANCARIA_NOMBRE", cuenta.getNombre());
			
			parametros.put("FECHAEJECUCION", DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));		
			parametros.put("DESDE", this.desde);
			parametros.put("HASTA", this.hasta);		
						
			this.addParameters(parametros);
		}
		
		this.formatoImpresion = (TipoFormatoImpresion)this.getView().getValue("formato");
		if (this.formatoImpresion != null){
			if (this.formatoImpresion.equals(TipoFormatoImpresion.Excel)){
				this.setFormat(ReportBaseAction.EXCEL);
			}
			else if (this.formatoImpresion.equals(TipoFormatoImpresion.PDF)){
				this.setFormat(ReportBaseAction.PDF);
			}
		}
		
		super.execute();
		
		this.deseleccionarElementos(true);
	}
}
