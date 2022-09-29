package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.filter.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion;" + 
			"numero, fecha, fechaCreacion;" +
			"estado, subestado, ultimaTransicion;" + 
			"depositoOrigen, motivo;" +
			"observaciones];" +
	"items;" 		
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		properties="fecha, numero, estado, depositoOrigen.codigo, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class AjusteInventario extends Transaccion implements ITransaccionInventario, IImportadorItemCSV{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private Deposito depositoOrigen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	private MotivoAjusteInventario motivo;

	@OneToMany(mappedBy="ajusteInventario", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, despacho.codigo, lote.codigo")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@ListAction("ColeccionItemsTransaccionProductos.Multiseleccion")
	private Collection<ItemAjusteInventario> items;

	
	public Deposito getDepositoOrigen() {
		return depositoOrigen;
	}

	public void setDepositoOrigen(Deposito depositoOrigen) {
		this.depositoOrigen = depositoOrigen;
	}

	public MotivoAjusteInventario getMotivo() {
		return motivo;
	}

	public void setMotivo(MotivoAjusteInventario motivo) {
		this.motivo = motivo;
	}
	
	public Collection<ItemAjusteInventario> getItems() {
		return items;
	}

	public void setItems(Collection<ItemAjusteInventario> items) {
		this.items = items;
	}
	
	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario(){
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getItems());
		return movimientos;
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Ajuste de Inventario";
	}
	
	@Override
	public ObjetoNegocio generarCopia() {
		AjusteInventario copia = new AjusteInventario();
		copia.copiarPropiedades(this);
		copia.setItems(new ArrayList<ItemAjusteInventario>());
		
		XPersistence.getManager().persist(copia);
		
		for(ItemAjusteInventario item: this.getItems()){
			ItemAjusteInventario itemCopia = new ItemAjusteInventario();
			itemCopia.copiarPropiedades(item);
			itemCopia.setAjusteInventario(copia);
			copia.getItems().add(itemCopia);
			
			XPersistence.getManager().persist(copia);
		}
		return copia;
	}
	
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getDepositoOrigen() != null){
			parameters.put("DEPOSITO_CODIGO", this.getDepositoOrigen().getCodigo());
			parameters.put("DEPOSITO_NOMBRE", this.getDepositoOrigen().getNombre());
		}
		else{
			parameters.put("DEPOSITO_CODIGO", "");
			parameters.put("DEPOSITO_NOMBRE", "");
		}
		
		if (this.getMotivo() != null){
			parameters.put("MOTIVO", this.getMotivo().getNombre());
		}
		else{
			parameters.put("MOTIVO", "");
		}
	}
	
	@Transient
	private Map<String, ItemAjusteInventario> itemsPorProductoCSV = null; 
	
	private String obtenerClaveCSV(ItemAjusteInventario item){
		if (item.getProducto() != null){
			String clave = item.getProducto().getCodigo();
			if (item.getDespacho() != null){
				clave += item.getDespacho().getCodigo();
			}
			if (item.getLote() != null){
				clave += item.getLote().getCodigo();
			}
			return clave;
		}
		else{
			return null;
		}
	}
	
	private Map<String, ItemAjusteInventario> getItemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemAjusteInventario>();
			for(ItemAjusteInventario item: this.getItems()){
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
		ItemAjusteInventario item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			if (cantidad.compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException("Cantidad en cero");
			}
			String codigoDespacho = values[3];
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			DespachoImportacion despacho = null;
			if (!Is.emptyString(codigoDespacho)){
				despacho = DespachoImportacion.buscar(codigoDespacho);
			}
			
			Lote lote = null;
			if (producto.getLote()){
				if (values.length > 4){
					String codigoLote = values[4];
					if (!Is.emptyString(codigoLote)){
						lote = Lote.buscarPorCodigo(codigoLote, codigoProducto);
						if (lote == null){
							throw new ValidationException("No existe el lote " + codigoLote + " para el producto " + codigoProducto);
						}
					}
					else if (cantidad.compareTo(BigDecimal.ZERO) > 0){
						throw new ValidationException("Falta asignar el código de lote para " + codigoProducto);
					}
				}
			}
			
			// se crea un item nuevo, si existe en la transacción se actualiza el item de la transacción.
			item = new ItemAjusteInventario();
			item.setAjusteInventario(this);
			item.setProducto(producto);
			item.setUnidadMedida(producto.getUnidadMedida());
			item.setDespacho(despacho);
			item.setLote(lote);
			
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
	
	@Override
	protected void asignarSucursal(){
		if (this.getDepositoOrigen() != null){
			this.setSucursal(this.getDepositoOrigen().getSucursal());
		}
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
		ItemAjusteInventario item = new ItemAjusteInventario();
		item.setAjusteInventario(this);
		item.setProducto(producto);
		item.setCantidad(new BigDecimal(1));
		if (producto.getDespacho()){
			item.setDespacho(producto.ultimoDespacho(this.getDepositoOrigen().getId()));
			if (item.getDespacho() == null){
				item.setDespacho(producto.ultimoDespachoGeneral());
			}
		}
		if (producto.getLote()){
			item.setLote(producto.loteMasViejo(this.getDepositoOrigen().getId()));			
		}
		this.getItems().add(item);
		item.recalcular();
		XPersistence.getManager().persist(item);
	}

	@Override
	public EmpresaExterna empresaExternaInventario() {
		return null;
	}
}
