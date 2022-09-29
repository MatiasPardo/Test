package org.openxava.reportes.actions;

import java.text.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import net.sf.jasperreports.engine.*;

public class ReportRetencionesPagoAction extends ReportBaseConcatAction{
	
	private PagoProveedores pago;
	
	private Collection<ItemPagoRetencion> retenciones;
	
	private PagoProveedores getPago() throws Exception{
		if (this.pago == null){
			this.pago = (PagoProveedores)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
		}
		return pago;
	}
	
	private Collection<ItemPagoRetencion> getRetenciones() throws Exception{
		if (this.retenciones == null){
			this.retenciones = this.getPago().retencionesCalculadas();
		}
		return this.retenciones;
	}
	
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			throw new ValidationException("No se puede imprimir, falta confirmar el comprobante");
		}
		else{
			if (!this.getPago().cerrado()){
				throw new ValidationException("No se puede imprimir, falta confirmar el comprobante");
			}
		}
		
		if (this.getRetenciones().isEmpty()){
			throw new ValidationException("No hay retenciones");
		}
		
		PagoProveedores pago = this.getPago();
		for(ItemPagoRetencion retencion: this.getRetenciones()){
			Map<String, Object> parametros = this.crearParametros();
			
			SimpleDateFormat formatoDate = new SimpleDateFormat("dd/MM/yyyy");
						
			parametros.put("FECHA", formatoDate.format(pago.getFecha()));
			parametros.put("FECHADATE",  DateFormat.getDateInstance(DateFormat.LONG).format(pago.getFecha()));
			parametros.put("COMPROBANTE", pago.getId());
			parametros.put("CODIGOIMPUESTO", retencion.getImpuesto().getCodigo());
			parametros.put("NOMBREIMPUESTO", retencion.getImpuesto().getNombre());
			parametros.put("NOMBRETIPOIMPUESTO", retencion.getImpuesto().getTipo().toString());
			parametros.put("CODIGOTIPOIMPUESTO", Integer.toString(retencion.getImpuesto().getTipo().getIndice()));
			parametros.put("ALICUOTARETENCION", retencion.getAlicuota());
			parametros.put("MONTONOSUJETORETENCION", retencion.getMontoNoSujetoRetencion());
			parametros.put("NETOACUMULADO", retencion.getNetoAcumulado());
			parametros.put("NETOGRABADO", retencion.getNetoGrabado());
			parametros.put("RETENCIONACTUAL", retencion.getRetencionActual());
			parametros.put("RETENCIONESANTERIORES", retencion.getRetencionesAnteriores());
			parametros.put("RETENCIONTOTAL", retencion.getRetencionTotal());
			parametros.put("RETENCIONNUMERO", retencion.getNumero());
			
			// detalle de facturas que originan la retencion
			StringBuffer detalleFacturas = new StringBuffer();
			for(CuentaCorrienteCompra ctacte: pago.getComprobantesPorPagar()){
				if (Is.equalAsString(ctacte.getIdPagoProveedores(), pago.getId())){
					String tipo = ctacte.getTipo();
					if (tipo.indexOf("FACTURA") >= 0){
						tipo = "Factura";
					}
					else if (tipo.indexOf("CREDITO") >= 0){
						tipo = "Crédito";
					}
					else if (tipo.indexOf("DEBITO") >= 0){
						tipo = "Débito";
					}
					detalleFacturas.append(tipo).append(" ").append(ctacte.getNumero()).append("\n");
				}
			}
			if (detalleFacturas.length() > 0){ 
				parametros.put("DETALLEFACTURAS", detalleFacturas.toString());
			}
			else{
				parametros.put("DETALLEFACTURAS", "Sin detalle");
			}
			
			parametros.put("EMPRESA", pago.getEmpresa().getNombre());
			parametros.put("CUIT_EMPRESA", pago.getEmpresa().getCuit());
			parametros.put("RAZONSOCIAL_EMPRESA", pago.getEmpresa().getRazonSocial());			
			parametros.put("DIRECCION_EMPRESA", pago.getEmpresa().getDomicilio().getDireccion());
			parametros.put("CIUDAD_EMPRESA", pago.getEmpresa().getDomicilio().getCiudad().getCiudad());
			parametros.put("PROVINCIAL_EMPRESA", pago.getEmpresa().getDomicilio().getCiudad().getProvincia().getProvincia());
			
			parametros.put("NOMBRE_PROVEEDOR", pago.getProveedor().getNombre());
			parametros.put("NUMERODOCUMENTO_PROVEEDOR", pago.getProveedor().getNumeroDocumento());
			parametros.put("TIPODOCUMENTO_PROVEEDOR", pago.getProveedor().getTipoDocumento().toString());
			parametros.put("POSICIONIVA_PROVEEDOR", pago.getProveedor().getPosicionIva().toString());
			parametros.put("DIRECCION_PROVEEDOR", pago.getProveedor().getDomicilio().getDireccion());
			parametros.put("CODIGOPOSTAL_PROVEEDOR", pago.getProveedor().getDomicilio().getCiudad().getCodigoPostal().toString());
			parametros.put("CIUDAD_PROVEEDOR", pago.getProveedor().getDomicilio().getCiudad().getCiudad());
			parametros.put("PROVINCIA_PROVEEDOR", pago.getProveedor().getDomicilio().getCiudad().getProvincia().getProvincia());
						
			this.addParameters(parametros);
		}		
		super.execute();
	}
	
	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		return null;
	}

	@Override
	protected String[] getJRXMLs() throws Exception {
		String nombreReporte = "RetencionPago.jrxml";
		String[] JRXMLs = new String[this.getRetenciones().size()];
 		for (int i=0; i < JRXMLs.length; i++){
 			JRXMLs[i] = ConfiguracionERP.pathConfig().concat(nombreReporte);
 		}		
		return JRXMLs;
	}

	@Override
	protected String[] getNombresReportes(){
		String nombreReporte = "RetencionPago.jrxml";
		try{
			String[] reportes = new String[this.getRetenciones().size()];
			for (int i=0; i < reportes.length; i++){
				reportes[i] = nombreReporte;
			}		
			return reportes;
		}
		catch(Exception e){
			if (e.getMessage() != null){
				throw new ValidationException(e.getMessage());
			}
			else{
				throw new ValidationException(e.toString());
			}
		}
	}

}
