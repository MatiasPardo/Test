package org.openxava.ventas.model;


import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.contratos.model.Contrato;
import org.openxava.contratos.model.NovedadContrato;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(members=
	"Principal{ Principal[#" + 
			"descripcion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado;" +
			"moneda, cotizacion, numero;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" + 
	"Descuentos[#" +
			"porcentajeDescuento];" +
	"items;" + 
	"subtotalSinDescuento, descuento, total;}" +
	"Remito {remito} Liquidacion {liquidacion}" +
	"Trazabilidad{trazabilidad}" + 
	"CuentaCorriente{ctacte}" 	
	),
	@View(name="Simple",
			members="numero, estado")
})

@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, numero, tipo.tipo, estado, cliente.codigo, cliente.nombre, total, subtotal, descuento, subtotalSinDescuento",
		defaultOrder="${fechaCreacion} desc")

public class FacturaManual extends VentaElectronica{

	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Factura Manual");
	}
	
	@Override
	public String CtaCteTipo(){
		return "FACTURA MANUAL";
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Factura de Venta Manual";
	}
	
	@Override
	public Boolean numeraSistema(){
		return true;
	}
	
	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		if (this.facturaOrigen != null){
			return this.facturaOrigen.CtaCteResponsable();
		}
		else{
			return super.CtaCteResponsable();
		}
	}

	
	@Transient
	private FacturaVenta facturaOrigen = null;
	
	public void generadaPorFactura(FacturaVenta factura){
		this.facturaOrigen = factura;
		this.setIdObjetoAsociado(factura.getId());
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (Is.emptyString(this.getIdObjetoAsociado())){
			EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
			for (ItemVentaElectronica item: this.getItems()){
				if (item.getItemLiquidacion() != null){
					IItemPendiente itemPendiente = item.getItemLiquidacion().itemPendienteFacturaVentaProxy();
					if (itemPendiente != null){
						estrategia.getItemsPendientes().add(itemPendiente);
					}
				}
				else if (item.getItemRemito() != null){
					IItemPendiente itemPendiente = item.getItemRemito().itemPendienteFacturaVentaProxy();
					if (itemPendiente != null){
						estrategia.getItemsPendientes().add(itemPendiente);	
					}					
				}
				else if (item.getItemPedido() != null){
					ItemPendientePorCantidadProxy itemPendiente = item.getItemPedido().itemPendienteFacturaVentaProxy();
					if (itemPendiente != null){
						Cantidad cantidadPendiente = itemPendiente.getCantidadACancelar();
						cantidadPendiente.setCantidad(item.getCantidad());
						cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
						estrategia.getItemsPendientes().add(itemPendiente);
					}
				}
			}
			if (!estrategia.getItemsPendientes().isEmpty()){
				return estrategia;
			}
			else{
				return super.establecerEstrategiaCancelacionPendiente();
			}
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}

	@Override
	public boolean puedeGenerarTransaccionIntercompany() {
		boolean generar = super.puedeGenerarTransaccionIntercompany();
		if (generar){
			if (this.getPorcentajeFinanciero().compareTo(BigDecimal.ZERO) != 0){
				throw new ValidationException("No se puede generar facturación intercompany si tiene descuento financiero");
			}
		}		
		return generar;
	}
	
	@Override
	public void generarTransaccionIntercompany(BigDecimal porcentaje){
		if ((porcentaje.compareTo(BigDecimal.ZERO) > 0) &&
				(porcentaje.compareTo(new BigDecimal(100)) <= 0)){
			BigDecimal descuentoFinanciero = (new BigDecimal(100)).subtract(porcentaje);
			if (Esquema.getEsquemaApp().getCreditoIntercompany()){
				VentaElectronica credito = this.existeCreditoFacturacionIntercompany();
				if (credito == null){
					crearCreditoFacturacionIntercompany(descuentoFinanciero);
				}
				else{
					if (credito.getPorcentajeFinanciero().compareTo(descuentoFinanciero) != 0){
						throw new ValidationException("Existe un crédito generado con otro porcentaje financiero");
					}
				}
			}
			if (!existeFacturaIntercompany()){
				crearFacturaIntercompany(descuentoFinanciero);
			}
			else{
				throw new ValidationException("Ya se generó una factura intercompany");
			}
		}
		else{
			throw new ValidationException("Porcentaje debe ser entre 0 y 100");
		}
	}

	private VentaElectronica existeCreditoFacturacionIntercompany(){
		Query query = XPersistence.getManager().createQuery("from CreditoInternoVenta where idObjetoAsociado = :id and estado not in (2, 4)");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (VentaElectronica)results.get(0);
		}
		else{
			return null;
		}
	}
	
	private boolean existeFacturaIntercompany(){
		Query query = XPersistence.getManager().createQuery("from FacturaVenta where idObjetoAsociado = :id and estado not in (2, 4)");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}
	
	private void crearCreditoFacturacionIntercompany(BigDecimal porcentajeDescuentoVenta) {
		try {
			CreditoInternoVenta credito = (CreditoInternoVenta)new CreditoInternoVenta();
			credito.copiarPropiedades(this);
			credito.setEstado(Estado.Borrador);
			credito.setItems(new LinkedList<ItemVentaElectronica>());
			credito.setIdObjetoAsociado(this.getId());
			credito.setIntercompany(Boolean.TRUE);
			XPersistence.getManager().persist(credito);
			
			credito.setPorcentajeFinanciero(porcentajeDescuentoVenta);	
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemCredito = new ItemVentaElectronica();
				itemCredito.copiarPropiedades(item);
				itemCredito.setVenta(credito);				
				credito.getItems().add(itemCredito);
				itemCredito.recalcular();
				XPersistence.getManager().persist(itemCredito);
			}
			credito.confirmarTransaccion();
			
			List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
			List<Imputacion> imputaciones = new LinkedList<Imputacion>();
			comprobantesCuentaCorriente.add(this.comprobanteCuentaCorriente());
			comprobantesCuentaCorriente.add(credito.comprobanteCuentaCorriente());
			Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
						
			for(Imputacion imputacion: imputaciones){
				imputacion.asignarGeneradaPor(this);
			}
		} catch (Exception e) {
			String msj = "Error al crear crédito interno venta: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}
	
	private void crearFacturaIntercompany(BigDecimal porcentajeDescuentoVenta){
		try {
			FacturaVenta factura = (FacturaVenta)new FacturaVenta();
			this.inicializarTrCreadaPorWorkFlow(factura);
			factura.setEmpresa(null);
			
			// Se configura en la entidad
			// para garantizar que la moneda sea siempre la configurada para la facturación
			//factura.setMoneda(factura.buscarMonedaDefault());
			factura.setItems(new LinkedList<ItemVentaElectronica>());		
			XPersistence.getManager().persist(factura);
			// para sincronizar las cotizaciones
			factura.grabarTransaccion(); 
			
			factura.setPorcentajeFinanciero(porcentajeDescuentoVenta);
			factura.setIdObjetoAsociado(this.getId());
			factura.setIntercompany(Boolean.TRUE);
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemFactura = new ItemVentaElectronica();
				itemFactura.copiarPropiedades(item);
				itemFactura.setVenta(factura);
				itemFactura.setPrecioUnitario(Transaccion.convertirMoneda(this, factura, item.getPrecioUnitario()));
				factura.getItems().add(itemFactura);
				itemFactura.recalcular();
				
				if (itemFactura.getTasaiva().compareTo(BigDecimal.ZERO) > 0){
					// se actualiza el precio unitario, de tal forma que con el cálculo de iva, de el mismo importe que antes
					BigDecimal precioUnitario = itemFactura.descontarIva(itemFactura.getPrecioUnitario());
					itemFactura.setPrecioUnitario(precioUnitario);
					itemFactura.recalcular();
				}
				
				XPersistence.getManager().persist(itemFactura);
			}
			factura.grabarTransaccion();
			
		} catch (Exception e) {
			String msj = "Error al crear factura de venta intercompany: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return CreditoInternoVenta.class;
	}
	
	@Override
	public boolean verificarPrecioUnitario(ItemVentaElectronica item) {
		if (this.facturaOrigen != null){
			return false;
		}
		else{
			return super.verificarPrecioUnitario(item);
		}		
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), Contrato.class.getSimpleName())){
			for(ItemVentaElectronica item: this.getItems()){
				if (item.getNovedadContrato() != null){
					Trazabilidad.crearTrazabilidad(item.getNovedadContrato(), NovedadContrato.class.getSimpleName(), this, FacturaManual.class.getSimpleName());
				}
			}
		}
	}
	
	@Override
	public boolean esFactura() {
		return true;
	}
}
