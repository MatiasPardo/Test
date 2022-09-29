package org.openxava.reportes.actions;

import java.math.*;
import java.util.*;

import org.apache.commons.lang.time.*;
import org.openxava.actions.*;
import org.openxava.compras.model.*;
import org.openxava.tab.*;
import org.openxava.util.*;

import net.sf.jasperreports.engine.*;

public class ReportSaldoCtaCtePorProveedorAction extends ReportBaseConcatAction{
	
	private Collection<Proveedor> proveedores = null;

	private String formato = JasperMultipleReportBaseAction.EXCEL;
	
	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "SaldoCtaCteProveedorEXCEL.jrxml";
		if (Is.equalAsString(this.getFormato(), "PDF")){
			nombreReporte = "SaldoCtaCteProveedorPDF.jrxml";
		}
		
		String[] JRXMLs = new String[this.proveedores.size()];
 		for (int i=0; i < JRXMLs.length; i++){
 			JRXMLs[i] = nombreReporte;
 		}		
		return JRXMLs;
	}

	@Override
	protected JRDataSource[] getDataSources() throws Exception {		
		return null;
	}
	
	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		Date fecha = (Date)this.getView().getValue("desde");
		if (fecha != null){
			fecha = DateUtils.truncate(fecha, Calendar.DATE);
		}
		
		this.setIdFiltroEmpresa(this.getView().getValueString("empresa.id"));
		
		this.proveedores = (Collection<Proveedor>)this.getRequest().getAttribute("objetosSeleccionados");
		this.getRequest().removeAttribute("objetosSeleccionados");
		if (this.proveedores != null){
			if (this.proveedores.isEmpty()){
				addError("Debe seleccionar al menos un proveedor");
			}
		}
		else{
			addError("Debe seleccionar al menos un proveedor");
		}
		
		for(Proveedor proveedor: this.proveedores){
			Map<String, Object> parametros = this.crearParametros();
			parametros.put("PROVEEDOR_ID", proveedor.getId());
			parametros.put("PROVEEDOR_CODIGO", proveedor.getCodigo());
			parametros.put("PROVEEDOR_NOMBRE", proveedor.getNombre());
			parametros.put("PROVEEDOR_DIRECCION", proveedor.getDomicilio().getDireccion());
			parametros.put("PROVEEDOR_CIUDAD", proveedor.getDomicilio().getCiudad().getCiudad());
			parametros.put("PROVEEDOR_TELEFONO", proveedor.getTelefono());
			
			parametros.put("FECHA_DESDE", fecha);
			
			if (fecha != null){
				parametros.put("COMPLETO", false);
				ArrayList<BigDecimal> saldos = new ArrayList<BigDecimal>();
				proveedor.calcularSaldoCtaCteFecha(fecha, saldos);				
				parametros.put("SALDOAFECHA1", saldos.get(0));
				parametros.put("SALDOAFECHA2", saldos.get(1));
			}
			else{
				parametros.put("COMPLETO", true);
				parametros.put("SALDOAFECHA1", BigDecimal.ZERO);
				parametros.put("SALDOAFECHA2", BigDecimal.ZERO);
			}
			this.addParameters(parametros);
		}
		
		if (Is.equalAsString(this.getFormato(), "EXCEL")){
			this.setFormat(JasperMultipleReportBaseAction.EXCEL);
		}
		else if (Is.equalAsString(this.getFormat(), "PDF")){
			this.setFormat(JasperMultipleReportBaseAction.PDF);
		}
		super.execute();
		
		this.closeDialog();
		
		Tab tab = (Tab)this.getContext().get(getRequest(), "xava_tab");
		if (tab != null){
			tab.deselectAll();
		}
	}
	
	@Override
	protected boolean filtraPorEmpresa(){
		return true;
	}
}
