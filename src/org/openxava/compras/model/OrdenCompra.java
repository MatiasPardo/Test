package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.UnidadMedida;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;


@Entity

@Views({
	@View(members=
		"Principal{" +
			"Principal[#" + 
					"fecha, fechaCreacion, usuario;" +
					"estado, subestado, ultimaTransicion;" + 
					"empresa, numero, moneda;" +
					"fechaRecepcion, fechaRecepcionPorItem, participaStock;" + 
					"proveedor;" + 
					"listaPrecio, condicionCompra;" +			
					"observaciones];" +			
			"items;" +
			"subtotalSinDescuento, descuento;" +		
			"subtotal, iva, total;}" +
		"Trazabilidad{trazabilidad}" +
		"Auditoria{registroModificaciones}"	
	),
	@View(name="CambioFechaRecepcion", members="fechaRecepcion"),
	@View(name="CambioParticipaStock", members="participaStock"),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, numero, estado, subestado.nombre, proveedor.nombre, total, subtotal, iva, fechaCreacion, usuario",
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class OrdenCompra extends Compra implements IImportadorItemCSV, IDestinoEMail{
	
	@Required
	@DefaultValueCalculator(CurrentDateCalculator.class)
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioFechaRecepcion")
	private Date fechaRecepcion;
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean fechaRecepcionPorItem;
	
	@DefaultValueCalculator(FalseCalculator.class)
	@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioParticipaStock")
	private Boolean participaStock;
	
	@OneToMany(mappedBy="ordenCompra", cascade=CascadeType.ALL) 
	@ListProperties("producto.codigo, producto.nombre, cantidad, pendienteRecepcion, precioUnitario, porcentajeDescuento, descuento, suma, tasaiva, noEntregado")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	@ListAction("ColeccionItemsTransaccionProductos.Multiseleccion")
	private Collection<ItemOrdenCompra> items;
	
	public Collection<ItemOrdenCompra> getItems() {
		return items;
	}

	public void setItems(Collection<ItemOrdenCompra> items) {
		this.items = items;
	}

	@Override
	public Collection<ItemCompra> ItemsCompra() {
		if (this.getItems() == null){
			return new LinkedList<ItemCompra>();
		}
		else{
			List<ItemCompra> list = new LinkedList<ItemCompra>();
			list.addAll(this.getItems());
			return list;
		}
	}

	public Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
		
		// Cuando de modifica después de confirmar la transacción
		if (this.estaCambiandoAtributo()){			
			if (!this.getFechaRecepcionPorItem()){
				for(ItemOrdenCompra item: this.getItems()){
					item.setFechaRecepcion(fechaRecepcion);
				}
			}
		}
	}
	
	public Boolean getFechaRecepcionPorItem() {
		return fechaRecepcionPorItem == null ? Boolean.FALSE : this.fechaRecepcionPorItem;
	}

	public void setFechaRecepcionPorItem(Boolean fechaRecepcionPorItem) {
		if (fechaRecepcionPorItem == null){
			this.fechaRecepcionPorItem = Boolean.FALSE;
		}
		else{
			this.fechaRecepcionPorItem = fechaRecepcionPorItem;
		}
	}	


	public Boolean getParticipaStock() {
		return participaStock;
	}

	public void setParticipaStock(Boolean participaStock) {
		this.participaStock = participaStock;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Orden de Compra";
	}
	
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(RecepcionMercaderia.class);
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(RecepcionMercaderia.class)){
			
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(RecepcionMercaderia.class)){
			RecepcionMercaderia recepcion = (RecepcionMercaderia)destino;
			recepcion.setItems(new LinkedList<ItemRecepcionMercaderia>());
			
			for(IItemPendiente itemPendiente: items){
				ItemOrdenCompra item = (ItemOrdenCompra)itemPendiente.getItem();
				if (item.getPendienteRecepcion().compareTo(BigDecimal.ZERO) > 0){
					ItemRecepcionMercaderia itemDestino = new ItemRecepcionMercaderia();
					itemDestino.copiarPropiedades(item);
					itemDestino.setItemOrdenCompra(item);
					itemDestino.setRecepcionMercaderia(recepcion);
					itemDestino.setCantidad(item.getPendienteRecepcion());
					recepcion.getItems().add(itemDestino);
					XPersistence.getManager().persist(itemDestino);
				}
			}
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Collection<RecepcionMercaderia> recepciones = new LinkedList<RecepcionMercaderia>();		
		this.recepcionesGeneradas(recepciones);
		trs.addAll(recepciones);
	}
	
	public void recepcionesGeneradas(Collection<RecepcionMercaderia> list){
		if (this.cerrado()){
			String sql = "select distinct i.recepcionmercaderia_id from " + Esquema.concatenarEsquema("itemordencompra") + " ic join " + Esquema.concatenarEsquema("itemrecepcionmercaderia") + " i on ic.id = i.itemordencompra_id " +
						"where ic.ordencompra_id = :id";
			Query query = XPersistence.getManager().createNativeQuery(sql);
			query.setParameter("id", this.getId());
			@SuppressWarnings("unchecked")
			List<String> ids = query.getResultList();
			Iterator<String> it = ids.iterator();
			while (it.hasNext()){
				String id = (String) it.next();
				list.add((RecepcionMercaderia)XPersistence.getManager().find(RecepcionMercaderia.class, id));				
			}			
		}
	}

	
	@Transient
	private Map<String, ItemOrdenCompra> itemsPorProductoCSV = null; 
	
	private Map<String, ItemOrdenCompra> getItemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemOrdenCompra>();
			for(ItemOrdenCompra item: this.getItems()){
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
		ItemOrdenCompra item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			BigDecimal precio = null;
			if ((values.length > 3) && (!Is.emptyString(values[3]))){
				precio = ProcesadorCSV.convertirTextoANumero(values[3]);
			}
			BigDecimal porcentajeDescuento = BigDecimal.ZERO;
			if ((values.length > 4) && (!Is.emptyString(values[4]))){
				porcentajeDescuento = ProcesadorCSV.convertirTextoANumero(values[4]);
			}
			
			String codigoUnidadMedida = null;
			if ((values.length > 5)){
				codigoUnidadMedida = values[5];
			}
			
			Date fechaRecepcion = null;
			if (values.length > 6){
				fechaRecepcion = ProcesadorCSV.convertirTextoAFecha(values[6], null);
				if (fechaRecepcion != null && !this.getFechaRecepcionPorItem()){
					if (!Is.equal(UtilERP.trucarDateTime(fechaRecepcion), this.getFechaRecepcion())){
						throw new ValidationException("La orden de compra no acepta fecha de recepción por items y se esta intentando asignar una fecha de recepción. Debe quedar vacía o tener la misma fecha de recepción que el comprobante");
					}
				}
			}
			
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			if (this.getItemsPorProducto().containsKey(codigoProducto)){
				item = this.getItemsPorProducto().get(codigoProducto);
			}
			else{
				item = new ItemOrdenCompra();
				item.setOrdenCompra(this);
				item.setProducto(producto);
				this.getItemsPorProducto().put(codigoProducto, item);
				this.getItems().add(item);
			}	
			
			if (!Is.emptyString(codigoUnidadMedida)){
				item.setUnidadMedida((UnidadMedida)UnidadMedida.buscarPorCodigoError(codigoUnidadMedida, UnidadMedida.class.getSimpleName()));
			}
			item.setCantidad(cantidad);
			if (precio != null){
				item.setPrecioUnitario(precio);
			}	
			item.setPorcentajeDescuento(porcentajeDescuento);
			item.setFechaRecepcion(fechaRecepcion);	
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
		String email = null;
		if (this.getProveedor() != null){
			email = this.getProveedor().getMail1();			
		}
		return email;
	}

	@Override
	public String emailCC() {
		String email = null;
		if (this.getProveedor()!= null){
			email = this.getProveedor().getMail2();
		}
		return email;
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Producto producto = (Producto)MapFacade.findEntity("Producto", key);
			this.crearItemOrdenCompra(producto);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar producto: " + error);
		}
		
	}
	
	private void crearItemOrdenCompra(Producto producto){
		ItemOrdenCompra item = new ItemOrdenCompra();
		item.setOrdenCompra(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		this.getItems().add(item);
		item.recalcular();
		XPersistence.getManager().persist(item);
	}
}
