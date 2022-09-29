package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.distribucion.model.ZonaReparto;
import org.openxava.inventario.calculators.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.actions.*;
import org.openxava.ventas.filter.*;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.MapCloud;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"fecha, fechaVencimiento, fechaCreacion;" +
			"estado, subestado, ultimaTransicion;" + 
			"empresa, numero, deposito;" +
			"moneda, cotizacion;" + 
			"cliente;" +
			"vendedor;" +			
			"listaPrecio, condicionVenta, porcentajeDescuento, zonaReparto;" + 
			"domicilioEntrega;" +			
			"observaciones];" +
	"Consignacion[porConsignacion];" +  		
	"productos;" +
	"items;" +
	"total, subtotalSinDescuento, descuento, subtotal, iva;"
	),
	@View(name="SoloLectura", members=
	"Principal[#" + 
			"fecha, fechaVencimiento, fechaCreacion;" +
			"estado, subestado, ultimaTransicion;" + 
			"empresa, numero, deposito, pendientePreparacion;" +
			"moneda, cotizacion;" + 
			"cliente;" +
			"vendedor;" +			
			"listaPrecio, condicionVenta, porcentajeDescuento, zonaReparto;" + 
			"domicilioEntrega;" + 			
			"observaciones;];" + 
	"Consignacion[porConsignacion];" + 		
	"items{items}" +
	"ordenes{ordenes}" +
	"historico{historicoEstados};" +
	"trazabilidad{trazabilidad};" +
	"total, subtotalSinDescuento, descuento, subtotal, iva;"  		
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=VentasFilter.class,
		properties="fecha, empresa.nombre, numero, estado, subestado.nombre, cliente.nombre, vendedor.nombre, total, subtotal, iva, subtotalSinDescuento, descuento, fechaVencimiento, fechaCreacion, usuario",
		baseCondition="(true = ? or ${vendedor.id} = ?) and " + EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class PedidoVenta extends Venta implements IImportadorItemCSV, IDestinoEMail{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@DefaultValueCalculator(DepositoDefaultCalculator.class)
	@NoCreate @NoModify
	private Deposito deposito;
			
	@ManyToMany
	@NewAction("PedidoVenta.add")
	//@SaveAction("ProductosEnPedidoVenta.add")
	@Collapsed
	@SearchListCondition(value="${ventas} = 't'")
	private Collection<Producto> productos;
			
	@OneToMany(mappedBy="venta", cascade=CascadeType.ALL) 
	@ListProperties("producto.codigo, producto.nombre, cantidad, precioUnitario, descuento, suma, descuentoGlobal, subtotal, tasaiva, pendientePreparacion")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	private Collection<EstadisticaPedidoVenta> items;
	
	@DefaultValueCalculator(value=ConsignacionVentaCalculator.class, 
					properties={@PropertyValue(from="cliente.id", name="idCliente")})
	private Boolean porConsignacion = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ZonaReparto zonaReparto;
		
	public Deposito getDeposito() {
		return deposito;
	}
	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}
	public Collection<Producto> getProductos() {
		return productos;
	}
	public void setProductos(Collection<Producto> productos) {
		this.productos = productos;
	}
		
	public Collection<EstadisticaPedidoVenta> getItems() {
		return items;
	}
	
	public void setItems(Collection<EstadisticaPedidoVenta> items) {
		this.items = items;
	}
	
	public Boolean getPorConsignacion() {
		return porConsignacion;
	}
	public void setPorConsignacion(Boolean porConsignacion) {
		this.porConsignacion = porConsignacion;
	}
	@Override
	public boolean generaCuentaCorriente(){
		return false;
	}
	
	@Override
	public Collection<EstadisticaItemVenta> ItemsVenta() {
		if (this.getItems() == null){
			return new LinkedList<EstadisticaItemVenta>();
		}
		else{
			List<EstadisticaItemVenta> list = new LinkedList<EstadisticaItemVenta>();
			list.addAll(this.getItems());
			return list;
		}
	}
	
	@Override
	public boolean calcularImpuestos() {
		Boolean calcula = Boolean.TRUE;
		if (this.getEmpresa() != null){
			calcula = this.getEmpresa().getInscriptoIva();
			if (calcula){
				calcula = !this.getEmpresa().esMonotributista();
			}
		}
		if (calcula){
			if (this.getCliente() != null){
				calcula = this.getCliente().getRegimenFacturacion().calculaImpuestos();
			}
		}
		return calcula;
	}
	
	@Override
	public String viewName(){
		if (this.soloLectura()){
			return "SoloLectura";
		}
		else{
			return super.viewName();
		}
	}
	
	@org.hibernate.annotations.Formula("(select not p.cumplido from PendienteOrdenPreparacion p where p.idtrorigen = id)")
	private Boolean pendientePreparacion;
	
	public Boolean getPendientePreparacion() {
		return pendientePreparacion;
	}
	
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(OrdenPreparacion.class);
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
		if ((FacturaVenta.class.equals(tipoTrDestino)) || (FacturaManual.class.equals(tipoTrDestino))){
			if (this.pideSoloConceptos()){
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
			else{
				return false;
			}
		}
		else if (OrdenPreparacion.class.equals(tipoTrDestino)){
			return !this.pideSoloConceptos();
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	private boolean pideSoloConceptos(){
		boolean soloConceptos = false;
		if (!this.getItems().isEmpty()){
			soloConceptos = true;
			for(EstadisticaPedidoVenta item: this.getItems()){
				if (item.getProducto().getTipo().equals(TipoProducto.Producto)){
					soloConceptos = false;
					break;
				}
			}
		}
		
		return soloConceptos;
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(OrdenPreparacion.class)){
			// Ahora se configura en la entidad
			//destino.setMoneda(this.buscarMonedaDefault());
			
			// Se busca el primer pedido de la lista y se asigna a la orden de preparación
			// Esto sirve para todos los que procesan de a un pedido.
			// Si generan una única orden desde muchos pedidos, en este caso la trazabilida con el pedido es a nivel de item
			for(IItemPendiente itemPendiente: items){
				EstadisticaPedidoVenta item = (EstadisticaPedidoVenta)itemPendiente.getItem();
				if (item.getPendientePreparacion().compareTo(BigDecimal.ZERO) > 0){
					((OrdenPreparacion)destino).setPedido(item.getVenta());
					break;
				}
			}
		}
		if (destino.getClass().equals(FacturaVenta.class)){
			FacturaVenta factura = (FacturaVenta)destino;
			// se configura en la entidad
			// para garantizar que la moneda sea siempre la configurada para la facturación
			//factura.setMoneda(factura.buscarMonedaDefault());
			factura.asignarCreadoPor(this);
			Cliente cliente = factura.getCliente();
			BigDecimal descuentoFinanciero = cliente.buscarDescuentoFinanciero();
			factura.setPorcentajeDescuento(this.getPorcentajeDescuento());
			factura.setPorcentajeFinanciero(descuentoFinanciero);
		}
		else if (destino.getClass().equals(FacturaManual.class)){
			FacturaManual factura = (FacturaManual)destino;
			factura.setMoneda(factura.buscarMonedaDefault());
			factura.asignarCreadoPor(this);
			factura.setPorcentajeDescuento(this.getPorcentajeDescuento());			
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(OrdenPreparacion.class)){
			OrdenPreparacion orden = (OrdenPreparacion)destino;
			orden.setItems(new LinkedList<ItemOrdenPreparacion>());
			
			for(IItemPendiente itemPendiente: items){
				EstadisticaPedidoVenta item = (EstadisticaPedidoVenta)itemPendiente.getItem();
				if (item.getPendientePreparacion().compareTo(BigDecimal.ZERO) > 0){
					ItemOrdenPreparacion itemOrdenPreparacion = new ItemOrdenPreparacion();
					itemOrdenPreparacion.copiarPropiedades(item);
					itemOrdenPreparacion.setItemPedidoVenta(item);
					itemOrdenPreparacion.setPedido(item.getVenta());
					itemOrdenPreparacion.setOrdenPreparacion(orden);
					itemOrdenPreparacion.setCantidad(item.getPendientePreparacion());
					orden.getItems().add(itemOrdenPreparacion);
					XPersistence.getManager().persist(itemOrdenPreparacion);
				}
			}
		}
		else if ( destino.getClass().equals(FacturaVenta.class) || destino.getClass().equals(FacturaManual.class) ){
			VentaElectronica factura = (VentaElectronica)destino;
			factura.setItems(new LinkedList<ItemVentaElectronica>());
			
			for(IItemPendiente itemPendiente: items){
				EstadisticaPedidoVenta itemorigen = (EstadisticaPedidoVenta)itemPendiente.getItem();
				ItemVentaElectronica itemdestino = new ItemVentaElectronica();
				itemdestino.setVenta(factura);
				itemdestino.setItemPedido(itemorigen);
				itemdestino.setProducto(itemorigen.getProducto());
				itemdestino.setUnidadMedida(((ItemPendienteFacturaVentaPorCantidadProxy)itemPendiente).getUnidadMedida());
				itemdestino.setCantidad(((ItemPendienteFacturaVentaPorCantidadProxy)itemPendiente).getCantidadPendiente());
				itemdestino.setPorcentajeDescuento(itemorigen.getPorcentajeDescuento());								
				itemdestino.setPrecioUnitario(Transaccion.convertirMoneda(this, factura, itemorigen.getPrecioUnitario()));
				itemdestino.setDetalle(itemorigen.getDetalle());
				itemdestino.setCentroCostos(itemorigen.getCentroCostos());
				itemdestino.recalcular();
				factura.getItems().add(itemdestino);
				XPersistence.getManager().persist(itemdestino);					
			}
		}
	}
	
	@ReadOnly
	@ListProperties("fecha, numero, estado, observaciones, usuario, fechaCreacion")
	public Collection<OrdenPreparacion> getOrdenes(){
		if (this.getEstado().equals(Estado.Confirmada)) {
			Collection<OrdenPreparacion> ordenes = new ArrayList<OrdenPreparacion>();
			String sqlText = "select distinct ordenpreparacion_id from " + Esquema.concatenarEsquema("ItemOrdenPreparacion") + " i " + 
					"join " + Esquema.concatenarEsquema("EstadisticaPedidoVenta") + " v on v.id = i.itempedidoventa_id " +
					"where v.venta_id = '" + this.getId() + "'";
			Query query = XPersistence.getManager().createNativeQuery(sqlText);
			@SuppressWarnings("unchecked")
			List<String> list = query.getResultList();
			Iterator<String> it = list.iterator();
			while (it.hasNext()){
				String id = (String) it.next();
				OrdenPreparacion orden = (OrdenPreparacion) XPersistence.getManager().find(OrdenPreparacion.class, id);
				ordenes.add(orden);
			}
			return ordenes;
		}
		else{
			return Collections.emptyList();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Collection<VentaElectronica> facturasGeneradas(){
		if (this.getEstado().equals(Estado.Confirmada)){
			Collection<VentaElectronica> facturas = new LinkedList<VentaElectronica>();
			
			String sql = "from FacturaVenta where idCreadaPor = :id";
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("id", this.getId());
			facturas.addAll(query.getResultList());
			
			sql = "from FacturaManual where idCreadaPor = :id";
			query = XPersistence.getManager().createQuery(sql);
			query.setParameter("id", this.getId());
			facturas.addAll(query.getResultList());
			
			return facturas;
		}
		else{
			return Collections.emptyList();
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Collection<OrdenPreparacion> ordenes = getOrdenes();
		if (!ordenes.isEmpty()){
			trs.addAll(ordenes);
		}
		
		Collection<VentaElectronica> facturas = facturasGeneradas();
		if (!facturas.isEmpty()){
			trs.addAll(facturas);
		}
	}
	@Override
	public String descripcionTipoTransaccion() {
		return "Pedido de Venta";
	}
	
	@Override
	protected EstadoEntidad establecerEstadoDestino(TransicionEstado transicion){
		EstadoEntidad destino = null;
				
		if (destino == null){
			EstadoEntidad estadoCumpleSituacionCrediticia = null;
			if ((transicion.getCondicion1() != null) && (transicion.getCondicion1().equals(CondicionTransicionar.SituacionCrediticia))){
				estadoCumpleSituacionCrediticia = transicion.getDestino1();
			}
			else if ((transicion.getCondicion2() != null) && (transicion.getCondicion2().equals(CondicionTransicionar.SituacionCrediticia))){
				estadoCumpleSituacionCrediticia = transicion.getDestino2();
			}
			
			if (estadoCumpleSituacionCrediticia != null){
				// se evalua la situacion crediticia del cliente
				BigDecimal situacionCrediticia = this.getCliente().getCalculos().getSituacionCrediticia();
				BigDecimal limiteCredito = this.getCliente().getLimiteCredito();
				
				if (this.getTotal().add(situacionCrediticia).compareTo(limiteCredito) < 0){
					destino = estadoCumpleSituacionCrediticia;
				}
			}
		}
		
		if (destino == null){
			destino = transicion.getDestino();
		}
		return destino;
	}
	
	public EstadisticaPedidoVenta crearItemPedido(Producto producto){
		EstadisticaPedidoVenta nuevo = new EstadisticaPedidoVenta();
		nuevo.setVenta(this);
		nuevo.setProducto(producto);
		nuevo.setUnidadMedida(producto.getUnidadMedida());
		nuevo.setTasaiva(producto.getTasaIva().getPorcentaje());
		nuevo.setCantidad(new BigDecimal(1));
		nuevo.recalcular();
		this.getItems().add(nuevo);
		XPersistence.getManager().persist(nuevo);
		
		return nuevo;
	}
	
	@Transient
	private Map<String, EstadisticaPedidoVenta> itemsPorProductoCSV = null; 
	
	private Map<String, EstadisticaPedidoVenta> getItemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, EstadisticaPedidoVenta>();
			for(EstadisticaPedidoVenta item: this.getItems()){
				if (!itemsPorProductoCSV.containsKey(item.getProducto().getCodigo())){
					itemsPorProductoCSV.put(item.getProducto().getCodigo(), item);
				}
			}
		}
		return this.itemsPorProductoCSV;
	}
	
	@Override
	public ItemTransaccion crearItemDesdeCSV(String[] values) {
		Integer cantidadCampos = 3;
		EstadisticaPedidoVenta item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);			
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			if (this.getItemsPorProducto().containsKey(codigoProducto)){
				item = this.getItemsPorProducto().get(codigoProducto);
			}
			else{
				item = new EstadisticaPedidoVenta();
				item.setVenta(this);
				item.setProducto(producto);
				this.getItemsPorProducto().put(codigoProducto, item);
				this.getItems().add(item);
			}			
			item.setCantidad(cantidad);
			
			return item;
		}
		else{
			throw new ValidationException("Faltan campos: deben ser " + cantidadCampos.toString());
		}	
	}

	@Override
	public void iniciarImportacionCSV() {
	}

	@Override
	public void finalizarImportacionCSV() {
		this.getItemsPorProducto().clear();
		this.itemsPorProductoCSV = null;
	}

	@Override
	public String emailPara() {
		if (this.getCliente() != null){
			return this.getCliente().getMail1();
		}
		else{
			return null;
		}
	}
	@Override
	public String emailCC() {
		if (this.getCliente() != null){
			return this.getCliente().getMail2();
		}
		else{
			return null;
		}
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getDeposito() != null){
			parameters.put("CODIGO_DEPOSITO", this.getDeposito().getCodigo());
			parameters.put("NOMBRE_DEPOSITO", this.getDeposito().getNombre());
			parameters.put("ID_DEPOSITO", this.getDeposito().getId());
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getCliente() != null){
			if (this.getCliente().getSinIdentificacion()){
				errores.add("No se puede utilizar el cliente sin identificación");
			}
		}
	}
	public ZonaReparto getZonaReparto() {
		return zonaReparto;
	}
	public void setZonaReparto(ZonaReparto zonaReparto) {
		this.zonaReparto = zonaReparto;
	}

	public AddressCloud addressMapCloud(MapCloud map){
		AddressCloud address = super.addressMapCloud(map);
		if (address != null){
			this.addressRefresh(address);
		}
		return address;
	}
	
	@Override
	public void addressRefresh(AddressCloud address) {
		String label = "N° " + this.getNumero() + " - " + this.getCliente().getNombre();
		if (this.getZonaReparto() != null){
			address.setAsignado(true);
			address.setLabel(this.getZonaReparto().getCodigo() + " " + label);
		}
		else{
			address.setAsignado(false);
			address.setLabel(label);
		}		
	}
	
}
