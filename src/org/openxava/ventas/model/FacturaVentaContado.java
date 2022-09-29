package org.openxava.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.Sucursal;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.mercadolibre.model.ConfiguracionMercadoLibre;
import org.openxava.mercadolibre.model.MediosPagoEcommerce;
import org.openxava.mercadolibre.model.PedidoML;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.actions.*;

@Entity

@Views({
	@View(name="FacturaVentaContado", members=
		"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"empresa, puntoVenta, tipo;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"estado, subestado;" + 
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento;" + 
				"listaPrecio, email;" + 
				"];" +
			"Domicilio[entrega; direccion;ciudad; domicilioEntrega];" + 
			"observaciones];" + 
		"Descuentos[#" +
			"condicionVenta, porcentajeDescuento, porcentajeFinanciero];" +
		"financiacion;" +
		"items; " + 
		"subtotalSinDescuento;" +
		"descuento;" + 		
		"subtotal;" + 
		"iva, percepcion1, percepcion2;" + 
		"total;}" +
		"Trazabilidad{trazabilidad}; "  
	),
	@View(members=
		"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"empresa, puntoVenta, tipo;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"estado, subestado;" + 
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento;" + 
				"listaPrecio, email];" +
			"Domicilio[entrega; direccion;ciudad];" + 
			"observaciones];" + 
		"Descuentos[#" +
			"condicionVenta, porcentajeDescuento, porcentajeFinanciero];" +
		"financiacion;" +
		"items; " + 
		"subtotalSinDescuento;" +
		"descuento;" + 		
		"subtotal;" + 
		"iva, percepcion1, percepcion2;" + 
		"total;}" +
		"Trazabilidad{trazabilidad}; "  
	),
	@View(name="GeneradaPorDevolucion", members=
		"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"empresa, puntoVenta, tipo;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"estado, subestado;" + 
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento;" + 
				"listaPrecio, email];" +
			"Domicilio[entrega; direccion;ciudad; domicilioEntrega];" + 
			"observaciones];" + 
		"Descuentos[#" +
			"condicionVenta, porcentajeDescuento, porcentajeFinanciero];" +
		"financiacion;" +
		"items; " + 
		"subtotalSinDescuento;" +
		"descuento;" + 		
		"subtotal;" + 
		"iva, percepcion1, percepcion2;" + 
		"total, creditoAFavor, totalACobrar;}" +
		"Trazabilidad{trazabilidad}; "  
	),
	@View(name="DevolucionContado", 
		members="Principal[numero, fecha;" + 
				"subtotal, iva, total;];" + 
			"Cliente[cliente, razonSocial;" + 
			"cuit, posicionIva, tipoDocumento];"),
	
	@View(name="Simple",
			members="numero, estado"),
	@View(name="SimpleSubEstado",
			members="numero, estado, subestado"),
	@View(name="Reversion", extendsView="super.Reversion"),
	@View(name="ReciboContado", members="subtotal, iva, total; creditoAFavor, totalACobrar;"),
	@View(name="CambioEmail", members="email")
})

@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")

public class FacturaVentaContado extends VentaElectronica implements ITransaccionInventario, IVentaInventario{

	public final static String ACCIONGENERARCREDITOCONTADO = "FacturaVentaContado.GenerarCredito";
	public final static String ACCIONREGISTRARCOBRANZA = "FacturaVentaContado.ModificarCobranza";
	public final static String ACCIONCONFIRMARCONTADO = "FacturaVentaContado.confirmar";
	public final static String ACCIONDEVOLUCIONCONTADO = "FacturaVentaContado.Devolucion";
	
	public final static String ESTADOFACTURADO = "1";
	public final static String ESTADOCOBRADO = "2";
	public final static String ESTADOCREDITO = "3";
	public final static String ESTADOCREDITODEVOLUCION = "4";
	public final static String ESTADONOCOBRAR = "5";
	public final static String ESTADOCREDITOFAVOR = "6";
	
	private class MedioPagoFactura{
		
		public MedioPagoFactura(TipoValorConfiguracion medioPago, BigDecimal importe){
			this.setMedioPago(medioPago);
			this.setImporte(importe);
		}
		
		private TipoValorConfiguracion medioPago;
		
		private BigDecimal importe;

		public TipoValorConfiguracion getMedioPago() {
			return medioPago;
		}

		public void setMedioPago(TipoValorConfiguracion medioPago) {
			this.medioPago = medioPago;
		}

