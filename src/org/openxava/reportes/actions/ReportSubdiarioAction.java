package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.Empresa;
import org.openxava.contabilidad.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportSubdiarioAction extends ReportBaseAction{
	
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
			throw new ValidationException("Primero debe grabar el subdiario.");
		}
		if (!Is.emptyString(this.getTipoFormato())){
			if (this.getTipoFormato().equals("excel")){
				this.setFormat(JasperReportBaseAction.EXCEL);
			}
			else if (this.getTipoFormato().equals("pdf")){
				this.setFormat(JasperReportBaseAction.PDF);
			}
			else{
				throw new ValidationException("Tipo de formato incorrecto " + this.getTipoFormato());
			}
		}
		else{
			throw new ValidationException("Debe asignar la variable Tipo de formato en la definición de la acción");
		}
		
		super.execute();
		addMessage("Subdiario IVA generado");
	}
	
	@Override
	protected void agregarParametros(Map<String, Object> parametros){
		SimpleDateFormat formatoDate = new SimpleDateFormat("yyyy-MM-dd");
		parametros.put("DESDE", formatoDate.format(this.getView().getValue("fechaDesde")));
		parametros.put("HASTA", formatoDate.format(this.getView().getValue("fechaHasta")));		
		parametros.put("DESDEDATE", (Date)this.getView().getValue("fechaDesde"));
		parametros.put("HASTADATE", (Date)this.getView().getValue("fechaHasta"));
		
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		if (empresa != null){
			parametros.put("EMPRESA_CUIT", empresa.getCuit());
			parametros.put("EMPRESA_RAZONSOCIAL", empresa.getRazonSocial());
			parametros.put("EMPRESA_DIRECCION", empresa.getDomicilio().getDireccion());
			parametros.put("EMPRESA_CIUDAD", empresa.getDomicilio().getCiudad().getCiudad());
			parametros.put("EMPRESA_PROVINCIA", empresa.getDomicilio().getProvincia().getProvincia());
		}
	}

	protected JRDataSource getDataSource() throws Exception {
		return null;
	}
	
	@Override
	protected String getNombreReporte() {
		TipoSubdiarioIVA tipo = (TipoSubdiarioIVA)this.getView().getValue("subdiario");
		String nombreReporte = new String("SubdiarioIVA_Ventas.jrxml");
		if (TipoSubdiarioIVA.Compra.equals(tipo)){
			nombreReporte = new String("SubdiarioIVA_Compras.jrxml");
		}
		return nombreReporte;
	}
}
