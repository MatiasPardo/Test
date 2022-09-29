package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal{" +
		"Principal[#" + 
			"descripcion, estado;" + 			
			"numero, fecha, fechaCreacion;" +
			"depositoPorConsignacion, depositoDevolucion;" +
			"cliente;" + 
			"observaciones];" +
		"items;" + 
	"}" + 
	"Remito{remito} Trazabilidad{trazabilidad}"
	),
	@View(name="Simple", members="numero, estado;")
})


@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, numero, estado, cliente.codigo, cliente.nombre, depositoPorConsignacion.codigo, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class LiquidacionConsignacion extends Transaccion implements ITransaccionInventario{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly	
	@ReferenceView("Liquidacion")
	private Remito remito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
				condition="${consignacion} = 't'")	
	@NoCreate @NoModify
	@ReadOnly
	private Deposito depositoPorConsignacion;

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")	
	@NoCreate @NoModify
	private Deposito depositoDevolucion;

	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("Transaccion")
	@ReadOnly
	private Cliente cliente;
			
	@OneToMany(mappedBy="liquidacion", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, despacho.codigo, facturar, devolucion")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsLiquidacionConsignacion.DevolucionCompleta")
	private Collection<ItemLiquidacionConsignacion> items;
	
	public Deposito getDepositoDevolucion() {
		return depositoDevolucion;
	}

	public void setDepositoDevolucion(Deposito depositoDevolucion) {
		this.depositoDevolucion = depositoDevolucion;
	}

	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Deposito getDepositoPorConsignacion() {
		return depositoPorConsignacion;
	}

	public void setDepositoPorConsignacion(Deposito depositoPorConsignacion) {
		this.depositoPorConsignacion = depositoPorConsignacion;
	}

	public Collection<ItemLiquidacionConsignacion> getItems() {
		return items;
	}

	public void setItems(Collection<ItemLiquidacionConsignacion> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Liquidación Consignación";
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		for(ItemLiquidacionConsignacion item: this.getItems()){			
			if (item.getCantidadTotal().compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException("Cantidad en cero");
			}
			
			movimientos.add(item);
			if (item.getDevolucion().compareTo(BigDecimal.ZERO) > 0){
				ItemMovInvIngresoProxy ingresoPorDevolucion = new ItemMovInvIngresoProxy(item);
				ingresoPorDevolucion.setCantidad(item.cantidadPorDevolucion());
				ingresoPorDevolucion.setDeposito(this.getDepositoDevolucion());
				movimientos.add(ingresoPorDevolucion);
			}
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
		for (ItemLiquidacionConsignacion item: this.getItems()){
			if (item.getItemRemito() != null){
				IItemPendientePorCantidad pendientePorCantidad = item.getItemRemito().itemPendienteLiquidacionProxy();
				if (pendientePorCantidad != null){					
					Cantidad cantidadPendiente = pendientePorCantidad.getCantidadACancelar();
					cantidadPendiente.setCantidad(item.getFacturar().add(item.getDevolucion()));
					cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
					estrategia.getItemsPendientes().add(pendientePorCantidad);
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
	// WorkFlow:  FACTURA VENTA o FACTURA MANUAL
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(FacturaVenta.class);
		tipoTrsDestino.add(FacturaManual.class);
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
		if (FacturaVenta.class.equals(tipoTrDestino)){
			if (this.getEmpresa().getInscriptoIva()){
				for(ItemLiquidacionConsignacion item: this.getItems()){
					if (item.itemPendienteFacturaVentaProxy() != null){
						return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
					}
				}
				return false;
			}
			else{
				// no inscripto en iva genera factura manual
				return false;
			}
		}
		else if (FacturaManual.class.equals(tipoTrDestino)){
			if (!this.getEmpresa().getInscriptoIva()){
				for(ItemLiquidacionConsignacion item: this.getItems()){
					if (item.itemPendienteFacturaVentaProxy() != null){
						return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
					}
				}
				return false;
			}
			else{
				// inscripto en iva genera factura venta
				return false;
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
			// Ahora se configura en la entidad
			// para garantizar que la moneda sea siempre la configurada para la facturación
			//destino.copiarPropiedades(this.getRemito());
			// factura.setMoneda(factura.buscarMonedaDefault());
			this.getRemito().inicializarTrCreadaPorWorkFlow(destino);
			factura.setLiquidacion(this);			
			Cliente cliente = factura.getCliente();
			BigDecimal descuentoFinanciero = cliente.buscarDescuentoFinanciero();
			PedidoVenta pedido = this.getRemito().primerPedidoAsociado();
			if (pedido != null){
				factura.setCondicionVenta(pedido.getCondicionVenta());
				factura.setPorcentajeDescuento(pedido.getPorcentajeDescuento());
			}
			factura.setPorcentajeFinanciero(descuentoFinanciero);
		}
		else if (destino.getClass().equals(FacturaManual.class)){
			FacturaManual factura = (FacturaManual)destino;
			// Ahora se configura en la entidad
			// para garantizar que la moneda sea siempre la configurada para la facturación
			// destino.copiarPropiedades(this.getRemito());
			// factura.setMoneda(factura.buscarMonedaDefault());
			this.getRemito().inicializarTrCreadaPorWorkFlow(destino);
			factura.setLiquidacion(this);
			PedidoVenta pedido = this.getRemito().primerPedidoAsociado();
			if (pedido != null){
				factura.setCondicionVenta(pedido.getCondicionVenta());
				factura.setPorcentajeDescuento(pedido.getPorcentajeDescuento());
			}
		}		
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(FacturaVenta.class) || (destino.getClass().equals(FacturaManual.class))){
			VentaElectronica factura = (VentaElectronica)destino;
			factura.setItems(new LinkedList<ItemVentaElectronica>());
			
			boolean primerItemPedido = true;
			for(IItemPendiente itemPendiente: items){
				ItemLiquidacionConsignacion itemorigen = (ItemLiquidacionConsignacion)itemPendiente.getItem();
				ItemVentaElectronica itemdestino = new ItemVentaElectronica();
				itemdestino.setVenta(factura);
				itemdestino.copiarPropiedades(itemorigen);
				itemdestino.setCantidad(itemorigen.getFacturar());
				itemdestino.setItemLiquidacion(itemorigen);
				
				if (itemorigen.getItemRemito() != null){
					if (itemorigen.getItemRemito().getItemOrdenPreparacion() != null){
						EstadisticaPedidoVenta itempedido = itemorigen.getItemRemito().getItemOrdenPreparacion().getItemPedidoVenta();
						if (itempedido != null){
							itemdestino.setItemPedido(itempedido);
							itemdestino.setDetalle(itempedido.getDetalle());
							itemdestino.setCentroCostos(itempedido.getCentroCostos());
							if (primerItemPedido){
								primerItemPedido = false;
								factura.setZonaReparto(itempedido.getVenta().getZonaReparto());
							}
						}
					}
				}				
				itemdestino.recalcular();
				factura.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);					
			}
		}		
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Query query = XPersistence.getManager().createQuery("from FacturaVenta f where f.liquidacion = :transaccion");
		query.setParameter("transaccion", this);
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados);
		
		query = XPersistence.getManager().createQuery("from FacturaManual f where f.liquidacion = :transaccion");
		query.setParameter("transaccion", this);
		@SuppressWarnings("unchecked")
		List<Transaccion> resultados2 = (List<Transaccion>)query.getResultList();
		trs.addAll(resultados2);
	}
	
	public Pendiente buscarPendienteFactura(){
		return this.buscarPendienteGeneradoPorTr(PendienteFacturaVenta.class.getSimpleName());
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("CLIENTE_CODIGO", this.getCliente().getCodigo());
		parameters.put("CLIENTE_NOMBRE", this.getCliente().getNombre());
		parameters.put("CLIENTE_NOMBREFANTASIA", this.getCliente().getNombreFantasia());
		if (this.getCliente().getVendedor() != null){
			parameters.put("CLIENTE_VENDEDOR", this.getCliente().getVendedor().getNombre());
		}
		else{
			parameters.put("CLIENTE_VENDEDOR", "");
		}
		parameters.put("CLIENTE_DIRECCION", this.getCliente().getDomicilio().getDireccion());
		parameters.put("CLIENTE_CIUDAD", this.getCliente().getDomicilio().getCiudad().getCiudad());
		parameters.put("CLIENTE_TELEFONO", this.getCliente().getTelefono());
		parameters.put("CLIENTE_MAIL1", this.getCliente().getMail1());
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getCliente() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		else{
			return null;
		}
	}
	
	public void devolverTodos() {
		if (!this.soloLectura()){
			for(ItemLiquidacionConsignacion item: this.getItems()){
				if (item.getFacturar().compareTo(BigDecimal.ZERO) > 0){
					BigDecimal cantidad = item.getFacturar();
					item.setFacturar(BigDecimal.ZERO);
					item.setDevolucion(item.getDevolucion().add(cantidad));
					item.recalcular();
				}
			}
		}
	}
}