		public BigDecimal getImporte() {
			return importe;
		}

		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
	}
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@Required
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
			properties={@PropertyValue(name="entidad", value="TipoEntrega")})
	@OnChange(value=OnChangeTipoEntregaFacturaContadoAction.class)
	private TipoEntrega entrega;
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Factura");
	}
	
	@Override
	public String CtaCteTipo(){
		return "FACTURA";
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Factura Contado";
	}
	
	@Override
	public Integer AfipTipoComprobante(){
		return this.getTipo().codigoFiscal("FacturaVentaContado");
		/*if (this.getTipo().equals(TipoComprobanteAfip.A)){
			return 1;	
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.B)){
			return 6;
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
		boolean debeAutorizar = true;
		if (this.getEmpresa() != null){
			debeAutorizar = this.getEmpresa().getInscriptoIva();
		}
		return debeAutorizar;		
	}
	
	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Preparar)){
			// Deja un pendiente de orden de preparación, no modifica el stock
			return null;
		}
		else{
			ArrayList<IItemMovimientoInventario> items = new ArrayList<IItemMovimientoInventario>();
			items.addAll(this.getItems());
			return items;
		}
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			if (this.ventaEcommerce()){
				return new TipoMovInvEgresoDesreserva();
			}
			else if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Egresar)){
				return new TipoMovInvEgreso();
			}
			else if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Remitir)){
				return new TipoMovInvReserva();
			}
			else{
				throw new ValidationException("Falta definir el tipo de movimiento de inventario en Factura Contado para " + this.getEntrega().toString());
			}
		}
		else{
			if (this.ventaEcommerce()){
				//return new TipoMovInvIngresoReserva();
				
				// si se anula la factura, ya sea con un crédito o por anulación, 
				// el stock de devuelve pero sin reservar
				return new TipoMovInvIngreso();
			}
			else if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Egresar)){
				return new TipoMovInvIngreso();
			}
			else if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Remitir)){
				return new TipoMovInvDesreserva();
			}
			else{
				throw new ValidationException("Falta definir el tipo de movimiento de inventario en Factura Contado para " + this.getEntrega().toString());
			}
		}
	}
	
	@Override
	@Hidden
	public Deposito getDeposito() {
		if (this.ventaEcommerce()){
			PedidoML pedidoML = (PedidoML)this.creadoPor();
			return pedidoML.getDeposito();
		}
		else if (this.getSucursal() != null){
			Deposito dep = this.getSucursal().depositoPrincipal();
			if (dep == null){
				throw new ValidationException("No se encontró depósito principal para la factura de venta contado en la sucursal " + this.getSucursal().toString());
			}
			return dep;
		}
		else{
			throw new ValidationException("No tiene asignada la sucursal. No se puede obtener el depósito");
		}
	}
	
	@Override
	public boolean generaCtaCte(){
		return false;
	}

	@Override
	public boolean validarStockDisponible() {
		if (this.ventaEcommerce()){
			return false;
		}
		else if (this.getEntrega().getComportamiento().equals(ComportamientoTipoEntrega.Preparar)){
			return false;
		}
		else{
			return true;
		}
	}
	
	public ReciboCobranza generarReciboContado() {
		ReciboCobranza recibo = null;
		if (!this.tieneOperacionQueRevierte()){
			recibo = buscarReciboCobranzaContado();			
			if (recibo != null){
				// Si tiene un recibo, en caso de estar confirmado se anula para volver a registrar
				if (recibo.finalizada()){
					if (recibo.getEstado().equals(Estado.Confirmada)){
						recibo.anularTransaccion();
					}					
					recibo = null;					
				}				
			}
				
			if (recibo == null){				
				recibo = new ReciboCobranza();
				recibo.copiarPropiedades(this);
				recibo.setNumero(this.getNumero());
				recibo.setNumeroInterno(this.getNumeroInterno());
				recibo.setFacturaContado(this);
				XPersistence.getManager().persist(recibo);
				
				recibo.setItems(new LinkedList<ItemReciboCobranza>());
				BigDecimal totalACobrar = FacturaVentaContado.convertirMoneda(this, recibo, this.getTotalACobrar().setScale(2, RoundingMode.HALF_EVEN));
				
				Tesoreria destino = null;
				Map<String, MedioPagoFactura> mediosPago = new HashMap<String, MedioPagoFactura>();
				if (this.ventaEcommerce()){
					PedidoML pedidoML = (PedidoML)this.creadoPor();
					ConfiguracionMercadoLibre configuracion = pedidoML.getConfiguracionEcommerce();
										
					MediosPagoEcommerce medioPagoEcommerce = configuracion.buscarMedioPago(pedidoML.getFormaPago());
					TipoValorConfiguracion tipoValorMedioPago = null;
					if (medioPagoEcommerce != null){
						destino = medioPagoEcommerce.getTesoreria();
						tipoValorMedioPago = medioPagoEcommerce.getTipoValor();						
					}
					else{
						destino = configuracion.getCuentaBancaria();
						tipoValorMedioPago = configuracion.getTipoValor();
					}
					
					MedioPagoFactura medioPago = new MedioPagoFactura(tipoValorMedioPago, totalACobrar);
					mediosPago.put(configuracion.getTipoValor().getId(), medioPago);
				}
				else{
					Caja caja = (Caja)Caja.buscarPrincipal(Caja.class.getSimpleName(), recibo.getEmpresa(), this.getSucursal());
					if (caja != null){
						destino = caja;
						TipoValorConfiguracion tipoValorDefecto = caja.tipoValorEfectivo(recibo.getMoneda());
						BigDecimal importePendiente = totalACobrar;
						for(InteresFacturacionVenta interes: this.getFinanciacion()){
							MedioPagoFactura medioPago = null;
							TipoValorConfiguracion tipoValorMedioPago = interes.getCondicionVenta().getMedioPago();
							if (tipoValorMedioPago == null){
								tipoValorMedioPago = tipoValorDefecto;
							}							
							if (tipoValorMedioPago == null){
								throw new ValidationException("Falta definir efectivo en la caja para el recibo contado");
							}
							if (mediosPago.containsKey(tipoValorMedioPago.getId())){
								medioPago = mediosPago.get(tipoValorMedioPago.getId());
							}
							else{
								medioPago = new MedioPagoFactura(tipoValorMedioPago, BigDecimal.ZERO);
								mediosPago.put(tipoValorMedioPago.getId(), medioPago);
							}
							
							BigDecimal importe = FacturaVentaContado.convertirMoneda(this, recibo, interes.getImporte());
							importePendiente = importePendiente.subtract(importe);
							
							medioPago.setImporte(medioPago.getImporte().add(importe));							
						}
						
						// el saldo restante 
						if (importePendiente.compareTo(BigDecimal.ZERO) > 0){
							TipoValorConfiguracion tipoValorMedioPago = tipoValorDefecto;
							if (this.getCondicionVenta() != null && this.getCondicionVenta().getMedioPago() != null){
								tipoValorMedioPago = this.getCondicionVenta().getMedioPago();
							}
							
							MedioPagoFactura medioPago = null;
							if (mediosPago.containsKey(tipoValorMedioPago.getId())){
								medioPago = mediosPago.get(tipoValorMedioPago.getId());								
							}
							else{
								medioPago = new MedioPagoFactura(tipoValorMedioPago, BigDecimal.ZERO);
								mediosPago.put(tipoValorMedioPago.getId(), medioPago);
							}
							medioPago.setImporte(medioPago.getImporte().add(importePendiente));
						}
					}
					else{
						throw new ValidationException("Falta definir una caja como principal para el recibo contado");
					}
				}
				
				for(MedioPagoFactura medioPago: mediosPago.values()){
				
					ItemReciboCobranza item = new ItemReciboCobranza();
					item.setReciboCobranza(recibo);				
					recibo.getItems().add(item);
					item.setDestino(destino);
					item.setTipoValor(medioPago.getMedioPago());
					if (item.getTipoValor().getMoneda().equals(recibo.getMoneda())){
						item.setImporteOriginal(medioPago.getImporte());
					}
					else{
						BigDecimal cotizacion = recibo.buscarCotizacionTrConRespectoA(item.getTipoValor().getMoneda());
						item.setImporteOriginal(medioPago.getImporte().multiply(cotizacion).setScale(2, RoundingMode.HALF_EVEN));
					}
					item.recalcular();
					XPersistence.getManager().persist(item);
				}	
				 				
				recibo.grabarTransaccion();				
				recibo.setaCobrar(totalACobrar);
			}
			this.setIdObjetoAsociado(recibo.getId());
		}
		else{
			throw new ValidationException("No se puede registrar la cobranza, el comprobante tiene un crédito asociado");
		}
		return recibo;
	}
		
	public void asignarSubEstadoFacturado(){
		EstadoEntidad estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOFACTURADO);
		if (estado != null){
			this.setSubestado(estado);
		}
	}
	
	private void evaluarSubEstadoPosConfirmar(BigDecimal totalACobrar){
		int compare = totalACobrar.compareTo(BigDecimal.ZERO);
		if (compare > 0){
			this.asignarSubEstadoFacturado();
		}
		else{
			EstadoEntidad estado = null;
			if (compare == 0){
				// fue una devolución, que no tiene que cobrar nada
				estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADONOCOBRAR);
			}
			else if (compare < 0){
				// le quedo crédito a favor
				estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOCREDITOFAVOR);
			}
			if (estado == null){
				// no se tiene que cobrar, así que si no existen los estados anteriores, se le asigna el estado de cobrado
				estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOCOBRADO);
			}
			
			if (estado != null){
				this.setSubestado(estado);
			}
		}		
	}
	
	public void asignarSubEstadoCobrado(){
		EstadoEntidad estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOCOBRADO);
		if (estado != null){
			this.setSubestado(estado);
		}
	}
	
	public void asignarSubEstadoCredito(){
		EstadoEntidad estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOCREDITO);
		if (estado != null){
			this.setSubestado(estado);
		}
	}
	
	public void asignarSubEstadoCreditoPorDevolucion() {
		EstadoEntidad estado = this.configurador().buscarEstado(FacturaVentaContado.ESTADOCREDITODEVOLUCION);
		if (estado != null){
			this.setSubestado(estado);
		}		
	}
	
	@Override
	public void accionesValidas(List<String> showActions, List<String> hideActions) {
		super.accionesValidas(showActions, hideActions);
		
		showActions.remove(VentaElectronica.ACCIONSOLICITARCAE);
		hideActions.remove(VentaElectronica.ACCIONSOLICITARCAE);
		
		showActions.remove(VentaElectronica.ACCIONCONFIRMAR);
		hideActions.add(VentaElectronica.ACCIONCONFIRMAR);
		
		showActions.remove(VentaElectronica.ACCIONANULAR);
		hideActions.add(VentaElectronica.ACCIONANULAR);
		
		if (getEstado().equals(Estado.Confirmada)){
			hideActions.add(FacturaVentaContado.ACCIONCONFIRMARCONTADO);
			showActions.add(FacturaVentaContado.ACCIONGENERARCREDITOCONTADO);
			showActions.add(FacturaVentaContado.ACCIONREGISTRARCOBRANZA);
			showActions.add(FacturaVentaContado.ACCIONDEVOLUCIONCONTADO);
		}
		else if (getEstado().equals(Estado.Cancelada)){
			hideActions.add(FacturaVentaContado.ACCIONCONFIRMARCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONGENERARCREDITOCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONREGISTRARCOBRANZA);
			hideActions.add(FacturaVentaContado.ACCIONDEVOLUCIONCONTADO);
		}
		else if (getEstado().equals(Estado.Anulada)){
			hideActions.add(FacturaVentaContado.ACCIONCONFIRMARCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONGENERARCREDITOCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONREGISTRARCOBRANZA);
			hideActions.add(FacturaVentaContado.ACCIONDEVOLUCIONCONTADO);
		}
		else{			
			showActions.add(FacturaVentaContado.ACCIONCONFIRMARCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONGENERARCREDITOCONTADO);
			hideActions.add(FacturaVentaContado.ACCIONREGISTRARCOBRANZA);
			hideActions.add(FacturaVentaContado.ACCIONDEVOLUCIONCONTADO);
		}		
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		if (this.getEmpresa() != null){
			if (this.getEmpresa().getInscriptoIva()){
				return CreditoVenta.class;
			}
			else{
				return CreditoInternoVenta.class;
			}
		}
		else{
			throw new ValidationException("Empresa no asignada: no puede determinar el tipo de comprobante para revertir");
		}
	}
	
	@Override
	public boolean verificarPrecioUnitario(ItemVentaElectronica item) {
		if (this.ventaEcommerce()){
			return false;
		}
		else{
			// siempre verificar el precio unitario, sin importar lo que asigna el usuario
			return true;
		}
		
	}
	
	public boolean refrescarColecciones(){
		return true;
	}
	
	@Override
	public String emailCC() {
		return null;
	}
	
	public ReciboCobranza anularReciboContado(){
		ReciboCobranza recibo = this.buscarReciboCobranzaContado();
		if (recibo != null){
			if (recibo.getEstado().equals(Estado.Confirmada)){
				recibo.anularTransaccion();
			}
			else if (recibo.getEstado().equals(Estado.Borrador) || (recibo.getEstado().equals(Estado.Abierta))){
				recibo.cancelarTransaccion();
			}
		}		
		return recibo;
	}
	
	@Override
	public String viewName(){
		if (this.generadaPorDevolucion()){
			return "GeneradaPorDevolucion";
		}
		else{
			return "FacturaVentaContado";
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getCliente() != null){
			if (this.getCliente().getSinIdentificacion()){
				if (Is.emptyString(this.getDireccion())){
					errores.add("Falta asignar dirección");
				}
				if (this.getCiudad() == null){
					errores.add("Falta asignar ciudad");
				}
			}
		}
		
		Map<String, Object> condicionesVenta = new HashMap<String, Object>();
		if (this.getCondicionVenta() != null){
			condicionesVenta.put(this.getCondicionVenta().getId(), null);
		}
		for(InteresFacturacionVenta interes: this.getFinanciacion()){
			if (condicionesVenta.containsKey(interes.getCondicionVenta().getId())){
				errores.add("Financiación: no puede repetir la condición de venta");
			}
			else{
				condicionesVenta.put(interes.getCondicionVenta().getId(), null);
			}
		}
		
	}

	public TipoEntrega getEntrega() {
		return entrega;
	}

	public void setEntrega(TipoEntrega entrega) {
		this.entrega = entrega;
	}
	
	@Override
	public VentaElectronica generarComprobanteReversion(Sucursal otraSucursal){
		VentaElectronica comprobante = super.generarComprobanteReversion(otraSucursal);
		if (comprobante != null){			
			this.asignarSubEstadoFacturado();
		}		
		return comprobante;
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		BigDecimal importeCredito = BigDecimal.ZERO;
		if (this.generadaPorDevolucion()){
			DevolucionFacturaContado devolucion = (DevolucionFacturaContado)this.creadoPor();
			devolucion.confirmarTransaccion();
						
			Trazabilidad.crearTrazabilidad(devolucion.getCredito(), devolucion.getVenta().tipoTransaccionRevierte().getSimpleName(), this, FacturaVentaContado.class.getSimpleName());
			
			importeCredito = devolucion.getCredito().getTotal();
		}
		else if (this.ventaEcommerce()){
			Trazabilidad.crearTrazabilidad((Transaccion)this.creadoPor(), PedidoML.class.getSimpleName(), this, FacturaVentaContado.class.getSimpleName());
		}
		
		this.evaluarSubEstadoPosConfirmar(this.getTotal().subtract(importeCredito));		
	}
	
	private boolean generadaPorDevolucion(){
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), DevolucionFacturaContado.class.getSimpleName())){
			return true;
		}
		else{
			return false;
		}
	}
	
	public DevolucionFacturaContado generarDevolucion() {
		if (this.getSubestado() != null){
			if (this.getSubestado().getCodigo().equals(FacturaVentaContado.ESTADOCOBRADO) ||
				this.getSubestado().getCodigo().equals(FacturaVentaContado.ESTADOCREDITODEVOLUCION)){
				DevolucionFacturaContado devolucion = this.buscarUltimaDevolucionPendiente(); 
				if (devolucion == null){
					devolucion = new DevolucionFacturaContado();
					devolucion.copiarPropiedades(this);
					devolucion.setVenta(this);
					for(ItemVentaElectronica item: this.getItems()){
						ItemDevolucionFacturaContado itemDevolucion = new ItemDevolucionFacturaContado();
						itemDevolucion.setProducto(item.getProducto());
						itemDevolucion.setCantidad(item.getCantidad());
						itemDevolucion.setUnidadMedida(item.getUnidadMedida());
						itemDevolucion.setDevolucion(item.getCantidad());					
						devolucion.getItems().add(itemDevolucion);
					}
					XPersistence.getManager().persist(devolucion);
				}	
				return devolucion;
			}
			else{
				throw new ValidationException("Solo se puede registrar devoluciones de facturas cobradas");
			}
		}
		else{
			throw new ValidationException("Solo se puede registrar devoluciones de facturas cobradas");
		}
	}
	
	private DevolucionFacturaContado buscarUltimaDevolucionPendiente(){
		Query query = XPersistence.getManager().createQuery("from DevolucionFacturaContado where venta.id = :venta and estado != :anulado and estado != :cancelado order by fechaCreacion desc");
		query.setParameter("venta", this.getId());
		query.setParameter("anulado", Estado.Anulada);
		query.setParameter("cancelado", Estado.Cancelada);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (DevolucionFacturaContado)result.get(0);
		}
		else{
			return null;
		}
	}
	
	@Hidden
	public BigDecimal getCreditoAFavor(){
		BigDecimal credito = BigDecimal.ZERO;
		if (this.generadaPorDevolucion()){
			DevolucionFacturaContado devolucion = (DevolucionFacturaContado)this.creadoPor();
			// se trabaja con dos decimales, para no tener problemas de diferencias con el recibo
			return devolucion.getCredito().getTotal().setScale(2, RoundingMode.HALF_EVEN);
		}
		return credito;
	}
	
	@Hidden	
	public BigDecimal getTotalACobrar() {
		// se trabaja con dos decimales, para no tener problemas de diferencias con el recibo
		return this.getTotal().setScale(2, RoundingMode.HALF_EVEN).subtract(this.getCreditoAFavor());
	}
	
	@Override
	protected void inicializar(){
		super.inicializar();
		
		if (this.getEntrega() == null){
			ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
			calculator.setEntidad(TipoEntrega.class.getSimpleName());
			try{
				this.setEntrega((TipoEntrega)calculator.calculate());
			}
			catch(Exception e){
			}
		}
	}
	
	private boolean ventaEcommerce(){
		return Is.equalAsString(this.getTipoEntidadCreadaPor(), PedidoML.class.getSimpleName());
	}
	
	public void asignarConsumidorFinal(){
		this.setCliente(Cliente.buscarSinIdentificacion());
		if (this.getCliente() == null){
			throw new ValidationException("No esta definido el cliente sin identificación");
		}
		this.setDomicilioEntrega(this.getCliente().getDomicilio());
		this.setDireccion(this.getDomicilioEntrega().getDireccion());
		this.setCiudad(this.getDomicilioEntrega().getCiudad());
	}
	
	@ElementCollection
	@ListProperties("condicionVenta.nombre, importe")
	private Collection<InteresFacturacionVenta> financiacion = new LinkedList<InteresFacturacionVenta>();

	public Collection<InteresFacturacionVenta> getFinanciacion() {
		return financiacion;
	}

	public void setFinanciacion(Collection<InteresFacturacionVenta> financiacion) {
		if (financiacion != null){
			this.financiacion = financiacion;
		}
		else{
			this.financiacion = new LinkedList<InteresFacturacionVenta>();
		}
	}
	
	@Override
	protected boolean calculaIntereses(){
		if (this.ventaEcommerce()){
			return false;
		}
		else{
			return true;
		}
	}
	
	protected Collection<InteresFacturacionVenta> financiamiento(){
		Collection<InteresFacturacionVenta> montosAFinanciar = new LinkedList<InteresFacturacionVenta>();
		if (this.getFinanciacion() != null && !this.getFinanciacion().isEmpty()){
			montosAFinanciar.addAll(this.getFinanciacion());			
		}
		
		// Saldo de financiacion
		if (this.getCondicionVenta() != null && this.getCondicionVenta().getPorcentajeInteres().compareTo(BigDecimal.ZERO) > 0){
			InteresFacturacionVenta intereses = new InteresFacturacionVenta();
			intereses.setCondicionVenta(this.getCondicionVenta());
			// se pone en cero para indicar que es el saldo restante
			intereses.setImporte(BigDecimal.ZERO);
			montosAFinanciar.add(intereses);
		}
		return montosAFinanciar;
	}
	
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		this.setFechaServicio(UtilERP.trucarDateTime(new Date()));
		this.setFechaVencimiento(UtilERP.trucarDateTime(new Date()));
		this.setFinanciacion(null);
	}
	
	@Override
	public ObjetoNegocio generarCopia() {
		FacturaVentaContado copia = new FacturaVentaContado();
		copia.copiarPropiedades(this);		
		copia.setItems(new ArrayList<ItemVentaElectronica>());
		XPersistence.getManager().persist(copia);

		for(ItemVentaElectronica item: this.getItems()){
			ItemVentaElectronica itemCopia = new ItemVentaElectronica();
			itemCopia.copiarPropiedades(item);
			itemCopia.setVenta(copia);
			copia.getItems().add(itemCopia);
			
			XPersistence.getManager().persist(copia);
		}

		return copia;		
	}
	
	@Override
	public boolean esFactura() {
		return true;
	}
}
