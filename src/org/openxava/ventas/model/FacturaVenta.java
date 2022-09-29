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
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
	"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado, subestado;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" + 
	"Descuentos[#" +
			"porcentajeDescuento, porcentajeFinanciero];" +			
	"items; " + 
	"subtotalSinDescuento;" +
	"descuento;" + 		
	"subtotal;" + 
	"iva, percepcion1, percepcion2, impuestosInternos;" + 
	"total;}" +
	"Remito {remito} Liquidacion {liquidacion} Trazabilidad{trazabilidad} CuentaCorriente{ctacte}; "  
	),
	@View(name="Simple",
			members="numero, estado"),
	@View(name="Reversion", extendsView="super.Reversion")
})

@Tab(filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")

public class FacturaVenta extends VentaElectronica{
		
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Factura");
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		if (this.debeGenerarDescuentoFinanciero()){
			this.crearDescuentoVenta();
		}
		
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), Contrato.class.getSimpleName())){
			for(ItemVentaElectronica item: this.getItems()){
				if (item.getNovedadContrato() != null){
					Trazabilidad.crearTrazabilidad(item.getNovedadContrato(), NovedadContrato.class.getSimpleName(), this, FacturaVenta.class.getSimpleName());
				}
			}
		}
	}
		
	public void crearDescuentoVenta(){
		try {
			FacturaManual facturaManual = (FacturaManual)new FacturaManual();
			facturaManual.copiarPropiedades(this);
			facturaManual.setEmpresa(null);
			facturaManual.setEstado(Estado.Borrador);
			facturaManual.asignarNumeracion(this.getNumero(), this.getNumeroInterno());
			facturaManual.setItems(new LinkedList<ItemVentaElectronica>());
			facturaManual.generadaPorFactura(this);
			XPersistence.getManager().persist(facturaManual);
			
			BigDecimal porcentajeDescuentoVenta = (new BigDecimal(100)).subtract(this.getPorcentajeFinanciero());
			facturaManual.setPorcentajeFinanciero(porcentajeDescuentoVenta);
			
			boolean utilizaPrecioMasIva = facturaManual.getEmpresa().utilizaPrecioMasIva();
			
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemFacturaManual = new ItemVentaElectronica();
				itemFacturaManual.copiarPropiedades(item);
				if (utilizaPrecioMasIva){
					itemFacturaManual.setPrecioUnitario(itemFacturaManual.getProducto().agregarIva(itemFacturaManual.getPrecioUnitario()));
				}
				itemFacturaManual.setVenta(facturaManual);				
				facturaManual.getItems().add(itemFacturaManual);
				itemFacturaManual.recalcular();
				XPersistence.getManager().persist(itemFacturaManual);
			}
			facturaManual.confirmarTransaccion();
			
			// Trazabilidad entre factura de venta y la factura manual
			Trazabilidad.crearTrazabilidad(this, this.getClass().getSimpleName(), facturaManual, facturaManual.getClass().getSimpleName());
		} catch (Exception e) {
			String msj = "Error al procesar descuento de venta: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}
	
	@Override
	public String CtaCteTipo(){
		return "FACTURA";
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Factura de Venta";
	}
	
	public boolean esFacturaIntercompany(){
		boolean intercompany = false;
		if (!Is.emptyString(getIdObjetoAsociado())){
			try{
				Object o = XPersistence.getManager().find(FacturaManual.class, this.getIdObjetoAsociado());
				if (o != null){
					intercompany = true;
				}
			}
			catch(Exception e){				
			}
		}
		return intercompany;
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
		
		boolean verificarItems = true;
		if (this.esFacturaIntercompany()){
			verificarItems = false;
		}		
		if (verificarItems){
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
		}	
		if (!estrategia.getItemsPendientes().isEmpty()){
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	@Override
	public Integer AfipTipoComprobante(){
		return this.getTipo().codigoFiscal("FacturaVenta");
		/*if (this.getTipo().equals(TipoComprobanteAfip.A)){
			return 1;	
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.B)){
			return 6;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.E)){
			return 19;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.C)){
			return 11;
		}
		else{
		    throw new ValidationException("Tipo de comprobante AFIP Incorrecto");
		} */
	}
	
	@Override
	public boolean debeAutorizaAfip(){
		return true;
	}
	
	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		IResponsableCuentaCorriente responsable = null;		
		if (this.getRemito() != null){
			if (this.getRemito().getOrdenPreparacion() != null){
				Iterator<ItemOrdenPreparacion> it = this.getRemito().getOrdenPreparacion().getItems().iterator();
				while (it.hasNext()){
					EstadisticaPedidoVenta itemPedido = it.next().getItemPedidoVenta();
					if (itemPedido != null){
						responsable = itemPedido.getVenta().getVendedor();
						break;
					}
				}
			}
		}
		
		if (responsable == null){
			responsable = super.CtaCteResponsable();
		}
		
		return responsable;
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return CreditoVenta.class;
	}
	
	@Override
	public boolean esFactura() {
		return true;
	}
}
