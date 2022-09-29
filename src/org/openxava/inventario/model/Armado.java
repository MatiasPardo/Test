package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.inventario.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
	"Principal[#" + 
			"descripcion;" + 
			"numero, fecha, fechaCreacion;" +
			"estado, usuario;" + 
			"deposito, listaCostos;" +
			"producto;" +
			"unidadMedida, cantidad; despacho; lote;" + 
			"observaciones; total];" +
	"componentes;" 		
	),
	@View(name="Simple", members="numero, estado;")
})


@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, numero, estado, producto.codigo, cantidad, deposito.codigo, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")


public class Armado extends Transaccion implements ITransaccionInventario, IItemMovimientoInventario{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=DepositoDefaultCalculator.class)
	private Deposito deposito;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
					condition="${costo} = true")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ListaPrecioDefaultCalculator.class, 
								properties={@PropertyValue(name="costos", value="true")})
	private ListaPrecio listaCostos;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@OnChange(OnChangeProducto.class)
	private Producto producto;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal cantidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Lote lote;
	
	@OneToMany(mappedBy="armado", cascade=CascadeType.ALL)	
	@ListProperties("producto.codigo, producto.nombre, unidadMedida.codigo, cantidad, despacho.codigo, costoUnitario, costoTotal")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	private Collection<ItemArmado> componentes;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	public ListaPrecio getListaCostos() {
		return listaCostos;
	}

	public void setListaCostos(ListaPrecio listaCostos) {
		this.listaCostos = listaCostos;
	}

	@SuppressWarnings("unchecked")
	public Collection<ItemArmado> getComponentes() {
		return componentes == null ?  Collections.EMPTY_LIST : componentes;
	}

	public void setComponentes(Collection<ItemArmado> componentes) {
		this.componentes = componentes;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}
	
	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	public Lote getLote() {
		return lote;
	}

	public void setLote(Lote lote) {
		this.lote = lote;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Armado";
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : this.total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		// se egresan los componentes
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getComponentes());
		
		// se ingresa el armado
		movimientos.add(this);
		return movimientos;		
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		if (this.getDeposito() != null){
			parameters.put("DEPOSITO_CODIGO", this.getDeposito().getCodigo());
			parameters.put("DEPOSITO_NOMBRE", this.getDeposito().getNombre());
		}
		else{
			parameters.put("DEPOSITO_CODIGO", "");
			parameters.put("DEPOSITO_NOMBRE", "");
		}
		
		if (this.getProducto() != null){
			parameters.put("PRODUCTO_CODIGO", this.getProducto().getCodigo());
			parameters.put("PRODUCTO_NOMBRE", this.getProducto().getNombre());
		}
		else{
			parameters.put("PRODUCTO_CODIGO", "");
			parameters.put("PRODUCTO_NOMBRE", "");
		}
		
		parameters.put("CANTIDAD", this.getCantidad());
		if (this.getUnidadMedida() != null){
			parameters.put("UNIDADMEDIDA", this.getUnidadMedida().toString());
		}
		else{
			parameters.put("UNIDADMEDIDA", "");
		}
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvIngreso();
		}
		else{
			return new TipoMovInvEgreso();
		}
	}

	@Override
	public Cantidad cantidadStock() {
		Cantidad cantidad = new Cantidad();
		cantidad.setUnidadMedida(this.getUnidadMedida());
		cantidad.setCantidad(this.getCantidad().abs());
		return cantidad;		
	}

	@Override
	public void actualizarCantidadItem(Cantidad cantidad) {
		if (cantidad.getUnidadMedida().equals(this.getUnidadMedida())){
			this.setCantidad(cantidad.getCantidad());		
		}
		else{
			throw new ValidationException("Difieren las unidades de medida");
		}		
	}
	
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		if (this.getComponentes().isEmpty()){
			errores.add("sin_items");
		}
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		BigDecimal costoTotal = BigDecimal.ZERO;
		for(ItemArmado item: this.getComponentes()){
			item.recalcular();
			costoTotal = costoTotal.add(item.getCostoTotal());
		}
		this.setTotal(costoTotal);
	}
	
	public void agregarComponentesPorDefecto(){
		if (!this.cerrado()){
			if ((this.getProducto() != null) && (this.getCantidad().compareTo(BigDecimal.ZERO) > 0) 
					&& (this.getUnidadMedida() != null)){
				
				Collection<ComponenteProducto> composicionProducto = new LinkedList<ComponenteProducto>();
				Cantidad cantidadArmar = new Cantidad();
				cantidadArmar.setUnidadMedida(this.getUnidadMedida());
				cantidadArmar.setCantidad(this.getCantidad());
				this.getProducto().explotarComponentes(composicionProducto, cantidadArmar);
				for(ComponenteProducto componente: composicionProducto){
					ItemArmado item = new ItemArmado();
					item.setArmado(this);
					this.getComponentes().add(item);
					item.setProducto(componente.getComponente());
					item.setUnidadMedida(componente.getUnidadMedida());
					item.setCantidad(componente.getCantidad());
					XPersistence.getManager().persist(item);
				}
				
				this.recalcularTotales();
			}
		}
	}
	
	@Override
	public void crearItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem){
	}

	@Override
	public void posActualizarItemGeneradoPorInventario(IItemMovimientoInventario nuevoItem) {		
	}
	
	@Override
	public EmpresaExterna empresaExternaInventario() {
		return null;
	}
}
