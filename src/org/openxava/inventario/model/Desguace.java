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
			"deposito, depositoPartes;" +
			"listaCostos;" +
			"producto;" +
			"unidadMedida, cantidad; despacho; lote;" + 
			"observaciones; total];" +
	"partes;" 		
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, estado, producto.codigo, cantidad, deposito.codigo, depositoPartes.codigo, fechaCreacion, usuario",
	defaultOrder="${fechaCreacion} desc")


public class Desguace extends Transaccion implements ITransaccionInventario, IItemMovimientoInventario{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=DepositoDefaultCalculator.class)
	private Deposito deposito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=DepositoDefaultCalculator.class)
	private Deposito depositoPartes;
	
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
	@SearchAction("ReferenciaProductoFiltroPorDeposito.buscar")
	private Producto producto;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					depends=UnidadMedida.DEPENDSDESCRIPTIONLIST,
					condition=UnidadMedida.CONDITIONDESCRIPTIONLIST)
	@NoCreate @NoModify
	@OnChange(OnChangeUnidadMedida.class)
	private UnidadMedida unidadMedida;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@SearchAction("ReferenciaDespachoPorProducto.buscar")
	private DespachoImportacion despacho;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Lote lote;
	
	@Required
	@Min(value=0, message="No puede ser negativo")
	private BigDecimal cantidad;
	
	@OneToMany(mappedBy="desguace", cascade=CascadeType.ALL)	
	@ListProperties("producto.codigo, producto.nombre, unidadMedida.codigo, cantidad, despacho.codigo, costoUnitario, costoTotal")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	private Collection<ItemDesguace> partes;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Desguace";
	}

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public Deposito getDepositoPartes() {
		return depositoPartes;
	}

	public void setDepositoPartes(Deposito depositoPartes) {
		this.depositoPartes = depositoPartes;
	}
	
	public ListaPrecio getListaCostos() {
		return listaCostos;
	}

	public void setListaCostos(ListaPrecio listaCostos) {
		this.listaCostos = listaCostos;
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

	public BigDecimal getCantidad() {
		return cantidad == null ? BigDecimal.ZERO : this.cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	@SuppressWarnings("unchecked")
	public Collection<ItemDesguace> getPartes() {
		return partes == null ?  Collections.EMPTY_LIST : this.partes;
	}

	public void setPartes(Collection<ItemDesguace> partes) {
		this.partes = partes;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		// se ingresan las partes
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getPartes());
		
		// se egresa el producto desarmado
		movimientos.add(this);
		return movimientos;		
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}

	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		if (!reversion){
			return new TipoMovInvEgreso(true);
		}
		else{
			return new TipoMovInvIngreso();
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
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		BigDecimal costoTotal = BigDecimal.ZERO;
		for(ItemDesguace item: this.getPartes()){
			item.recalcular();
			costoTotal = costoTotal.add(item.getCostoTotal());
		}
		this.setTotal(costoTotal);
	}
	
	public void agregarPartesPorDefecto(){
		if (!this.cerrado()){
			if ((this.getProducto() != null) && (this.getCantidad().compareTo(BigDecimal.ZERO) > 0) 
					&& (this.getUnidadMedida() != null)){
				
				for(ItemDesguace item: this.getPartes()){
					XPersistence.getManager().remove(item);
				}
				this.getPartes().clear();
				
				Collection<ComponenteProducto> composicionProducto = new LinkedList<ComponenteProducto>();
				Cantidad cantidadArmar = new Cantidad();
				cantidadArmar.setUnidadMedida(this.getUnidadMedida());
				cantidadArmar.setCantidad(this.getCantidad());
				this.getProducto().explotarComponentes(composicionProducto, cantidadArmar);
				for(ComponenteProducto componente: composicionProducto){
					ItemDesguace item = new ItemDesguace();
					item.setDesguace(this);
					this.getPartes().add(item);
					item.setProducto(componente.getComponente());
					item.setUnidadMedida(componente.getUnidadMedida());
					item.setCantidad(componente.getCantidad());
					if (item.getProducto().getDespacho()){
						item.setDespacho(this.getDespacho());
					}
					if (item.getProducto().getLote()){
						item.setLote(item.getProducto().loteMasViejo(this.getDepositoPartes().getId()));
					}
					XPersistence.getManager().persist(item);
				}
				this.recalcularTotales();
			}
		}
	}


	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		
		if (this.getPartes().isEmpty()){
			errores.add("sin_items");
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
