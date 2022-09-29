package org.openxava.inventario.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EditAction;
import org.openxava.annotations.ListAction;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NewAction;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RemoveAction;
import org.openxava.annotations.RemoveSelectedAction;
import org.openxava.annotations.SaveAction;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.base.model.Pendiente;
import org.openxava.base.model.Transaccion;
import org.openxava.compras.model.CompraElectronica;
import org.openxava.compras.model.CreditoCompra;
import org.openxava.compras.model.CreditoInternoCompra;
import org.openxava.compras.model.ItemCompraElectronica;
import org.openxava.compras.model.PendienteCreditoCompra;
import org.openxava.compras.model.Proveedor;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +			
			"empresa, origen;" +
			"proveedor;" +			
			"observaciones];" +
	"Items{items}Trazabilidad{trazabilidad};"		
	),	
	@View(name="Simple", members="numero, estado;"),	
})

@Tab(
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		properties="fecha, empresa.nombre, numero, estado, empresa.nombre, proveedor.nombre, origen.nombre, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class EgresoPorDevolucion extends Transaccion implements ITransaccionInventario{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Proveedor proveedor;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito origen;
	
	@OneToMany(mappedBy="devolucion", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, despacho.codigo")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsTransaccionProductos.Multiseleccion")
	private Collection<ItemEgresoPorDevolucion> items;
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public Deposito getOrigen() {
		return origen;
	}

	public void setOrigen(Deposito origen) {
		this.origen = origen;
	}

	public Collection<ItemEgresoPorDevolucion> getItems() {
		return items;
	}

	public void setItems(Collection<ItemEgresoPorDevolucion> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Devolución al Proveedor";
	}
	
	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getItems());
		return movimientos;
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}

	@Override
	public EmpresaExterna empresaExternaInventario() {
		if (this.getProveedor() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getProveedor().getId());
		}
		else{
			return null;
		}
	}
	
	// WorkFlow Credito Compra o Crédito interno Compra
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(CreditoCompra.class);
		tipoTrsDestino.add(CreditoInternoCompra.class);
	}

	@Override
	public Class<?> getTipoPendiente(Class<?> tipoTransaccionDestino){
		if (CreditoInternoCompra.class.equals(tipoTransaccionDestino)){
			// se utiliza el mismo pendiente que el de credito venta.
			return PendienteCreditoCompra.class;
		}
		else{
			return super.getTipoPendiente(tipoTransaccionDestino);
		}
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){		
		if (CreditoCompra.class.equals(tipoTrDestino)){
			return this.getEmpresa().getInscriptoIva();			
		}
		else if (CreditoInternoCompra.class.equals(tipoTrDestino)){
			return !this.getEmpresa().getInscriptoIva();
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		if ((destino.getClass().equals(CreditoCompra.class)) || (destino.getClass().equals(CreditoInternoCompra.class))){			
			CompraElectronica credito = (CompraElectronica) destino;
			credito.asignarCreadoPor(this);			
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(CreditoCompra.class) || (destino.getClass().equals(CreditoInternoCompra.class))){
			CompraElectronica credito = (CompraElectronica)destino;			
			credito.setItems(new LinkedList<ItemCompraElectronica>());
			for(ItemEgresoPorDevolucion item: this.getItems()){				
				ItemCompraElectronica itemCredito = new ItemCompraElectronica();				
				itemCredito.copiarPropiedades(item);				
				itemCredito.setCompra(credito);
				credito.getItems().add(itemCredito);			
				itemCredito.recalcular();
				XPersistence.getManager().persist(itemCredito);
			}
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Query query = XPersistence.getManager().createQuery("from CreditoCompra where idCreadaPor = :id");
		query.setParameter("id", this.getId());		
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			for(Object obj: result){
				trs.add((Transaccion)obj);
			}
		}
		
		query = XPersistence.getManager().createQuery("from CreditoInternoCompra where idCreadaPor = :id");
		query.setParameter("id", this.getId());		
		result = query.getResultList();
		if (!result.isEmpty()){
			for(Object obj: result){
				trs.add((Transaccion)obj);
			}
		} 
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_PROVEEDOR", this.getProveedor().getNombre());
		parameters.put("CODIGO_PROVEEDOR", this.getProveedor().getCodigo());
		parameters.put("CUIT_PROVEEDOR", this.getProveedor().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_PROVEEDOR", this.getProveedor().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_PROVEEDOR", this.getProveedor().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_PROVEEDOR", this.getProveedor().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getCodigoPostal().toString());
		parameters.put("CIUDAD_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getCiudad());
		parameters.put("PROVINCIA_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getProvincia().getProvincia());
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Producto producto = (Producto)MapFacade.findEntity("Producto", key);
			this.crearItem(producto);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar producto: " + error);
		}
		
	}
	
	private void crearItem(Producto producto){
		ItemEgresoPorDevolucion item = new ItemEgresoPorDevolucion();
		item.setDevolucion(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		if (producto.getDespacho()){
			item.setDespacho(producto.ultimoDespacho(this.getOrigen().getId()));
			if (item.getDespacho() == null){
				item.setDespacho(producto.ultimoDespachoGeneral());
			}
		}
		this.getItems().add(item);
		item.recalcular();
		XPersistence.getManager().persist(item);
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getOrigen() != null){
			this.setSucursal(this.getOrigen().getSucursal());
		}
	}
}
