package org.openxava.reportes.actions;

import java.math.*;
import java.util.*;

import org.apache.commons.lang.time.*;
import org.openxava.actions.*;
import org.openxava.tab.*;
import org.openxava.ventas.model.*;

import net.sf.jasperreports.engine.*;

public class ReportSaldoCtaCtePorClienteAction extends ReportBaseConcatAction{

	private Collection<Cliente> clientes = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		Date fecha = (Date)this.getView().getValue("desde");
		if (fecha != null){
			fecha = DateUtils.truncate(fecha, Calendar.DATE);
		}
		Date hasta = (Date)this.getView().getValue("hasta");
		if (hasta != null){
			hasta = DateUtils.truncate(hasta, Calendar.DATE);
		}
		this.setIdFiltroEmpresa(this.getView().getValueString("empresa.id"));
		
		if ((hasta != null) && (fecha != null)){
			if (hasta.compareTo(fecha) < 0){
				addError("La fecha hasta no puede ser anterior a la fecha desde");
			}
		}
		
		this.clientes = (Collection<Cliente>)this.getRequest().getAttribute("objetosSeleccionados");
		this.getRequest().removeAttribute("objetosSeleccionados");
		if (this.clientes != null){
			if (this.clientes.isEmpty()){
				addError("Debe seleccionar al menos un cliente");
			}
		}
		else{
			addError("Debe seleccionar al menos un cliente");
		}
		
		for(Cliente cliente: this.clientes){
			Map<String, Object> parametros = this.crearParametros();
			parametros.put("CLIENTE_ID", cliente.getId());
			parametros.put("CLIENTE_CODIGO", cliente.getCodigo());
			parametros.put("CLIENTE_NOMBRE", cliente.getNombre());
			parametros.put("CLIENTE_NOMBREFANTASIA", cliente.getNombreFantasia());
			parametros.put("CLIENTE_DIRECCION", cliente.getDomicilio().getDireccion());
			parametros.put("CLIENTE_CIUDAD", cliente.getDomicilio().getCiudad().getCiudad());
			parametros.put("CLIENTE_TELEFONO", cliente.getTelefono());
			
			parametros.put("FECHA_DESDE", fecha);
			parametros.put("FECHA_HASTA", hasta);
			
			Collection<String> empresas = (Collection<String>)parametros.get("FILTROEMPRESAS");
			
			if (fecha != null){
				parametros.put("COMPLETO", false);
				ArrayList<BigDecimal> saldos = new ArrayList<BigDecimal>();
				cliente.calcularSaldoCtaCteFecha(fecha, empresas, saldos);				
				parametros.put("SALDOAFECHA1", saldos.get(0));
				parametros.put("SALDOAFECHA2", saldos.get(1));
			}
			else{
				parametros.put("COMPLETO", true);
				parametros.put("SALDOAFECHA1", BigDecimal.ZERO);
				parametros.put("SALDOAFECHA2", BigDecimal.ZERO);
			}			
			if (hasta != null){
				parametros.put("COMPLETOHASTA", false);
			}
			else{
				parametros.put("COMPLETOHASTA", true);
			}
			this.addParameters(parametros);
		}
		
		this.setFormat(JasperMultipleReportBaseAction.EXCEL);
		super.execute();
		
		this.closeDialog();
		
		Tab tab = (Tab)this.getContext().get(getRequest(), "xava_tab");
		if (tab != null){
			tab.deselectAll();
		}
	}
	
	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "SaldoCtaCteCliente.jrxml";
		String[] JRXMLs = new String[this.clientes.size()];
 		for (int i=0; i < JRXMLs.length; i++){
 			JRXMLs[i] = nombreReporte;
 		}		
		return JRXMLs;
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
