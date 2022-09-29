package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.fisco.model.RegimenFacturacionFiscal;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.model.*;
import org.openxava.view.*;

public class OnChangeClienteVentaElectronicaAction extends OnChangePropertyBaseAction{
	
	private Cliente cliente = null;
	
	protected Cliente getCliente(){
		return this.cliente;
	}
	
	@Override
	public void execute() throws Exception {
		this.cliente = null;
		if (!Is.emptyString((String)getNewValue())){
			String idCliente = (String)getNewValue();
			this.cliente = (Cliente)XPersistence.getManager().find(Cliente.class, idCliente);
			
			this.verificarPuntoVenta(this.cliente);
			
			
			if (this.getView().getValue("listaPrecio") != null){
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("id", this.cliente.getListaPrecio().getId());
				getView().setValue("listaPrecio", values);
			}
			
			if (this.cliente.getCondicionVenta() != null){
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("id", this.cliente.getCondicionVenta().getId());
				getView().trySetValue("condicionVenta", values);
			}
			
			this.getView().setEditable("tipoDocumento", cliente.getSinIdentificacion());
			this.getView().setEditable("cuit", cliente.getSinIdentificacion());
			this.getView().setEditable("posicionIva", cliente.getSinIdentificacion());
			this.getView().setEditable("razonSocial", cliente.getSinIdentificacion());
			
			this.getView().setValue("tipoDocumento", cliente.getTipoDocumento());
			this.getView().setValue("cuit", cliente.getNumeroDocumento());
			this.getView().setValue("razonSocial", cliente.getNombre());
						
			this.getView().trySetValue("email", cliente.getMail1());
			
			Domicilio domicilio = this.cliente.domicilioEntregaPrincipal();
			
			View viewDomicilio = this.getView().getSubview("domicilioEntrega");
			viewDomicilio.clear();
			if (domicilio != null){
				if (Is.equalAsString(this.getView().getViewName(), "FacturaVentaContado")){
					boolean blanquearDomicilio = false;
					if ((this.cliente.getSinIdentificacion()) && (this.getView().getValue("entrega") != null)){
						String idTipoEntrega = this.getView().getValueString("entrega.id");
						if (!Is.emptyString(idTipoEntrega)){
							TipoEntrega tipoEntrega = XPersistence.getManager().find(TipoEntrega.class, idTipoEntrega);
							blanquearDomicilio = tipoEntrega.getDomicilioObligatorio();
						}
					}
					if (!blanquearDomicilio){
						this.getView().setValue("direccion", domicilio.getDireccion());
						
						Map<String, Object> valuesCiudad = new HashMap<String, Object>();
						valuesCiudad.put("codigoPostal", domicilio.getCiudad().getCodigoPostal());
						valuesCiudad.put("ciudad", domicilio.getCiudad().getCiudad());
						valuesCiudad.put("codigo", domicilio.getCiudad().getCodigo());
						
						Map<String, Object> valuesProvincia = new HashMap<String, Object>();
						valuesProvincia.put("codigo", domicilio.getCiudad().getProvincia().getCodigo());
						valuesProvincia.put("provincia", domicilio.getCiudad().getProvincia().getProvincia());
						valuesCiudad.put("provincia", valuesProvincia);
						
						View viewCiudad = this.getView().getSubview("ciudad");
						viewCiudad.setValues(valuesCiudad);
					}
					else{
						this.getView().setValue("direccion", null);
						this.getView().getSubview("ciudad").clear();
					}
						
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("__MODEL_NAME__", viewDomicilio.getModelName());
					values.put("id", domicilio.getId());
					values.put("codigoExterno", domicilio.getCodigoExterno());
					viewDomicilio.setValues(values);
				}
				else{
					Map<String, Object> values = new HashMap<String, Object>();
					values.put("__MODEL_NAME__", viewDomicilio.getModelName());
					values.put("id", domicilio.getId());
					values.put("direccion", domicilio.getDireccion());
					
					Map<String, Object> valuesCiudad = new HashMap<String, Object>();
					valuesCiudad.put("codigoPostal", domicilio.getCiudad().getCodigoPostal());
					valuesCiudad.put("ciudad", domicilio.getCiudad().getCiudad());
					valuesCiudad.put("codigo", domicilio.getCiudad().getCodigo());
					
					Map<String, Object> valuesProvincia = new HashMap<String, Object>();
					valuesProvincia.put("codigo", domicilio.getCiudad().getProvincia().getCodigo());
					valuesProvincia.put("provincia", domicilio.getCiudad().getProvincia().getProvincia());
					valuesCiudad.put("provincia", valuesProvincia);
					
					values.put("ciudad", valuesCiudad);
					
					viewDomicilio.setValues(values);
				}	
			}
						
			if (cliente.getSinIdentificacion()){
				this.getView().setFocus("razonSocial");
			}
			
			getView().setValue("porcentajeDescuento", this.getCliente().getPorcentajeDescuento());
			
			// Puede no estar el porcentaje financiero
			getView().trySetValue("porcentajeFinanciero", this.getCliente().getPorcentajeFinanciero());
			
			View viewPosicion = this.getView().getSubview("posicionIva");
			viewPosicion.clear();
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("codigo", cliente.getPosicionIva().getCodigo());
			values.put("descripcion", cliente.getPosicionIva().getDescripcion());
			viewPosicion.setValuesNotifying(values);
		}
	}
	
	private void verificarPuntoVenta(Cliente cliente){
		RegimenFacturacionFiscal regimenFacturacion = cliente.getRegimenFacturacion().getRegimenFacturacion();
		if (regimenFacturacion.equals(RegimenFacturacionFiscal.Exportacion)){
			String id = this.getView().getValueString("puntoVenta.id");
			PuntoVenta puntoVentaAsignado = XPersistence.getManager().find(PuntoVenta.class, id);
			if (!puntoVentaAsignado.getTipo().exportacion()){
				PuntoVentaDefaultCalculator calculator = new PuntoVentaDefaultCalculator();
				calculator.setCliente(cliente);
				try{
					PuntoVenta puntoVenta = (PuntoVenta)calculator.calculate();
					if (puntoVenta != null){
						Map<String, Object> values = new HashMap<String, Object>();
						values.put("id", puntoVenta.getId());
						getView().setValue("puntoVenta", values);
					}
				}
				catch(Exception e){
					
				}
			}
			
		}
	}

}

