package org.openxava.mercadolibre.model;

import java.math.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.lang.Boolean;
import javax.persistence.*;
import org.Mercadolibre.*;
import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.impuestos.model.PosicionAnteImpuesto;
import org.openxava.inventario.model.Deposito;
import org.openxava.inventario.model.IItemMovimientoInventario;
import org.openxava.inventario.model.ITransaccionInventario;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.TipoDocumento;
import org.openxava.tesoreria.model.Tesoreria;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

import com.allin.interfacesafip.model.AfipPosicionIVA;

@Entity

@Views({
	@View(members=
	 	"fecha, fechaHora, numero, tipoEcommerce;" +
		"empresa, estado, fechaCreacion, usuario;" + 
		"idComprador, nombreComprador;" +
		"nombre, apellido, email;" + 
		"tipoDocumento, numeroDocumento, formaPago, responsableInscripto;" +
		"costoEnvio, montoDescuento;" + 
		"items;" + 
		"factura;" + 
		"trazabilidad; "  
	),
	@View(name="ItemPedidoML", members=
		"fecha, fechaHora, numero;" +
		"empresa, estado, fechaCreacion, usuario;" + 
		"idComprador, nombreComprador, costoEnvio, montoDescuento;" + 
		"nombre, apellido, tipoDocumento, numeroDocumento;" +
		"trazabilidad; "  
	),
	@View(name="Simple", members="numero"),
	@View(name="CambioTipoDocumento", members="tipoDocumento"),
	@View(name="CambioNumeroDocumento", members="numeroDocumento"),
	@View(name="CambioResponsableInscripto", members="responsableInscripto")

})


@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="fecha, fechaHora, numero, estado, idComprador, nombreComprador, factura.subestado.nombre, factura.numero",
	defaultOrder="${fechaCreacion} desc")

public class PedidoML extends Transaccion implements ITransaccionInventario{
	
	
	public static List<?> notificacionesPendientes(){
		String sql = "select id, applications_id from " + Esquema.concatenarEsquema("NotificacionML") + " where processed = :false " + 
				"and resource ilike '%" + NotificacionML.ORDERS + "%' " +
				"order by date_sent asc, applications_id";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("false", false);
		List<?> results = query.getResultList();
		return results;
	}
	
	public static PedidoML existePedido(long idMercadoLibre){
		Query query = XPersistence.getManager().createQuery("from PedidoML where numero = :idMercadoLibre");
		query.setMaxResults(1);
		query.setParameter("idMercadoLibre", String.valueOf(idMercadoLibre));
		List<?> result = query.getResultList();
		if (result.isEmpty()){
			return null;
		}
		else{
			return (PedidoML)result.get(0);
		}
	}
		
	@Override
	public String descripcionTipoTransaccion() {
		return "Venta Ecommerce";
	}

	@Required
	@ReadOnly
	private Ecommerce tipoEcommerce;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaHora;
	
	@Column(length=25)
	@ReadOnly
	private String idComprador;
	
	@Column(length=50)
	@ReadOnly
	private String nombreComprador;
	
	@Column(length=50)
	@ReadOnly
	private String email;
	
	@Column(length=50)
	@ReadOnly
	private String nombre;
	
	@Column(length=50)
	@ReadOnly
	private String apellido;
	
	@Column(length=20)
	@ReadOnly(notForViews="CambioNumeroDocumento")
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioNumeroDocumento")
	private String numeroDocumento;
	
	@Column(length=5)
	@ReadOnly(notForViews="CambioTipoDocumento")
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioTipoDocumento")
	private String tipoDocumento;
	
	@ReadOnly
	@Column(length=25)
	private String formaPago;
		
	@OneToMany(mappedBy="pedido", cascade=CascadeType.ALL) 
	@ReadOnly
	@ListProperties("publicacion.idMercadoLibre, producto.codigo, cantidad, precioUnitario, porcentajeDescuento, total")
	private Collection<ItemPedidoML> items;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("SimpleSubEstado")
	@ReadOnly
	private FacturaVentaContado factura;
	
	@ReadOnly(notForViews="CambioResponsableInscripto")
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioResponsableInscripto")
	private Boolean responsableInscripto = false;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify 
	@DescriptionsList(descriptionProperties="codigo")
	private ConfiguracionMercadoLibre configuracionEcommerce;
	
