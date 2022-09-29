package org.openxava.inventario.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
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
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.RemoveAction;
import org.openxava.annotations.RemoveSelectedAction;
import org.openxava.annotations.Required;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.RowStyles;
import org.openxava.annotations.SaveAction;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.base.model.EstrategiaCancelacionPendientePorItem;
import org.openxava.base.model.IEstrategiaCancelacionPendiente;
import org.openxava.base.model.IItemPendientePorCantidad;
import org.openxava.base.model.Transaccion;
import org.openxava.codigobarras.model.IControlCodigoBarra;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.Cantidad;
import org.openxava.util.Messages;
import org.openxava.ventas.model.Producto;

@Entity

@Views({ 
	@View(members = "Principal{" + 
				"Principal[#" + 
				"descripcion, estado;" + 
				"numero, fecha, fechaCreacion;" +
				"resultado;" + 
				"origen;" + 
				"recepcion;" + 
				"observaciones];" + 
				"items;" + 
			"}" + 
			"Remito{remito}, Trazabilidad{trazabilidad}"), 
	@View(name = "Simple", members = "numero, estado;") 
})

@Tab(filter=SucursalEmpresaFilter.class, 
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL, 
	properties="fecha, numero, estado, origen.codigo, recepcion.codigo, fechaCreacion, usuario", 
	defaultOrder="${fechaCreacion} desc")

public class ControlMercaderia extends Transaccion implements ITransaccionInventario, IControlCodigoBarra {

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@DescriptionsList(descriptionProperties = "codigo, nombre")
	@NoCreate
	@NoModify
	@ReadOnly
	private Deposito origen;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@DescriptionsList(descriptionProperties = "codigo, nombre")
	@NoCreate
	@NoModify
	@ReadOnly
	private Deposito recepcion;

	@OneToMany(mappedBy = "controlMercaderia", cascade = CascadeType.ALL)
	@ListProperties(value = "producto.codigo, producto.nombre, cantidad, detalle, control")
	@SaveAction(value = "ItemTransaccion.save")
	@NewAction(value = "ItemTransaccion.new")
	@EditAction(value = "ItemTransaccion.edit")
	@RemoveAction(value = "ItemTransaccion.remove")
	@RemoveSelectedAction(value = "ItemTransaccion.removeSelected")
	@NoCreate
	@ListAction("LectorCodigoBarras.ControlPorCodigoBarras")
	@RowStyles({
		@RowStyle(style="color-fila-verde", property="control", value="Controlado"),
		@RowStyle(style="pendiente-ejecutado", property="control", value="NoControlar"),
	})	
	private Collection<ItemControlMercaderia> items;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Remito remito;
	
	@Required
	@ReadOnly
	private ResultadoControlMercaderia resultado;
	
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		if (this.getItems().isEmpty()){
			errores.add("sin_items");
		}
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Control Mercadería";
	}

	public Deposito getOrigen() {
		return origen;
	}

	public void setOrigen(Deposito origen) {
		this.origen = origen;
	}

	public Deposito getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(Deposito recepcion) {
		this.recepcion = recepcion;
	}

	public Collection<ItemControlMercaderia> getItems() {
		return items;
	}

	public void setItems(Collection<ItemControlMercaderia> items) {
		this.items = items;
	}

	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente() {
		EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
		for (ItemControlMercaderia item : this.getItems()) {
			if (item.getItemRemito() != null) {
				IItemPendientePorCantidad pendientePorCantidad = item.getItemRemito()
						.itemPendienteControlMercaderiaProxy();
				if (pendientePorCantidad != null) {
					Cantidad cantidadPendiente = pendientePorCantidad.getCantidadACancelar();
					cantidadPendiente.setCantidad(item.getCantidad());
					cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
					estrategia.getItemsPendientes().add(pendientePorCantidad);
				}
			}
		}
		if (!estrategia.getItemsPendientes().isEmpty()) {
			return estrategia;
		} else {
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		movimientos.addAll(this.getItems());
		ResultadoControlMercaderia resultado = this.getResultado();
		for(ItemControlMercaderia item: this.getItems()){
			if (resultado.equals(ResultadoControlMercaderia.MercaderiaNoRecibida)){
				// se hace el reingreso al depósito origen
				ItemMovInvIngresoProxy itemIngreso = new ItemMovInvIngresoProxy(item);
				itemIngreso.setDeposito(this.getOrigen());
				movimientos.add(itemIngreso);						
			}
		}		
		return movimientos;
		
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}

	public ResultadoControlMercaderia getResultado() {
		return resultado;
	}

	public void setResultado(ResultadoControlMercaderia resultado) {
		this.resultado = resultado;
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		this.setSucursal(this.getRecepcion().getSucursal());
	}
	
	@Override
	protected void asignarSucursal(){
		super.asignarSucursal();
		if (this.getRecepcion() != null){
			this.setSucursal(this.getRecepcion().getSucursal());
		}
	}

	@Override
	public EmpresaExterna empresaExternaInventario() {
		if (this.getRemito() != null){
			return this.getRemito().empresaExternaInventario();
		}
		else{
			return null;
		}		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemsParaControlarPorCodigoBarra(List<IItemControlCodigoBarras> items, Producto producto,
			BigDecimal cantidadControlar) {
		if (!this.esNuevo()){
			Query query = XPersistence.getManager().createQuery("from ItemControlMercaderia where controlMercaderia = :tr and producto = :producto");
			query.setParameter("producto", producto);
			query.setParameter("tr", this);
			query.setFlushMode(FlushModeType.COMMIT);
			items.addAll(query.getResultList());
		}		
	}

	@Override
	public boolean permiteCantidadesNegativas() {
		return false;
	}

	@Override
	public IItemControlCodigoBarras crearItemDesdeCodigoBarras(Producto producto, BigDecimal cantidad,
			String codigoLote, String codigoSerie, Date vencimiento) {
		return null;
	}

	@Override
	public BigDecimal mostrarTotalLectorCodigoBarras() {
		return null;
	}

}

