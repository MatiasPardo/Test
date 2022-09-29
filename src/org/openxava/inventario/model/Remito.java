package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.actions.*;
import org.openxava.inventario.validators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;


@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +
			"numerador;" + 
			"deposito, porConsignacion, depositoPorConsignacion;" +
			"cliente;" + 
			"domicilioEntrega;" + 
			"observaciones];" +
	"Items{items} Trazabilidad{trazabilidad};"		
	),
	@View(name="Simple", members="numero, estado;"),
	@View(name="Liquidacion", members=
	"Principal[#" + 			 			
			"estado, usuario, fechaCreacion;" +
			"numero, fecha;" +
			"deposito, porConsignacion;" +
			"cliente;" + 
			"domicilioEntrega;" + 
			"observaciones];" +
	"items;" 		
	),
	@View(name="ReciboCobranza", members=
		"Principal{Principal[#" + 
				"descripcion, estado, usuario;" + 			
				"numero, fecha, fechaCreacion;" +
				"numerador, deposito;" + 
				"Factura[numeroFactura; nombreCliente];" +  
				"observaciones];" + 
				"items;}" +
		 "Trazabilidad{trazabilidad};"		
	),
	@View(name="ControlMercaderia",
	members=
		"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +
			"numerador;" + 
			"deposito, porConsignacion, depositoPorConsignacion;" +
			"cliente;" + 
			"domicilioEntrega;" + 
			"observaciones];" +
		"Items{items} Trazabilidad{trazabilidad};"		
	),
	@View(name="Simple", members="numero, estado;"),	
})