	@Hidden
	@ReadOnly
	private BigDecimal porcentajeDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal costoEnvio = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal montoDescuento = BigDecimal.ZERO;

	public BigDecimal getMontoDescuento() {
		return montoDescuento == null ? BigDecimal.ZERO: montoDescuento;
	}

	public void setMontoDescuento(BigDecimal montoDescuento) {
		this.montoDescuento = montoDescuento;
	}

	public BigDecimal getCostoEnvio() {
		return costoEnvio == null ? BigDecimal.ZERO: costoEnvio;
	}

	public void setCostoEnvio(BigDecimal costoEnvio) {
		this.costoEnvio = costoEnvio;
	}

	public ConfiguracionMercadoLibre getConfiguracionEcommerce() {
		return configuracionEcommerce;
	}

	public void setConfiguracionEcommerce(ConfiguracionMercadoLibre configuracionEcommerce) {
		this.configuracionEcommerce = configuracionEcommerce;
	}

	public Date getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(Date fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getIdComprador() {
		return idComprador;
	}

	public void setIdComprador(String idComprador) {
		this.idComprador = idComprador;
	}

	public String getNombreComprador() {
		return nombreComprador;
	}

	public void setNombreComprador(String nombreComprador) {
		this.nombreComprador = nombreComprador;
	}

	public Collection<ItemPedidoML> getItems() {
		return items;
	}

	public void setItems(Collection<ItemPedidoML> items) {
		this.items = items;
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		int maxLong = 50;
		if (!Is.emptyString(nombre)){
			if (nombre.length() > maxLong){
				this.nombre = nombre.substring(0, maxLong);
			}
			else{
				this.nombre = nombre;
			}
		}
		else{
			this.nombre = nombre;
		}		
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		int maxLong = 50;
		if (!Is.emptyString(apellido)){
			if (apellido.length() > maxLong){
				this.apellido = apellido.substring(0, maxLong);
			}
			else{
				this.apellido = apellido;
			}
		}
		else{
			this.apellido = apellido;
		}		
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		int maxLong = 50;
		if (!Is.emptyString(email)){
			if (email.length() > maxLong){
				this.email = email.substring(0, maxLong);
			}
			else{
				this.email = email;
			}
		}
		else{
			this.email = email;
		}		
	}

	@Override
	public Boolean soloLectura(){
		return true;
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> lista = new ArrayList<IItemMovimientoInventario>();
		for(ItemPedidoML item: this.getItems()){
			if (item.getProducto().getTipo().equals(TipoProducto.Producto)){
				lista.add(item);
			}			
		}
		return lista;
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}

	@Override
	public EmpresaExterna empresaExternaInventario() {
		return null;
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Deposito deposito;

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public FacturaVentaContado getFactura() {
		return factura;
	}

	public void setFactura(FacturaVentaContado factura) {
		this.factura = factura;
	}
	
	public FacturaVentaContado facturar() {
		if (this.getEstado().equals(Estado.Confirmada)){
			FacturaVentaContado facturaContado = this.getFactura();
			if (facturaContado != null){
				if (facturaContado.getEstado().equals(Estado.Confirmada)){
					throw new ValidationException("Pedido " + this.toString() + " ya fue facturado");
				}
				else if (facturaContado.getEstado().equals(Estado.Cancelada) || facturaContado.getEstado().equals(Estado.Anulada)){
					facturaContado = this.crearFacturaContado();
				}
			}
			else{
				facturaContado = this.crearFacturaContado();
			}		
			return facturaContado;
		}
		else{
			throw new ValidationException("primero_confirmar");
		}
	}
	
	private FacturaVentaContado crearFacturaContado(){

		FacturaVentaContado factura = new FacturaVentaContado();
		ConfiguracionMercadoLibre configurador = this.getConfiguracionEcommerce();
		
		MediosPagoEcommerce medioPago = configurador.buscarMedioPago(this.getFormaPago());
		Tesoreria tesoreriaCobranza = configurador.getCuentaBancaria();

		if (medioPago != null){
			tesoreriaCobranza = medioPago.getTesoreria();
			factura.setPorcentajeDescuento(medioPago.getPorcentajeDescuento());			
		}
		if (!tesoreriaCobranza.getSucursal().equals(configurador.getSucursal())){
			throw new ValidationException("La sucursal de la caja/cuenta " + tesoreriaCobranza.getNombre() + " no es " + configurador.getSucursal().getNombre());
		}
		factura.setEmpresa(tesoreriaCobranza.getEmpresa());
		factura.setSucursal(configurador.getSucursal());
		factura.setPuntoVenta(configurador.getPuntoFacturacion());
		factura.asignarCreadoPor(this);
		factura.setListaPrecio(configurador.getListaPrecio());
		factura.asignarConsumidorFinal();

		
		StringBuffer razonSocial = new StringBuffer();
		if (!Is.emptyString(this.getNombre())){
			razonSocial.append(this.getNombre());
		}
		if (!Is.emptyString(this.getApellido())){
			if (razonSocial.length() > 0) razonSocial.append(" ");
			razonSocial.append(this.getApellido());
		}
		if (razonSocial.length() > 0){
			factura.setRazonSocial(razonSocial.toString());
		}
		
		if (!Is.emptyString(this.getNumeroDocumento())){
			factura.setCuit(this.getNumeroDocumento());
		}
		if  (!Is.emptyString(this.getTipoDocumento())){
			factura.setTipoDocumento(TipoDocumento.tipoDocumentoPorCodigoMercadoLibre(this.getTipoDocumento()));
		}
		if (this.getResponsableInscripto()){
			factura.setPosicionIva(PosicionAnteImpuesto.buscarPorCodigo(AfipPosicionIVA.RESPONSABLEINSCRIPTO.getCodigoPosicionIVA()));
		}
		
		if (!Is.emptyString(this.getEmail())){
			factura.setEmail(this.getEmail());
		}

		XPersistence.getManager().persist(factura);
		
		List<ItemVentaElectronica> itemsFactura = new LinkedList<ItemVentaElectronica>();
		for(ItemPedidoML item: this.getItems()){
			ItemVentaElectronica itemFactura = new ItemVentaElectronica();
			itemFactura.setVenta(factura);
			itemFactura.setProducto(item.getProducto());
			itemFactura.setCantidad(item.getCantidad());
			itemFactura.calcularPrecioUnitarioSegunImporteTotal(item.getTotal());
			itemFactura.setDespacho(item.getDespacho());
			itemFactura.setLote(item.getLote());
			if(this.getPorcentajeDescuento() != null && this.getPorcentajeDescuento().compareTo(BigDecimal.ZERO) > 0){
				itemFactura.setPorcentajeDescuento(this.getPorcentajeDescuento());
			}
			itemFactura.recalcular();
			XPersistence.getManager().persist(itemFactura);
			itemsFactura.add(itemFactura);		
		}		
		
		if(this.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0 && this.getConfiguracionEcommerce().getCostoEnvio() != null){
			itemsFactura.add(crearItemExtra(factura, this.getConfiguracionEcommerce().getCostoEnvio(), this.getCostoEnvio()));		
		}
		
		if(this.getMontoDescuento().compareTo(BigDecimal.ZERO) > 0 && this.getConfiguracionEcommerce().getMontoDescuento() != null){
			itemsFactura.add(crearItemExtra(factura, this.getConfiguracionEcommerce().getMontoDescuento(), this.getMontoDescuento().negate()));		
		}
				
		factura.setItems(itemsFactura);
		factura.grabarTransaccion();		
		this.setFactura(factura);
		
		return factura;
	}

	private ItemVentaElectronica crearItemExtra(FacturaVentaContado factura, Producto concepto, BigDecimal monto) {
		ItemVentaElectronica itemFactura = new ItemVentaElectronica();
		itemFactura.setVenta(factura);
		itemFactura.setProducto(concepto);
		itemFactura.setCantidad(BigDecimal.ONE);
		itemFactura.calcularPrecioUnitarioSegunImporteTotal(monto);
		itemFactura.recalcular();
		XPersistence.getManager().persist(itemFactura);
		return itemFactura;
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		if (this.getFactura() != null){
			trs.add(this.getFactura());
		}
	}
	
	public Ecommerce getTipoEcommerce() {
		return tipoEcommerce;
	}

	public void setTipoEcommerce(Ecommerce tipoEcommerce) {
		this.tipoEcommerce = tipoEcommerce;
	}

	public Boolean getResponsableInscripto() {
		return responsableInscripto;
	}

	public void setResponsableInscripto(Boolean responsableInscripto) {
		this.responsableInscripto = responsableInscripto;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		if (formaPago != null){
			int max = 25;
			if (formaPago.length() > max){
				this.formaPago = formaPago.substring(0, max - 1);
			}
			else{
				this.formaPago = formaPago;
			}
		}
		else{
			this.formaPago = null;
		}
	}

	public static PedidoML crearPedidoDesdeMercadoLibre(MLPedido pedidoMercadoLibre, ConfiguracionMercadoLibre configuracionEcommerce) {
		
		PedidoML pedido = new PedidoML();
		pedido.setTipoEcommerce(Ecommerce.MercadoLibre);
		pedido.setSucursal(configuracionEcommerce.getSucursal());
		pedido.setNumeroInterno(pedidoMercadoLibre.getNumeroDePedido());
		pedido.setNumero(Long.toString(pedidoMercadoLibre.getNumeroDePedido()));
		pedido.setFecha(pedidoMercadoLibre.getFecha());
		pedido.setFechaHora(pedidoMercadoLibre.getFechaCierre());		
		pedido.setIdComprador(pedidoMercadoLibre.getIdUsuarioComprador());
		pedido.setNombreComprador(pedidoMercadoLibre.getNombreUsuarioCompra());
		pedido.setNombre(pedidoMercadoLibre.getNombreComprador());
		pedido.setApellido(pedidoMercadoLibre.getApellidoComprador());
		pedido.setNumeroDocumento(pedidoMercadoLibre.getNumeroDocumentoComprador());
		pedido.setTipoDocumento(pedidoMercadoLibre.getTipoDocumentoComprador());
		pedido.setDeposito(configuracionEcommerce.getStockMercadoLibre());
		pedido.setConfiguracionEcommerce(configuracionEcommerce);	
		
		try{
			pedido.setEmpresa(configuracionEcommerce.getCuentaBancaria().getEmpresa());
		}catch(Exception e){
		}
		
		XPersistence.getManager().persist(pedido);
	    
		List<ItemPedidoML> items = new LinkedList<ItemPedidoML>();
		for(MLItemPedido itemMercadoLibre: pedidoMercadoLibre.getItems()){
			ItemPedidoML itemPedido = new ItemPedidoML();
			itemPedido.setPedido(pedido);
			itemPedido.setNumeroPedidoCarrito(itemMercadoLibre.getPackOrder());
			try{
				itemPedido.setPublicacion(PublicacionML.buscarActiva(itemMercadoLibre, Ecommerce.MercadoLibre));
			}catch(ValidationException e){
				if(itemMercadoLibre.getIdVariante() != null){
					PublicacionML publicacion = PublicacionML.buscarSinVariante(itemMercadoLibre, Ecommerce.MercadoLibre);
					if(publicacion != null){
						/*
						 * Cuando las publicacion tiene una sola variante se asigna la variacion, para que pueda instanciar el pedido.
						 * Si ya existe, el usuario debe intervenir
						 */
						publicacion.setIdProducto(itemMercadoLibre.getIdVariante());
						itemPedido.setPublicacion(publicacion);
					}else
						throw new ValidationException("No existe la publicacion de id " + itemMercadoLibre.getIdItem() 
									+ " y id de variante: " + itemMercadoLibre.getIdVariante());
				}else{
					throw e;
				}
			}
			
			itemPedido.setCantidad(itemMercadoLibre.getCantidad());
			itemPedido.setPrecioUnitario(new BigDecimal(itemMercadoLibre.getPrecio()));
			itemPedido.setPrecioOriginal(itemPedido.getPrecioUnitario());
			itemPedido.recalcular();
			items.add(itemPedido);
			XPersistence.getManager().persist(itemPedido);
		}

		if(pedidoMercadoLibre.getShipping_cost().compareTo(BigDecimal.ZERO) > 0){
			pedido.setCostoEnvio(pedidoMercadoLibre.getShipping_cost());
		}
		
		if(pedidoMercadoLibre.getCoupon_amount().compareTo(BigDecimal.ZERO) > 0){
			pedido.setMontoDescuento(pedidoMercadoLibre.getCoupon_amount());
		}
		
		pedido.setItems(items);	
		pedido.abrirTransaccion();
		return pedido;
	}

	@Hidden
	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null ? BigDecimal.ZERO : this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal descuento) {
		this.porcentajeDescuento = descuento;
	}


}
