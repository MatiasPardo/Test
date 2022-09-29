package org.openxava.inventario.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa;" + 
			"numero, tipoComprobante];"  
	),
	@View(name="PedidoVenta", members=
		"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
		"pedidoVenta;" +
		"itemsPedido;" 		
	),
	@View(name="SolicitudMercaderia", members=
		"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa;" + 
			"numero, tipoComprobante];" +
		"cliente;" + 		
		"deposito;" + 	
		"itemsSolicitud;")
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, numero, cliente.codigo, cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteOrdenPreparacion extends Pendiente{

	public PendienteOrdenPreparacion(){
		// siempre generar un constructor default si se genera un constructor por parámetros
	}
	
	public PendienteOrdenPreparacion(Transaccion origen){
		super(origen);
		if (origen instanceof PedidoVenta){
			this.setPedidoVenta((PedidoVenta)origen);
			this.setDeposito(this.getPedidoVenta().getDeposito());
			this.setCliente(((PedidoVenta)origen).getCliente());
		}
		else if (origen instanceof SolicitudMercaderia){
			this.setDeposito(((SolicitudMercaderia)origen).getSolicitaA());
			this.setCliente(((SolicitudMercaderia)origen).getSucursal().clienteAsociado());			
		}
		this.setSucursal(this.getDeposito().getSucursal());		
	}
	
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		if (this.getPedidoVenta() != null){
			for(EstadisticaPedidoVenta item: this.getPedidoVenta().getItems()){
				item.setPendientePreparacion(item.getCantidad());
			}
		}
		else if (Is.equalAsString(this.getTipoTrOrigen(), SolicitudMercaderia.class.getSimpleName())){
			SolicitudMercaderia solicitud = (SolicitudMercaderia)origen;
			for(ItemSolicitudMercaderia item: solicitud.getItems()){
				item.setPendientePreparacion(item.getCantidad());
			}
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private PedidoVenta pedidoVenta;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Deposito deposito;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Simple")
	@ReadOnly
	private Cliente cliente;
	
	@Hidden
	@DisplaySize(value=15)
	public String getTipoComprobante(){
		return this.origen().getDescripcion();
	}
	
	public PedidoVenta getPedidoVenta() {
		return pedidoVenta;
	}

	public void setPedidoVenta(PedidoVenta pedidoVenta) {
		this.pedidoVenta = pedidoVenta;
	}

	@ReadOnly
	@ListProperties("producto.codigo, producto.nombre, pendientePreparacion")
	@CollectionView("PendienteOrdenPreparacion")
	public Collection<EstadisticaPedidoVenta> getItemsPedido(){
		if (!Is.emptyString(this.getId()) && (this.getPedidoVenta() != null)){
			Query query = XPersistence.getManager().createQuery("from EstadisticaPedidoVenta e where e.venta.id = :venta");
			query.setParameter("venta", this.getPedidoVenta().getId());
			try{
				@SuppressWarnings("unchecked")
				List<EstadisticaPedidoVenta> resultado = (List<EstadisticaPedidoVenta>)query.getResultList();
				Collection<EstadisticaPedidoVenta> items = new ArrayList<EstadisticaPedidoVenta>();
				items.addAll(resultado);
				return items;
			}
			catch(Exception e){
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
	}
	
	@Transient
	private Transaccion trOriginoPendiente = null;
	
	@Override
	public Transaccion origen() {
		if (this.getPedidoVenta() != null){
			return getPedidoVenta();
		}
		else{
			if (trOriginoPendiente == null){
				trOriginoPendiente = this.buscarOrigen();
			}
			return trOriginoPendiente;
			
		}
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen){
		return OrdenPreparacion.class.getSimpleName();
	}
	
	@Override
	public Transaccion crearTransaccionDestino(){
		return new OrdenPreparacion();
	}

	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean procesaJunto = false;
		PendienteOrdenPreparacion pendOP = (PendienteOrdenPreparacion)pendiente;
		if ((pendOP.getPedidoVenta() != null) && (this.getPedidoVenta() != null)){
			if (pendOP.getPedidoVenta().getCliente().equals(this.getPedidoVenta().getCliente())){
				if (pendOP.getPedidoVenta().getDomicilioEntrega().equals(this.getPedidoVenta().getDomicilioEntrega())){
					if (pendOP.getPedidoVenta().getPorConsignacion().equals(this.getPedidoVenta().getPorConsignacion())){
						if (Is.equal(pendOP.getPedidoVenta().getVendedor(), this.getPedidoVenta().getVendedor())){						
							if (Is.equal(pendOP.getPedidoVenta().getDeposito(), this.getPedidoVenta().getDeposito())){						
								procesaJunto = super.permiteProcesarJunto(pendiente);
							}
						}
					}
				}
			}
		}
		else if ((pendOP.origen() instanceof FacturaVentaContado) && (this.origen() instanceof FacturaVentaContado)){
			procesaJunto = super.permiteProcesarJunto(pendiente);
		}
		return procesaJunto;
	}
	
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		if (this.getPedidoVenta() != null){
			for(EstadisticaPedidoVenta item: this.getPedidoVenta().getItems()){
				items.add(item.itemPendienteOrdenPreparacionProxy());
			}
		}
		else if (Is.equalAsString(this.getTipoTrOrigen(), SolicitudMercaderia.class.getSimpleName())){
			SolicitudMercaderia solicitud = (SolicitudMercaderia)this.origen();
			for(ItemSolicitudMercaderia item: solicitud.getItems()){
				items.add(item.itemPendienteOrdenPreparacionProxy());
			}
		}
	}
	
	@Override
	public String viewName(){
		if (this.getPedidoVenta() != null){
			return "PedidoVenta";
		}
		else if (Is.equalAsString(this.getTipoTrOrigen(), SolicitudMercaderia.class.getSimpleName())){
			return "SolicitudMercaderia";
		}
		else{
			return null;
		}
	}
	
	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@ReadOnly
	@ListProperties("producto.codigo, producto.nombre, pendientePreparacion")
	public Collection<ItemSolicitudMercaderia> getItemsSolicitud(){
		if (!Is.emptyString(this.getId()) && (Is.equalAsString(this.getTipoTrOrigen(), SolicitudMercaderia.class.getSimpleName()))){
			Query query = XPersistence.getManager().createQuery("from ItemSolicitudMercaderia i where i.solicitud.id = :origen");
			query.setParameter("origen", this.getIdTrOrigen());
			try{
				@SuppressWarnings("unchecked")
				List<ItemSolicitudMercaderia> resultado = (List<ItemSolicitudMercaderia>)query.getResultList();
				Collection<ItemSolicitudMercaderia> items = new ArrayList<ItemSolicitudMercaderia>();
				items.addAll(resultado);
				return items;
			}
			catch(Exception e){
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
	}
	
	
}