@Tab(
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		properties="fecha, numero, estado, deposito.codigo, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

@EntityValidators({
	@EntityValidator(value=DepositoPorConsignacionAsignadoValidator.class,
			properties={@PropertyValue(from="depositoPorConsignacion", name="depositoPorConsignacion"),
						@PropertyValue(from="porConsignacion", name="porConsignacion")})
})

public class Remito extends Transaccion implements ITransaccionInventario{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", 
				condition="${entidad.entidad} = 'Remito'")	
	@NoCreate @NoModify	
	private Numerador numerador;
	
	@ReadOnly
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean porConsignacion = Boolean.FALSE; 
	
	@ReadOnly
	@Hidden
	private Boolean noFacturar = Boolean.FALSE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito deposito;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")	
	@NoCreate @NoModify	
	@ReadOnly
	private Deposito depositoPorConsignacion;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("Transaccion")
	@OnChange(OnChangeClienteEnRemito.class)
	private Cliente cliente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="Simple")
	@SearchAction(value="ReferenciaDomicilioVenta.buscar")
	private Domicilio domicilioEntrega;
	
	@OneToMany(mappedBy="remito", cascade=CascadeType.ALL)
	@ListsProperties({
		@ListProperties(value="producto.codigo, producto.nombre, cantidad, despacho.codigo", notForViews="Liquidacion, ControlMercaderia"),
		@ListProperties(value="producto.codigo, producto.nombre, despacho.codigo, pendienteLiquidacion, cantidad", forViews="Liquidacion, ControlMercaderia"),
	})
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	private Collection<ItemRemito> items;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	private OrdenPreparacion ordenPreparacion;
	
	public Boolean getPorConsignacion() {
		return porConsignacion == null ? Boolean.FALSE : this.porConsignacion;
	}

	public void setPorConsignacion(Boolean porConsignacion) {
		this.porConsignacion = porConsignacion;
	}

	public Domicilio getDomicilioEntrega() {
		return domicilioEntrega;
	}
	
	public void setDomicilioEntrega(Domicilio domicilioEntrega) {
		this.domicilioEntrega = domicilioEntrega;
	}

	public OrdenPreparacion getOrdenPreparacion() {
		return ordenPreparacion;
	}

	public void setOrdenPreparacion(OrdenPreparacion ordenPreparacion) {
		this.ordenPreparacion = ordenPreparacion;
	}
	
	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}
	
	public Deposito getDepositoPorConsignacion() {
		return depositoPorConsignacion;
	}

	public void setDepositoPorConsignacion(Deposito depositoPorConsignacion) {
		this.depositoPorConsignacion = depositoPorConsignacion;
	}

	public Collection<ItemRemito> getItems() {
		return items;
	}

	public void setItems(Collection<ItemRemito> items) {
		this.items = items;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Numerador getNumerador() {
		return numerador;
	}
	
	@Override
	protected void inicializar(){
		super.inicializar();
		
		if (this.getNumerador() == null){
			this.setNumerador(this.buscarNumeradorDefault());			
		}
	}
	
	@Override
	protected Numerador numeradorSeleccionado() {
		return this.getNumerador();
	}
	
	public void setNumerador(Numerador numerador) {
		this.numerador = numerador;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Remito";
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		
		if (this.getPorConsignacion()){
			Deposito depConsignacion = this.getDepositoPorConsignacion();
			if (depConsignacion != null){
				if (!depConsignacion.equals(this.getDeposito())){
					// Se egresa la mercadería reservada (EgresoDesreserva)
					movimientos.addAll(this.getItems());					
					// se hace un ingreso al depósito en consignación y se reserva la mercaderia
					for (IItemMovimientoInventario itemRemito: this.getItems()){
						ItemMovInvIngresoReservaProxy itemIngresoPorConsignacion = new ItemMovInvIngresoReservaProxy(itemRemito);
						itemIngresoPorConsignacion.setDeposito(depConsignacion);
						movimientos.add(itemIngresoPorConsignacion);
					}
				}
				else{
					// es el mismo depósito. La mercadería ya esta reservada.
				}
			}
			else{
				throw new ValidationException("No esta asignado el depósito por consignación");
			}
		}
		else if (this.transferenciaEntreSucursales()){
			// Se egresa la mercadería reservada (EgresoDesreserva)
			movimientos.addAll(this.getItems());
			// se hace un ingreso al depósito de la sucursal que pidió la mercadería y se la reserva 
			Deposito depConsignacion = this.getDepositoPorConsignacion();
			if (depConsignacion == null){
				throw new ValidationException("Es una transferencia entre sucursales: falta asignar el depósito de la sucursal destino");
			}
			else if (this.getDeposito().equals(this.getDepositoPorConsignacion())){
				throw new ValidationException("No pueden coincidir los depósitos");
			}
			for (IItemMovimientoInventario itemRemito: this.getItems()){
				ItemMovInvIngresoReservaProxy itemIngresoPorConsignacion = new ItemMovInvIngresoReservaProxy(itemRemito);
				itemIngresoPorConsignacion.setDeposito(depConsignacion);
				movimientos.add(itemIngresoPorConsignacion);				
			}			
		}
		else{
			// Se egresa la mercadería reservada (EgresoDesreserva)
			movimientos.addAll(this.getItems());
		}	
		
		return movimientos;
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
		for (ItemRemito item: this.getItems()){
			if (item.getItemOrdenPreparacion() != null){
				IItemPendiente itemPendiente = item.getItemOrdenPreparacion().itemPendienteRemitoProxy();
				if (itemPendiente != null){
					estrategia.getItemsPendientes().add(itemPendiente);	
				}					
			}
			else{
				ObjetoNegocio objeto = item.creadoPor();
				if (objeto != null){
					if (objeto instanceof ItemVentaElectronica){
						IItemPendiente itemPendiente = ((ItemVentaElectronica)objeto).itemPendienteRemitoProxy();
						if (itemPendiente != null){
							estrategia.getItemsPendientes().add(itemPendiente);
						}
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
	
	// WorkFlow: 
	//  FACTURA VENTA o FACTURA MANUAL
	//  LIQUIDACION CONSIGNACION
	//  REMITO SOLO
	//  CONTROL MERCADERIA: ENTRE SUCURSALES
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(FacturaVenta.class);
		tipoTrsDestino.add(FacturaManual.class);
		tipoTrsDestino.add(LiquidacionConsignacion.class);
		tipoTrsDestino.add(ControlMercaderia.class);
	}
	
	@Override
	public Class<?> getTipoPendiente(Class<?> tipoTransaccionDestino){
		if (FacturaManual.class.equals(tipoTransaccionDestino)){
			// se utiliza el mismo pendiente que el de factura de venta.
			return PendienteFacturaVenta.class;
		}
		else{
			return super.getTipoPendiente(tipoTransaccionDestino);
		}
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){	
		if ((FacturaVenta.class.equals(tipoTrDestino)) || (FacturaManual.class.equals(tipoTrDestino))){
			if (this.getCliente().getSucursal()){
				// Si es una sucursal, no se factura
				return false;
			}
			else if (this.getPorConsignacion()){
				// no se factura, queda pendiente de liquidar la consignación
				return false;
			}
			else if (this.transferenciaEntreSucursales()){
				// queda pendiente de control de mercadería
				return false;
			}
			else if (this.getNoFacturar()){
				return false;
			}
			else{
				// Si la empresa es inscripta, siempres se genera una factura venta.
				// Caso contrario, factura Manual
				if ((FacturaVenta.class.equals(tipoTrDestino)) && (!this.getEmpresa().getInscriptoIva())){
					return false;
				}
				else if ((FacturaManual.class.equals(tipoTrDestino)) && (this.getEmpresa().getInscriptoIva())){
					return false;
				}
				else{
					return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
				}
				
			}
		}
		else if (LiquidacionConsignacion.class.equals(tipoTrDestino)){
			if (!this.getPorConsignacion()){
				return false;
			}
			else{
				return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
			}
		}
		else if (ControlMercaderia.class.equals(tipoTrDestino)){
			if (!this.transferenciaEntreSucursales()){
				return false;
			}
			else{
				return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
			}
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(FacturaVenta.class)){
			FacturaVenta factura = (FacturaVenta)destino;
			// Ahora se configura en la entidad:
			// para garantizar que la moneda sea siempre la configurada para la facturación
			// factura.setMoneda(factura.buscarMonedaDefault());
			factura.setRemito(this);
			Cliente cliente = factura.getCliente();
			BigDecimal descuentoFinanciero = cliente.buscarDescuentoFinanciero();
			PedidoVenta pedido = this.primerPedidoAsociado();
			if (pedido != null){
				factura.setCondicionVenta(pedido.getCondicionVenta());
				factura.setPorcentajeDescuento(pedido.getPorcentajeDescuento());
			}
			factura.setPorcentajeFinanciero(descuentoFinanciero);
		}
		else if (destino.getClass().equals(FacturaManual.class)){
			FacturaManual factura = (FacturaManual)destino;
			// Ahora se configura en la entidad
			//factura.setMoneda(factura.buscarMonedaDefault());
			factura.setRemito(this);
			PedidoVenta pedido = this.primerPedidoAsociado();
			if (pedido != null){
				factura.setCondicionVenta(pedido.getCondicionVenta());
				factura.setPorcentajeDescuento(pedido.getPorcentajeDescuento());
			}
		}
		else if (destino.getClass().equals(LiquidacionConsignacion.class)){
			LiquidacionConsignacion liquidacion = (LiquidacionConsignacion)destino;
			liquidacion.setRemito(this);
			liquidacion.setDepositoDevolucion(this.getDeposito());
		}
		else if (destino.getClass().equals(ControlMercaderia.class)){
			ControlMercaderia control = (ControlMercaderia)destino;
			control.setRemito(this);
			control.setOrigen(this.getDeposito());
			control.setRecepcion(this.depositoPorConsignacion);
			control.setResultado(ResultadoControlMercaderia.MercaderiaRecibida);
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(FacturaVenta.class)){
			FacturaVenta factura = (FacturaVenta)destino;
			factura.setItems(new LinkedList<ItemVentaElectronica>());
			
			boolean primerItemPedido = true;
			for(IItemPendiente itemPendiente: items){
				ItemRemito itemorigen = (ItemRemito)itemPendiente.getItem();
				ItemVentaElectronica itemdestino = new ItemVentaElectronica();
				itemdestino.setVenta(factura);
				itemdestino.copiarPropiedades(itemorigen);
				itemdestino.setItemRemito(itemorigen);
				EstadisticaPedidoVenta itempedido = null;
				if (itemorigen.getItemOrdenPreparacion() != null){
					itempedido = itemorigen.getItemOrdenPreparacion().getItemPedidoVenta();					
				}
				if (itempedido != null){					
					itemdestino.setPrecioUnitario(Transaccion.convertirMoneda(itempedido.getVenta(), factura, itempedido.getPrecioUnitario()));
					itemdestino.setPorcentajeDescuento(itempedido.getPorcentajeDescuento());
					itemdestino.setCentroCostos(itempedido.getCentroCostos());
					itemdestino.setItemPedido(itempedido);
					itemdestino.setDetalle(itempedido.getDetalle());
					
					if (primerItemPedido){
						primerItemPedido = false;
						factura.setZonaReparto(itempedido.getVenta().getZonaReparto());
					}
				}
				else{		
					BigDecimal precio = factura.getCliente().calcularPrecio(null, itemdestino.getProducto(), itemdestino.getUnidadMedida(), itemdestino.getCantidad(), factura);
					if (precio == null) precio = BigDecimal.ZERO;
					itemdestino.setPrecioUnitario(precio);
				}				
				itemdestino.recalcular();
				factura.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);					
			}
		}
		else if (destino.getClass().equals(FacturaManual.class)){
			FacturaManual factura = (FacturaManual)destino;
			factura.setItems(new LinkedList<ItemVentaElectronica>());
			
			boolean primerItemPedido = true;
			for(IItemPendiente itemPendiente: items){
				ItemRemito itemorigen = (ItemRemito)itemPendiente.getItem();
				ItemVentaElectronica itemdestino = new ItemVentaElectronica();
				itemdestino.setVenta(factura);
				itemdestino.copiarPropiedades(itemorigen);
				itemdestino.setItemRemito(itemorigen);
				EstadisticaPedidoVenta itempedido = null;
				if (itemorigen.getItemOrdenPreparacion() != null){
					itempedido = itemorigen.getItemOrdenPreparacion().getItemPedidoVenta();					
				}
				if (itempedido != null){					
					itemdestino.setPrecioUnitario(Transaccion.convertirMoneda(itempedido.getVenta(), factura, itempedido.getPrecioUnitario()));
					itemdestino.setPorcentajeDescuento(itempedido.getPorcentajeDescuento());
					itemdestino.setCentroCostos(itempedido.getCentroCostos());
					itemdestino.setItemPedido(itempedido);
					itemdestino.setDetalle(itempedido.getDetalle());
					
					if (primerItemPedido){
						primerItemPedido = false;
						factura.setZonaReparto(itempedido.getVenta().getZonaReparto());
					}
				}
				else{		
					BigDecimal precio = factura.getCliente().calcularPrecio(null, itemdestino.getProducto(), itemdestino.getUnidadMedida(), itemdestino.getCantidad(), factura);
					if (precio == null) precio = BigDecimal.ZERO;
					itemdestino.setPrecioUnitario(precio);
				}
				itemdestino.recalcular();
				factura.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);					
			}
		}
		else if (destino.getClass().equals(LiquidacionConsignacion.class)){
			LiquidacionConsignacion liquidacion = (LiquidacionConsignacion)destino;
			liquidacion.setItems(new LinkedList<ItemLiquidacionConsignacion>());
			
			for(IItemPendiente itemPendiente: items){
				ItemRemito itemorigen = (ItemRemito)itemPendiente.getItem();
				if (itemorigen.getPendienteLiquidacion().compareTo(BigDecimal.ZERO) > 0){
					ItemLiquidacionConsignacion itemdestino = new ItemLiquidacionConsignacion();
					itemdestino.setLiquidacion(liquidacion);
					itemdestino.copiarPropiedades(itemorigen);
					itemdestino.setItemRemito(itemorigen);
					((ItemPendienteLiquidacionConsignacionProxy)itemPendiente).modificarCantidadesParaLiquidacion(itemdestino);
					itemdestino.recalcular();
					liquidacion.getItems().add(itemdestino);
					XPersistence.getManager().persist(itemdestino);
				}
			}
		}
		else if (destino.getClass().equals(ControlMercaderia.class)){
			ControlMercaderia control = (ControlMercaderia)destino;
			control.setItems(new LinkedList<ItemControlMercaderia>());
			for(IItemPendiente itemPendiente: items){
				ItemRemito itemorigen = (ItemRemito)itemPendiente.getItem();
				if (itemorigen.getPendienteLiquidacion().compareTo(BigDecimal.ZERO) > 0){
					ItemControlMercaderia itemdestino = new ItemControlMercaderia();
					itemdestino.setControlMercaderia(control);
					itemdestino.copiarPropiedades(itemorigen);
					itemdestino.setItemRemito(itemorigen);
					itemdestino.setCantidad(itemorigen.getPendienteLiquidacion());
					itemdestino.recalcular();
					control.getItems().add(itemdestino);
					XPersistence.getManager().persist(itemdestino);
				}
			}
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Query query = XPersistence.getManager().createQuery("from FacturaVenta f where f.remito = :transaccion");
		query.setParameter("transaccion", this);
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados);
		
		query = XPersistence.getManager().createQuery("from LiquidacionConsignacion f where f.remito = :transaccion");
		query.setParameter("transaccion", this);		
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados2 = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados2);
		
		query = XPersistence.getManager().createQuery("from FacturaManual f where f.remito = :transaccion");
		query.setParameter("transaccion", this);
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados3 = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados3);
		
		query = XPersistence.getManager().createQuery("from ControlMercaderia f where f.remito = :transaccion");
		query.setParameter("transaccion", this);		
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados4 = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados4);
	}
	
	@Override
	public String nombreReporteImpresion() {
		if (this.getPorConsignacion()){
			// Se intenta ejecutar el reporte por consignación, si no existe, ejecutar el reporte de remito común
			String reportePorConsignacion = "RemitoConsignacion_reporte.jrxml";
			if (this.configurador().getImpresionPorEmpresa()){
				reportePorConsignacion = "RemitoConsignacion" + Integer.toString(this.getEmpresa().getNumero()) + "_reporte.jrxml";
			}
			try{
				ConfiguracionERP.fullFileNameReporte(reportePorConsignacion);
			}
			catch(Exception e){
				reportePorConsignacion = super.nombreReporteImpresion();
			}
			return reportePorConsignacion;
		}
		else{
			return super.nombreReporteImpresion();
		}
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.generadaPorFacturaContado() && this.getCliente().getSinIdentificacion()){
			VentaElectronica factura = this.buscarFacturaVentaContado();			
			parameters.put("RAZONSOCIAL_CLIENTE", factura.getRazonSocial());
			parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
			parameters.put("CUIT_CLIENTE", factura.getCuit());
			parameters.put("TIPODOCUMENTO_CLIENTE", factura.getTipoDocumento().toString());
			parameters.put("POSICIONIVA_CLIENTE", factura.getPosicionIva().getDescripcion());
			
			parameters.put("DIRECCION_CLIENTE", factura.getDireccion());
			parameters.put("CODIGOPOSTAL_CLIENTE", factura.getCiudad().getCodigoPostal().toString());
			parameters.put("CIUDAD_CLIENTE", factura.getCiudad().getCiudad());
			parameters.put("PROVINCIA_CLIENTE", factura.getCiudad().getProvincia().getProvincia());
						
			parameters.put("DIRECCION_ENTREGA", factura.getDireccion());
			parameters.put("CODIGOPOSTAL_ENTREGA", factura.getCiudad().getCodigoPostal().toString());
			parameters.put("CIUDAD_ENTREGA", factura.getCiudad().getCiudad());
			parameters.put("PROVINCIA_ENTREGA", factura.getCiudad().getProvincia().getProvincia());
		}
		else{
			parameters.put("RAZONSOCIAL_CLIENTE", this.getCliente().getNombre());
			parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
			parameters.put("CUIT_CLIENTE", this.getCliente().getNumeroDocumento());
			parameters.put("TIPODOCUMENTO_CLIENTE", this.getCliente().getTipoDocumento().toString());
			parameters.put("POSICIONIVA_CLIENTE", this.getCliente().getPosicionIva().getDescripcion());
			parameters.put("DIRECCION_CLIENTE", this.getCliente().getDomicilio().getDireccion());
			parameters.put("CODIGOPOSTAL_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCodigoPostal().toString());
			parameters.put("CIUDAD_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCiudad());
			parameters.put("PROVINCIA_CLIENTE", this.getCliente().getDomicilio().getCiudad().getProvincia().getProvincia());		
			parameters.put("DIRECCION_ENTREGA", this.getDomicilioEntrega().getDireccion());
			parameters.put("CODIGOPOSTAL_ENTREGA", this.getDomicilioEntrega().getCiudad().getCodigoPostal().toString());
			parameters.put("CIUDAD_ENTREGA", this.getDomicilioEntrega().getCiudad().getCiudad());
			parameters.put("PROVINCIA_ENTREGA", this.getDomicilioEntrega().getCiudad().getProvincia().getProvincia());
		}
		
		parameters.put("DEPOSITO_CODIGO", this.getDeposito().getCodigo());
		parameters.put("DEPOSITO_NOMBRE", this.getDeposito().getNombre());
		parameters.put("CONSIGNACION", this.getPorConsignacion());
		if (this.getDepositoPorConsignacion() != null){
			parameters.put("DEPOSITOPORCONSIGNACION_CODIGO", this.getDepositoPorConsignacion().getCodigo());
			parameters.put("DEPOSITOPORCONSIGNACION_NOMBRE", this.getDepositoPorConsignacion().getNombre());
		}
	}
	
	public PedidoVenta primerPedidoAsociado(){
		for(ItemRemito item: this.getItems()){
			if (item.getItemOrdenPreparacion() != null){
				return item.getItemOrdenPreparacion().getPedido();				
			}
		}
		
		return null;
	}
	
	public Pendiente buscarPendienteFactura(){
		return this.buscarPendienteGeneradoPorTr(PendienteFacturaVenta.class.getSimpleName());
	}
	
	@Override
	protected void asignarSucursal(){		
		if (this.getDeposito() != null){
			this.setSucursal(this.getDeposito().getSucursal());
		}
	}

	public Boolean getNoFacturar() {
		return noFacturar;
	}

	public void setNoFacturar(Boolean noFacturar) {
		if (noFacturar == null){
			this.noFacturar = Boolean.FALSE;
		}
		else{
			this.noFacturar = noFacturar;
		}
	}
	
	@Override
	public String viewName(){
		if (this.getNoFacturar()){
			if (this.generadaPorFacturaContado()){
				return "ReciboCobranza";
			}
			else{
				return super.viewName();
			}
		}
		else{
			return super.viewName();
		}
	}
	
	private boolean generadaPorFacturaContado(){
		if (this.getItems() != null){
			for(ItemRemito item: this.getItems()){
				if (Is.equalAsString(item.getTipoEntidadCreadaPor(), ItemVentaElectronica.class.getSimpleName())){
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	private VentaElectronica buscarFacturaVentaContado(){
		if (this.generadaPorFacturaContado()){
			return ((ItemVentaElectronica)XPersistence.getManager().find(ItemVentaElectronica.class, this.getItems().iterator().next().getIdCreadaPor())).getVenta();
		}
		else{
			return null;
		}
	}
	
	@Hidden
	public String getNumeroFactura(){
		Transaccion tr = this.buscarFacturaVentaContado();
		if (tr != null){
			return tr.getNumero();
		}
		else{
			return "";
		}
	}
	
	@Hidden
	public String getNombreCliente(){
		VentaElectronica tr = this.buscarFacturaVentaContado();
		if (tr != null){
			return tr.getRazonSocial();
		}
		else{
			return "";
		}
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getCliente() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		else{
			return null;
		}
	}
	
	private boolean transferenciaEntreSucursales(){
		if (this.getOrdenPreparacion() != null){
			return this.getOrdenPreparacion().transferenciaEntreSucursales();
		}
		else{
			return false;
		}
	}
}
