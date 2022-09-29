package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +			
			"empresa, destino;" +
			"cliente;" +			
			"observaciones];" +
	"Items{items}Trazabilidad{trazabilidad};"		
	),	
	@View(name="Simple", members="numero, estado;"),	
})

@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, empresa.nombre, numero, estado, empresa.nombre, cliente.nombre, destino.nombre, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")


public class IngresoPorDevolucion extends Transaccion implements ITransaccionInventario, IImportadorItemCSV{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito destino;
	
	@OneToMany(mappedBy="devolucion", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, despacho.codigo")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsTransaccionProductos.Multiseleccion")
	private Collection<ItemIngresoPorDevolucion> items;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Deposito getDestino() {
		return destino;
	}

	public void setDestino(Deposito destino) {
		this.destino = destino;
	}

	public Collection<ItemIngresoPorDevolucion> getItems() {
		return items;
	}

	public void setItems(Collection<ItemIngresoPorDevolucion> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Devolución";
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
	
	// WorkFlow Credito Venta o Crédito interno venta
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(CreditoVenta.class);
		tipoTrsDestino.add(CreditoInternoVenta.class);
	}

	@Override
	public Class<?> getTipoPendiente(Class<?> tipoTransaccionDestino){
		if (CreditoInternoVenta.class.equals(tipoTransaccionDestino)){
			// se utiliza el mismo pendiente que el de credito venta.
			return PendienteCreditoVenta.class;
		}
		else{
			return super.getTipoPendiente(tipoTransaccionDestino);
		}
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){		
		if (CreditoVenta.class.equals(tipoTrDestino)){
			return this.getEmpresa().getInscriptoIva();			
		}
		else if (CreditoInternoVenta.class.equals(tipoTrDestino)){
			return !this.getEmpresa().getInscriptoIva();
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	
	@Override
	protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		if ((destino.getClass().equals(CreditoVenta.class)) || (destino.getClass().equals(CreditoInternoVenta.class))){			
			VentaElectronica credito = (VentaElectronica) destino;
			credito.setPorcentajeDescuento(this.getCliente().getPorcentajeDescuento());
			credito.asignarCreadoPor(this);
			
			// se configura en la entidad
			// credito.setMoneda(credito.buscarMonedaDefault());
			credito.setDomicilioEntrega(this.getCliente().getDomicilio());
			
			PuntoVentaDefaultCalculator calculator = new PuntoVentaDefaultCalculator();
			calculator.setCliente(this.getCliente());
			calculator.setSucursal(this.getDestino().getSucursal());
			try{
				credito.setPuntoVenta((PuntoVenta)calculator.calculate());
			}
			catch(Exception e){				
			}
			
			if (destino.getClass().equals(CreditoVenta.class)){
				credito.setPorcentajeFinanciero(this.getCliente().getPorcentajeFinanciero());
			}
		}
	}
	
	@Override
	protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(CreditoVenta.class) || (destino.getClass().equals(CreditoInternoVenta.class))){
			VentaElectronica credito = (VentaElectronica)destino;
			credito.setItems(new LinkedList<ItemVentaElectronica>());
			for(ItemIngresoPorDevolucion item: this.getItems()){				
				ItemVentaElectronica itemCredito = new ItemVentaElectronica();				
				itemCredito.copiarPropiedades(item);
				itemCredito.setVenta(credito);
				credito.getItems().add(itemCredito);			
				itemCredito.recalcular();
				XPersistence.getManager().persist(itemCredito);
			}
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		Query query = XPersistence.getManager().createQuery("from CreditoVenta where idCreadaPor = :id");
		query.setParameter("id", this.getId());		
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			for(Object obj: result){
				trs.add((Transaccion)obj);
			}
		}
		
		query = XPersistence.getManager().createQuery("from CreditoInternoVenta where idCreadaPor = :id");
		query.setParameter("id", this.getId());		
		result = query.getResultList();
		if (!result.isEmpty()){
			for(Object obj: result){
				trs.add((Transaccion)obj);
			}
		} 
	}

	
	@Transient
	private Map<String, ItemIngresoPorDevolucion> itemsPorProductoCSV = null; 
	
	private Map<String, ItemIngresoPorDevolucion> getItemsPorProductoCSV() {
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemIngresoPorDevolucion>();
			for(ItemIngresoPorDevolucion item: this.getItems()){
				String clave = this.claveCSV(item.getProducto(), item.getDespacho());
				if (!itemsPorProductoCSV.containsKey(clave)){
					itemsPorProductoCSV.put(clave, item);
				}
			}
		}
		return this.itemsPorProductoCSV;
	}

	private String claveCSV(Producto producto, DespachoImportacion despacho){
		String clave = "";
		if (producto != null){
			clave = producto.getCodigo();
		}
		if (despacho != null){
			clave += despacho.getCodigo();
		}
		return clave;
	}
	
	@Override
	public void iniciarImportacionCSV() {		
	}

	@Override
	public ItemTransaccion crearItemDesdeCSV(String[] values) {
		Integer cantidadCampos = 3;
		ItemIngresoPorDevolucion item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			String codigoDespacho = null;
			if (values.length >= 4){
				codigoDespacho = values[3];
			}
			
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			DespachoImportacion despacho = null;
			if (!Is.emptyString(codigoDespacho)){
				despacho = DespachoImportacion.buscar(codigoDespacho);
				if (despacho == null){
					throw new ValidationException("No existe el despacho de código " + codigoDespacho);
				}
			}
			else{
				despacho = producto.ultimoDespachoGeneral();
			}
			
			String clave = this.claveCSV(producto, despacho);
			if (this.getItemsPorProductoCSV().containsKey(clave)){
				item = this.getItemsPorProductoCSV().get(clave);
			}
			else{
				item = new ItemIngresoPorDevolucion();
				item.setDevolucion(this);
				item.setProducto(producto);
				item.setDespacho(despacho);
				this.getItemsPorProductoCSV().put(clave, item);
				this.getItems().add(item);
			}			
			item.setCantidad(cantidad);			
			return item;
		}
		else{
			throw new ValidationException("Faltan campos: como mínimo deben ser " + cantidadCampos.toString());
		}	
	}

	@Override
	public void finalizarImportacionCSV() {
		if (this.itemsPorProductoCSV != null){
			this.itemsPorProductoCSV.clear();
			this.itemsPorProductoCSV = null;
		}
				
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_CLIENTE", this.getCliente().getNombre());
		parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
		parameters.put("CUIT_CLIENTE", this.getCliente().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_CLIENTE", this.getCliente().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_CLIENTE", this.getCliente().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_CLIENTE", this.getCliente().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCodigoPostal().toString());
		parameters.put("CIUDAD_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCiudad());
		parameters.put("PROVINCIA_CLIENTE", this.getCliente().getDomicilio().getCiudad().getProvincia().getProvincia());
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Producto producto = (Producto)MapFacade.findEntity("Producto", key);
			this.crearItemAjusteInventario(producto);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar producto: " + error);
		}
		
	}
	
	private void crearItemAjusteInventario(Producto producto){
		ItemIngresoPorDevolucion item = new ItemIngresoPorDevolucion();
		item.setDevolucion(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		if (producto.getDespacho()){
			item.setDespacho(producto.ultimoDespacho(this.getDestino().getId()));
			if (item.getDespacho() == null){
				item.setDespacho(producto.ultimoDespachoGeneral());
			}
		}
		this.getItems().add(item);
		item.recalcular();
		XPersistence.getManager().persist(item);
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getCliente() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		else{
			return null;
		}
	}	
}
