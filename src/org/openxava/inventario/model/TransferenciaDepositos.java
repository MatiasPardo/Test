package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.filter.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion, estado, usuario;" + 			
			"numero, fecha, fechaCreacion;" +
			"origen, destino, motivo;" +
			"observaciones];" +
	"Items{items};"		
	),	
	@View(name="Simple", members="numero, estado;"),	
})

@Tab(
		filter=TransferenciaDepositosFilter.class,
		baseCondition=TransferenciaDepositosFilter.BASECONDITION_TRANSFERENCIA,
		properties="fecha, numero, estado, origen.nombre, destino.nombre, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class TransferenciaDepositos extends Transaccion implements ITransaccionInventario, IImportadorItemCSV{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito destino;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private MotivoAjusteInventario motivo;
	
	@OneToMany(mappedBy="transferencia", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, despacho.codigo")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsTransfereciaDeposito.Multiseleccion")
	private Collection<ItemTransferenciaDepositos> items;
	
	public Deposito getOrigen() {
		return origen;
	}

	public void setOrigen(Deposito origen) {
		this.origen = origen;
	}

	public Deposito getDestino() {
		return destino;
	}

	public void setDestino(Deposito destino) {
		this.destino = destino;
	}

	public MotivoAjusteInventario getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivoAjusteInventario motivo) {
		this.motivo = motivo;
	}

	public Collection<ItemTransferenciaDepositos> getItems() {
		return items;
	}

	public void setItems(Collection<ItemTransferenciaDepositos> items) {
		this.items = items;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Transferencia";
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		
		for(IItemMovimientoInventario item: this.getItems()){
			movimientos.add(item);
			
			ItemMovInvIngresoProxy itemIngreso = new ItemMovInvIngresoProxy(item);
			itemIngreso.setDeposito(this.getDestino());
			movimientos.add(itemIngreso);
		}
		
		return movimientos;
		
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getOrigen() != null){
			parameters.put("DEPOSITOORIGEN_NOMBRE", this.getOrigen().getNombre());
		}
		else{
			parameters.put("DEPOSITOORIGEN_NOMBRE", "");
		}
		if (this.getDestino() != null){
			parameters.put("DEPOSITODESTINO_NOMBRE", this.getDestino().getNombre());
		}
		else{
			parameters.put("DEPOSITODESTINO_NOMBRE", "");
		}
		if(this.getMotivo() != null){
			parameters.put("MOTIVO", this.getMotivo().getNombre());
		}
		else{
			parameters.put("MOTIVO", "");
		}
	}

	
	@Transient
	private Map<String, ItemTransferenciaDepositos> itemsPorProductoCSV = null; 
	
	private String obtenerClaveCSV(ItemTransferenciaDepositos item){
		if (item.getProducto() != null){
			String clave = item.getProducto().getCodigo();
			if (item.getDespacho() != null){
				clave += item.getDespacho().getCodigo();
			}
			return clave;
		}
		else{
			return null;
		}
	}
	
	private Map<String, ItemTransferenciaDepositos> getItemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemTransferenciaDepositos>();
			for(ItemTransferenciaDepositos item: this.getItems()){
				String clave = this.obtenerClaveCSV(item);
				if (!itemsPorProductoCSV.containsKey(clave)){
					itemsPorProductoCSV.put(clave, item);
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
		Integer cantidadCampos = 4;
		ItemTransferenciaDepositos item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			String codigoDespacho = values[3];
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			DespachoImportacion despacho = null;
			if (!Is.emptyString(codigoDespacho)){
				despacho = DespachoImportacion.buscar(codigoDespacho);
			}
			
			// se crea un item nuevo, si existe en la transacción se actualiza el item de la transacción.
			item = new ItemTransferenciaDepositos();
			item.setTransferencia(this);
			item.setProducto(producto);
			item.setUnidadMedida(producto.getUnidadMedida());
			item.setDespacho(despacho);
			
			String clave = this.obtenerClaveCSV(item);
			if (this.getItemsPorProducto().containsKey(clave)){
				// se reemplaza el item nuevo por el que tiene la transacción
				item = this.getItemsPorProducto().get(clave);				
			}
			else{
				this.getItemsPorProducto().put(clave, item);
				// se agrega el item nuevo
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
	public void finalizarImportacionCSV() {
		this.getItemsPorProducto().clear();
		this.itemsPorProductoCSV = null;		
	}		

	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if ((this.getOrigen() != null) && (this.getDestino() != null)){
			if (this.getOrigen().equals(this.getDestino())){
				errores.add("Coinciden el origen y destino");
			}			
		}
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getOrigen() != null){
			this.setSucursal(this.getOrigen().getSucursal());
		}
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			Producto producto = (Producto)MapFacade.findEntity("Producto", key);
			this.agregarProducto(producto);
			return true;
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar producto: " + error);
		}	
	}
	
	private void agregarProducto(Producto producto){
		boolean repetido = false;
		for(ItemTransferenciaDepositos item: this.getItems()){
			if (item.getProducto().equals(producto)){
				repetido = true;
				break;
			}
		}		
		if (!repetido){
			this.crearItemDepositoTransferencia(producto);
			
		}
		else{
			throw new ValidationException("Producto repetido " + producto.getCodigo());
		}
	}
	
	private ItemTransferenciaDepositos crearItemDepositoTransferencia(Producto producto){
		ItemTransferenciaDepositos item = new ItemTransferenciaDepositos();
		item.setTransferencia(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		item.recalcular();
		this.getItems().add(item);
		XPersistence.getManager().persist(item);
		return item;
	}
	
	@Override
	public EmpresaExterna empresaExternaInventario() {
		return null;
	}
}
