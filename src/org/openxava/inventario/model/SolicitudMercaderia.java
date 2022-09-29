package org.openxava.inventario.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Transient;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.EditAction;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NewAction;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.RemoveAction;
import org.openxava.annotations.RemoveSelectedAction;
import org.openxava.annotations.SaveAction;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.IImportadorItemCSV;
import org.openxava.base.model.IItemPendiente;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.ObjetoEstatico;
import org.openxava.base.model.ProcesadorCSV;
import org.openxava.base.model.Transaccion;
import org.openxava.inventario.calculators.DepositoDefaultCalculator;
import org.openxava.inventario.calculators.DepositoSucursalDestinoPrincipalCalculator;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +
			"empresa;" + 
			"origen;" +
			"solicitaA;" + 
			"observaciones;" + 
		"];" +
		"Items{items} Trazabilidad{trazabilidad};"		
	),
	@View(name="Simple", members="numero, estado;"),	
})

@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="fecha, numero, estado, origen.nombre, solicitaA.nombre, fechaCreacion, usuario",
	defaultOrder="${fechaCreacion} desc"
)

public class SolicitudMercaderia extends Transaccion implements IImportadorItemCSV{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@DefaultValueCalculator(DepositoDefaultCalculator.class)
	@NoCreate @NoModify
	private Deposito origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(DepositoSucursalDestinoPrincipalCalculator.class)
	private Deposito solicitaA;
	
	@OneToMany(mappedBy="solicitud", cascade=CascadeType.ALL)
	@ListProperties(value="producto.codigo, producto.nombre, cantidad, detalle")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	private Collection<ItemSolicitudMercaderia> items;
	
	public Deposito getOrigen() {
		return origen;
	}

	public void setOrigen(Deposito origen) {
		this.origen = origen;
	}

	public Deposito getSolicitaA() {
		return solicitaA;
	}

	public void setSolicitaA(Deposito solicitaA) {
		this.solicitaA = solicitaA;
	}

	public Collection<ItemSolicitudMercaderia> getItems() {
		return items;
	}

	public void setItems(Collection<ItemSolicitudMercaderia> items) {
		this.items = items;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Solicitud Mercadería";
	}
	
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(OrdenPreparacion.class);
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(OrdenPreparacion.class)){
			destino.setMoneda(this.buscarMonedaDefault());			
			OrdenPreparacion orden = (OrdenPreparacion)destino;
			if (orden.getSucursal() != null){
				orden.setCliente(orden.getSucursal().clienteAsociado());
			}
			else{
				throw new ValidationException("Sucursal no asignada en la orden de preparación");
			}
			orden.setPorConsignacion(Boolean.FALSE);
			orden.setDeposito(this.getSolicitaA());			
			orden.asignarCreadaPor(this);
		}		
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(OrdenPreparacion.class)){
			OrdenPreparacion orden = (OrdenPreparacion)destino;
			orden.setItems(new LinkedList<ItemOrdenPreparacion>());
			
			for(IItemPendiente itemPendiente: items){
				ItemSolicitudMercaderia item = (ItemSolicitudMercaderia)itemPendiente.getItem();
				if (item.getPendientePreparacion().compareTo(BigDecimal.ZERO) > 0){					
					this.crearItemOrdenPreparacion(orden, item, item.getPendientePreparacion());
				}
			}
		}
	}
	
	private void crearItemOrdenPreparacion(OrdenPreparacion orden, ItemSolicitudMercaderia item, BigDecimal cantidad){
		ItemOrdenPreparacion itemOrdenPreparacion = new ItemOrdenPreparacion();
		itemOrdenPreparacion.copiarPropiedades(item);
		itemOrdenPreparacion.setItemSolicitud(item);
		itemOrdenPreparacion.setOrdenPreparacion(orden);
		itemOrdenPreparacion.setCantidad(cantidad);
		itemOrdenPreparacion.setNoPreparar(BigDecimal.ZERO);		
		orden.getItems().add(itemOrdenPreparacion);
		XPersistence.getManager().persist(itemOrdenPreparacion);
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Query query = XPersistence.getManager().createQuery("from OrdenPreparacion where idCreadaPor = :id");
		query.setParameter("id", this.getId());
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			for(Object obj: result){
				trs.add((Transaccion)obj);
			}
		}
	}

	@Transient
	private Map<String, ItemSolicitudMercaderia> itemsPorProductoCSV = null; 
	
	private Map<String, ItemSolicitudMercaderia> itemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemSolicitudMercaderia>();
			for(ItemSolicitudMercaderia item: this.getItems()){
				if (!itemsPorProductoCSV.containsKey(item.getProducto().getCodigo())){
					itemsPorProductoCSV.put(item.getProducto().getCodigo(), item);
				}
			}
		}
		return this.itemsPorProductoCSV;
	}
	
	@Override
	public void iniciarImportacionCSV() {
	}

	@Override
	public ItemTransaccion crearItemDesdeCSV(String[] values) {
		Integer cantidadCampos = 3;
		ItemSolicitudMercaderia item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			if (this.itemsPorProducto().containsKey(codigoProducto)){
				item = this.itemsPorProducto().get(codigoProducto);
			}
			else{				
				item = this.crearItemSolicitud(producto);
				this.itemsPorProducto().put(codigoProducto, item);				
			}			
			item.setCantidad(cantidad);			
			return item;
		}
		else{
			throw new ValidationException("Faltan campos: deben ser " + cantidadCampos.toString());
		}	
	}

	@Override
	public void finalizarImportacionCSV() {
		this.itemsPorProductoCSV = null;		
	}
	
	@Override
	protected void asignarSucursal(){
		super.asignarSucursal();
		if (this.getOrigen() != null){
			this.setSucursal(this.getOrigen().getSucursal());
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getOrigen().equals(this.getSolicitaA())){
			errores.add("No puede coincidir los depósitos");
		}
		else if (this.getOrigen().getSucursal().equals(this.getSolicitaA().getSucursal())){
			errores.add("No puede coincidir las sucursales de los depósitos");
		}
		
		if (this.getItems() != null){
			Map<String, Object> productos = new HashMap<String, Object>();
			for(ItemSolicitudMercaderia item: this.getItems()){
				if (productos.containsKey(item.getProducto().getId())){
					errores.add("Producto repetido: " + item.getProducto().getCodigo());
				}
				else{
					productos.put(item.getProducto().getId(), null);
				}
			}
		}
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);		
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		if (itemsMultiseleccion.isEmpty()){
			// Se registran los items ya creados para no volver agregarlos
			for(ItemSolicitudMercaderia itemSolicitud: this.getItems()){
				if (!itemsMultiseleccion.containsKey(itemSolicitud.getProducto().getId())){
					itemsMultiseleccion.put(itemSolicitud.getProducto().getId(), null);
				}
			}
		}
		
		try {
			Producto producto = (Producto)MapFacade.findEntity("Producto", key);
			if (!itemsMultiseleccion.containsKey(producto.getId())){
				ItemSolicitudMercaderia item = this.crearItemSolicitud(producto);
				XPersistence.getManager().persist(item);
				return true;
			}
			else{
				return false;
			}
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar producto: " + error);
		}
		
	}
	
	private ItemSolicitudMercaderia crearItemSolicitud(Producto producto){
		ItemSolicitudMercaderia item = new ItemSolicitudMercaderia();
		item.setSolicitud(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		item.recalcular();
		this.getItems().add(item);
		return item;
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
	}

}
