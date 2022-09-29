package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal{" + 
			"descripcion, empresa;" +
			"numero, fecha, fechaCreacion;" +
			"estado, subestado, ultimaTransicion;" + 			
			"cliente;" +
			"deposito, porConsignacion;" + 
			"observaciones;" +
	"items;}" + 
	"Trazabilidad{trazabilidad}"
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		properties="fecha, numero, estado, subestado.nombre, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class OrdenPreparacion extends Transaccion implements ITransaccionInventario, IAccionCancelacionPendientes{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@ReadOnly
	private Boolean porConsignacion = Boolean.FALSE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@DefaultValueCalculator(DepositoDefaultCalculator.class)
	@NoCreate @NoModify
	private Deposito deposito;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	private String idCreadaPor;
	
	@ReadOnly
	@Hidden
	@Column(length=100)
	private String tipoEntidadCreadaPor;
	
	@OneToMany(mappedBy="ordenPreparacion", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, despacho.codigo, stock, noPreparar")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@DetailAction(value="ItemTransaccion.dividir")
	private Collection<ItemOrdenPreparacion> items;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private PedidoVenta pedido;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public Collection<ItemOrdenPreparacion> getItems() {
		if (items == null){
			items = new ArrayList<ItemOrdenPreparacion>();
		}
		return items;
	}

	public void setItems(Collection<ItemOrdenPreparacion> items) {
		this.items = items;
		
	}
	
	public Boolean getPorConsignacion() {
		return porConsignacion;
	}

	public void setPorConsignacion(Boolean porConsignacion) {
		this.porConsignacion = porConsignacion;
	}

	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
		for (ItemOrdenPreparacion item: this.getItems()){
			IItemPendientePorCantidad pendientePorCantidad = null;
			if (item.getItemPedidoVenta() != null){
				pendientePorCantidad = item.getItemPedidoVenta().itemPendienteOrdenPreparacionProxy();
			}
			else if (item.getItemSolicitud() != null){
				pendientePorCantidad = item.getItemSolicitud().itemPendienteOrdenPreparacionProxy();
			}
			
			if (pendientePorCantidad != null){					
				BigDecimal cantidadACancelar = item.getCantidad().add(item.getNoPreparar()).subtract(item.getExcedePedido());					
									
				((ItemPendienteOrdenPreparacionProxy)pendientePorCantidad).setItemOrdenPreparacion(item);
				Cantidad cantidadPendiente = pendientePorCantidad.getCantidadACancelar();
				cantidadPendiente.setCantidad(cantidadACancelar);
				cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
				estrategia.getItemsPendientes().add(pendientePorCantidad);
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
	public ArrayList<IItemMovimientoInventario> movimientosInventario(){
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		for(ItemOrdenPreparacion item: this.getItems()){
			if (item.afectaStock()){
				movimientos.add(item);
			}
		}
		return movimientos;
	}
	
	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		if (this.getDeposito() == null){
			errores.add("Deposito no asignado");
		}
	}
	
	// WorkFlow Remito de Venta
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(Remito.class);
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){
		if (Remito.class.equals(tipoTrDestino)){
			for(ItemOrdenPreparacion item: this.getItems()){
				if (item.afectaStock()){
					return true;
				}
			}
			return false;
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(Remito.class)){
			Remito remito = (Remito)destino;
			// Ahora se configura en la entidad
			// para garantizar que la moneda sea siempre la configurada para la facturación			
			// remito.setMoneda(remito.buscarMonedaDefault());
			remito.setOrdenPreparacion(this);
			if (this.getPorConsignacion()){
				DepositoPorConsignacionDefaultCalculator calculator = new DepositoPorConsignacionDefaultCalculator();
				calculator.setConsignacion(this.getPorConsignacion());
				try{
					remito.setDepositoPorConsignacion((Deposito)calculator.calculate());
				}
				catch(Exception e){					
				}
			}
			else if (this.transferenciaEntreSucursales()){
				SolicitudMercaderia solicitud = this.solicitud();
				if (solicitud != null){
					remito.setDepositoPorConsignacion(solicitud.getOrigen());					
				}
			}
			if (!items.isEmpty()){
				EstadisticaPedidoVenta itemPedidoVenta = ((ItemPendienteRemitoProxy)items.get(0)).getItemOrdenPreparacion().getItemPedidoVenta(); 
				if (itemPedidoVenta != null){
					PedidoVenta pedido = itemPedidoVenta.getVenta();
					remito.setDomicilioEntrega(pedido.getDomicilioEntrega());
				}
				else{
					remito.setDomicilioEntrega(this.getCliente().domicilioEntregaPrincipal());
				}
			}
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(Remito.class)){
			Remito remito = (Remito)destino;
			remito.setItems(new LinkedList<ItemRemito>());
			
			for(IItemPendiente itemPendiente: items){
				ItemOrdenPreparacion itemorigen = (ItemOrdenPreparacion)itemPendiente.getItem();
				ItemRemito itemdestino = new ItemRemito();
				itemdestino.copiarPropiedades(itemorigen);
				itemdestino.setItemOrdenPreparacion(itemorigen);
				itemdestino.setRemito(remito);
				itemdestino.recalcular();
				remito.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);	
			}
		}
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Orden de Preparación";
	}
	
	@Override
	protected void inicializar(){
		super.inicializar();
		if (this.getDeposito() == null){
			ICalculator defaultCalculator = new DepositoDefaultCalculator();
			try {
				this.setDeposito((Deposito)defaultCalculator.calculate());
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getCliente() != null){
			parameters.put("CLIENTE_CODIGO", this.getCliente().getCodigo());
			parameters.put("CLIENTE_NOMBRE", this.getCliente().getNombre());
			parameters.put("CLIENTE_POSICIONIVA", this.getCliente().getPosicionIva().getDescripcion());
			parameters.put("CLIENTE_CUIT", this.getCliente().getNumeroDocumento());
		}
		else{
			parameters.put("CLIENTE_CODIGO", "");
			parameters.put("CLIENTE_NOMBRE", "");
			parameters.put("CLIENTE_POSICIONIVA", "");
			parameters.put("CLIENTE_CUIT", "");
		}
				
		PedidoVenta pedido = null;
		if ((this.getItems() != null) && (!this.getItems().isEmpty())){
			for(ItemOrdenPreparacion itemOrden: this.getItems()){
				if (itemOrden.getItemPedidoVenta() != null){
					pedido = itemOrden.getItemPedidoVenta().getVenta();
					break;
				}
			}
		}
				
		if (pedido!= null){
			parameters.put("DIRECCION", pedido.getDomicilioEntrega().getDireccion());
			parameters.put("CODIGOPOSTAL", pedido.getDomicilioEntrega().getCiudad().getCodigoPostal());
			parameters.put("CIUDAD", pedido.getDomicilioEntrega().getCiudad().getCiudad());
			parameters.put("PROVINCIA", pedido.getDomicilioEntrega().getCiudad().getProvincia().getProvincia());			
		}
		else{
			parameters.put("DIRECCION", "");
			parameters.put("CODIGOPOSTAL", "");
			parameters.put("PROVINCIA", "");
			parameters.put("CIUDAD", "");
		}
		
		MedioTransporte medioTransporte = null;
		Zona zona = null;
		if ((pedido != null) && (this.getCliente() != null)){
			LugarEntregaMercaderia entrega = this.getCliente().lugarEntrega(pedido.getDomicilioEntrega());
			if (entrega != null){
				medioTransporte = entrega.getMedioTransporte();
				zona = entrega.getZona();
			}
		}
		
		if (zona != null){
			parameters.put("ZONA", zona.getNombre());
		}
		else{
			parameters.put("ZONA", "");
		}
		
		if (medioTransporte != null){
			parameters.put("MEDIOTRANSPORTE_NOMBRE", medioTransporte.getNombre());
			parameters.put("MEDIOTRANSPORTE_CUIT", medioTransporte.getCuit());
			parameters.put("MEDIOTRANSPORTE_DIRECCION", medioTransporte.getDomicilio().getDireccion());
			parameters.put("MEDIOTRANSPORTE_CIUDAD", medioTransporte.getDomicilio().getCiudad().getCiudad());
		}
		else{
			parameters.put("MEDIOTRANSPORTE_NOMBRE", "");
			parameters.put("MEDIOTRANSPORTE_CUIT", "");
			parameters.put("MEDIOTRANSPORTE_DIRECCION", "");
			parameters.put("MEDIOTRANSPORTE_CIUDAD", "");
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		this.getRemitosGenerados(trs);		
	}
	
	@Hidden
	private void getRemitosGenerados(Collection<Transaccion> trs){		
		if (this.cerrado()) {
			String sqlText = "select distinct remito_id from " + Esquema.concatenarEsquema("ItemRemito") + " i " + 
					"join " + Esquema.concatenarEsquema("ItemOrdenPreparacion") + " iop on iop.id = i.itemOrdenPreparacion_id " +
					"where iop.ordenPreparacion_id = '" + this.getId() + "'";
			Query query = XPersistence.getManager().createNativeQuery(sqlText);			
			@SuppressWarnings("unchecked")
			List<String> list = query.getResultList();
			Iterator<String> it = list.iterator();
			while (it.hasNext()){
				String id = (String) it.next();
				Remito remito = (Remito) XPersistence.getManager().find(Remito.class, id);
				trs.add(remito);
			}			
		}
	}

	public PedidoVenta getPedido() {
		return pedido;
	}

	public void setPedido(PedidoVenta pedido) {
		this.pedido = pedido;
	}
	
	@Override
	public void recalcularTotales(){
		ConfiguracionCircuito circuitoPedido = ConfiguracionCircuito.buscarCircuito(PedidoVenta.class.getSimpleName(), OrdenPreparacion.class.getSimpleName());
		Boolean permiteMasUnidadesPedido = Boolean.FALSE;
		if (circuitoPedido != null){
			permiteMasUnidadesPedido = circuitoPedido.getPermiteSuperarCantidad();
		}
				
		for(ItemOrdenPreparacion item: this.getItems()){
			if (!permiteMasUnidadesPedido){
				if (item.getAcepta() && (item.getItemPedidoVenta() != null)){
					throw new ValidationException("No esta permitido activar en los items de preparación ACEPTAR unidades que exceden lo pedido");
				}
			}
			item.recalcular();
		}
	}
	
	@Override
	public void copiarPropiedades(Object objeto){
		super.copiarPropiedades(objeto);
		
		this.setItems(null);
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getCliente() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		else{
			return null;
		}
	}

	public String getIdCreadaPor() {
		return idCreadaPor;
	}

	public void setIdCreadaPor(String idCreadaPor) {
		this.idCreadaPor = idCreadaPor;
	}

	public String getTipoEntidadCreadaPor() {
		return tipoEntidadCreadaPor;
	}

	public void setTipoEntidadCreadaPor(String tipoEntidadCreadaPor) {
		this.tipoEntidadCreadaPor = tipoEntidadCreadaPor;
	}
	
	public void asignarCreadaPor(Transaccion origen){
		this.setIdCreadaPor(origen.getId());
		this.setTipoEntidadCreadaPor(origen.getClass().getSimpleName());
	}
	
	public boolean transferenciaEntreSucursales(){
		return Is.equalAsString(this.getTipoEntidadCreadaPor(), SolicitudMercaderia.class.getSimpleName());
	}
	
	private SolicitudMercaderia solicitud(){
		if (this.transferenciaEntreSucursales()){
			return (SolicitudMercaderia)XPersistence.getManager().find(SolicitudMercaderia.class, this.getIdCreadaPor());
		}
		else{
			return null;
		}
	}

	@Override
	public void prepararParaCancelarPendiente() {
		for(ItemOrdenPreparacion item: this.getItems()){
			item.setNoPreparar(item.getCantidad());
			item.setCantidad(BigDecimal.ZERO);
		}		
	}

}
