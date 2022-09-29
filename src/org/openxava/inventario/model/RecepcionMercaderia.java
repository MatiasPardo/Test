package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.codigobarras.model.IControlCodigoBarra;
import org.openxava.codigobarras.model.IItemControlCodigoBarras;
import org.openxava.compras.model.*;
import org.openxava.inventario.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.model.CondicionVenta;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
		"Principal{" +
			"Principal[#" + 
					"descripcion, estado, fechaCreacion;" + 
					"numero, fecha, numeroFactura;" +
					"deposito;" +
					"proveedor;" + 
					"despacho;" + 
					"observaciones];" +
			"items;" + 
		"} Trazabilidad{trazabilidad}"
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		properties="fecha, numero, estado, deposito.codigo, proveedor.nombre, fechaCreacion, usuario",
		defaultOrder="${fechaCreacion} desc")

public class RecepcionMercaderia extends Transaccion implements ITransaccionInventario, IControlCodigoBarra, IAccionCancelacionPendientes{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=DepositoDefaultCalculator.class)
	private Deposito deposito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("Transaccion")	
	private Proveedor proveedor;
	
	@OneToMany(mappedBy="recepcionMercaderia", cascade=CascadeType.ALL)
	@ListProperties("producto.codigo, producto.nombre, cantidad, noEntregados, noConforme, cantidadExcedeOC, motivoNoConformidad, pendienteFacturacion")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@EditAction(value="ItemTransaccion.edit")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@DetailAction(value="ItemTransaccion.dividir")
	@ListAction("LectorCodigoBarras.ControlPorCodigoBarras")
	@RowStyles({
		@RowStyle(style="color-fila-verde", property="control", value="Controlado"),
		@RowStyle(style="pendiente-ejecutado", property="control", value="NoControlar"),
	})	
	private Collection<ItemRecepcionMercaderia> items;
	
	public Deposito getDeposito() {
		return deposito;
	}

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private DespachoImportacion despacho;
	
	public DespachoImportacion getDespacho() {
		return despacho;
	}

	public void setDespacho(DespachoImportacion despacho) {
		this.despacho = despacho;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public Collection<ItemRecepcionMercaderia> getItems() {
		return items;
	}

	public void setItems(Collection<ItemRecepcionMercaderia> items) {
		this.items = items;
	}

	@Override
	protected void inicializar(){
		super.inicializar();
		if (this.getDeposito() == null){
			ICalculator defaultCalculator = new DepositoDefaultCalculator();
			try {
				this.setDeposito((Deposito)defaultCalculator.calculate());
			} catch (Exception e) {
			}
		}
	}
	
	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		ArrayList<IItemMovimientoInventario> movimientos = new ArrayList<IItemMovimientoInventario>();
		for(IItemMovimientoInventario item: this.getItems()){
			if (item.cantidadStock().getCantidad().compareTo(BigDecimal.ZERO) != 0){
				movimientos.add(item);
			}
		}
		return movimientos;
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Recepción de Mercadería";
	}
	
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
		for (ItemRecepcionMercaderia item: this.getItems()){
			if (item.getItemOrdenCompra() != null){
				IItemPendientePorCantidad pendientePorCantidad = item.getItemOrdenCompra().itemPendienteRecepcionMercaderiaProxy();
				if (pendientePorCantidad != null){
					((ItemPendienteRecepcionMercaderiaProxy)pendientePorCantidad).setItemRecepcionMercaderia(item);
					Cantidad cantidadPendiente = pendientePorCantidad.getCantidadACancelar();
					cantidadPendiente.setCantidad(item.getCantidad().subtract(item.getCantidadExcedeOC()).add(item.getNoEntregados()));
					cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
					estrategia.getItemsPendientes().add(pendientePorCantidad);
				}
			}
		}
		if (!estrategia.getItemsPendientes().isEmpty()){
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		if (this.getDeposito() == null){
			errores.add("Deposito no asignado");
		}
	}
	
	// WorkFlow Factura de Compra
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(FacturaCompra.class);	
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){
		if (FacturaCompra.class.equals(tipoTrDestino)){
			for(ItemRecepcionMercaderia item: this.getItems()){
				if (item.getCantidad().compareTo(BigDecimal.ZERO) > 0){
					return true;
				}
			}
			return false;
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	/*protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(FacturaCompra.class)){
			FacturaCompra factura = (FacturaCompra)destino;
			factura.setRecepcion(this);
		}
	}*/
	
	protected void pasajeAtributosWorkFlowPrePersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(FacturaCompra.class)){
			FacturaCompra factura = (FacturaCompra)destino;
			factura.setRecepcion(this);
		}
		else{
			super.pasajeAtributosWorkFlowPrePersist(destino, items);
		}
	}
	
	/*protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(FacturaCompra.class)){
			FacturaCompra factura  = (FacturaCompra) destino;
			Collection<ItemCompraElectronica> itemsDestino = new LinkedList<ItemCompraElectronica>();
			for(Pendiente pendiente: pendientes){
				RecepcionMercaderia origen = (RecepcionMercaderia)pendiente.origen();				
				for(ItemRecepcionMercaderia itemOrigen: origen.getItems()){
					ItemCompraElectronica itemDestino = new ItemCompraElectronica();
					itemDestino.copiarPropiedades(itemOrigen);
					itemDestino.setCompra(factura);
					if (itemOrigen.getItemOrdenCompra() != null){
						itemDestino.setPorcentajeDescuento(itemOrigen.getItemOrdenCompra().getPorcentajeDescuento());
						itemDestino.setDetalle(itemOrigen.getItemOrdenCompra().getDetalle());
					}
					itemDestino.recalcular();
					
					factura.fusionarItem(itemsDestino, itemDestino);					
				}
			}
			
			if (factura.getItems() == null) factura.setItems(new ArrayList<ItemCompraElectronica>());
			factura.getItems().addAll(itemsDestino);
			for(ItemCompraElectronica item: factura.getItems()){
				XPersistence.getManager().persist(item);
			}
		}
	}*/
	
	protected void pasajeAtributosWorkFlowPosPersist(Transaccion destino, List<IItemPendiente> items){
		if (destino.getClass().equals(FacturaCompra.class)){
			FacturaCompra factura  = (FacturaCompra) destino;
			Collection<ItemCompraElectronica> itemsDestino = new LinkedList<ItemCompraElectronica>();
			boolean primerItemOrdenCompra = true;
			for(IItemPendiente itemPendiente: items){							
				ItemRecepcionMercaderia itemOrigen = (ItemRecepcionMercaderia)itemPendiente.getItem();
				ItemCompraElectronica itemDestino = new ItemCompraElectronica();
				itemDestino.copiarPropiedades(itemOrigen);
				if (itemOrigen.getItemOrdenCompra() != null){
					itemDestino.setPorcentajeDescuento(itemOrigen.getItemOrdenCompra().getPorcentajeDescuento());
					itemDestino.setDetalle(itemOrigen.getItemOrdenCompra().getDetalle());
					itemDestino.setAlicuotaIva(itemOrigen.getItemOrdenCompra().getAlicuotaIva());
					if (itemOrigen.getItemOrdenCompra().getCentroCostos() != null){
						itemDestino.setCentroCostos(itemOrigen.getItemOrdenCompra().getCentroCostos());
					}
					if (primerItemOrdenCompra){
						CondicionVenta condicionCompra = itemOrigen.getItemOrdenCompra().getOrdenCompra().getCondicionCompra();
						if (condicionCompra != null){
							factura.setCondicionCompra(condicionCompra);
						}
					}
					primerItemOrdenCompra = false;
				}
				itemDestino.setCantidad(itemOrigen.getPendienteFacturacion());
				itemDestino.setCompra(factura);
				itemDestino.agregarRecepcion(itemOrigen);
				itemDestino.recalcular();
				factura.fusionarItem(itemsDestino, itemDestino);									
			}
			
			if (factura.getItems() == null) factura.setItems(new ArrayList<ItemCompraElectronica>());
			factura.getItems().addAll(itemsDestino);
			for(ItemCompraElectronica item: factura.getItems()){
				XPersistence.getManager().persist(item);
				for(ItemRecepcionFacturaCompra itemRecepcion: item.getRecepciones()){
					XPersistence.getManager().persist(itemRecepcion);
				}
			}
		}
	}
	
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		this.getFacturasGenerada(trs);
	}
		
	@Hidden
	private void getFacturasGenerada(Collection<Transaccion> trs){
		if (this.cerrado()) {
			StringBuffer sqlText = new StringBuffer("select ic.compra_id from ").append(Esquema.concatenarEsquema("ItemRecepcionFacturaCompra i "));
			sqlText.append("join ").append(Esquema.concatenarEsquema("ItemCompraElectronica ic ")).append("on ic.id = i.itemFactura_id ");  
			sqlText.append("join ").append(Esquema.concatenarEsquema("ItemRecepcionMercaderia ir ")).append("on ir.id = i.itemRecepcion_id ");  
			sqlText.append("where ir.recepcionMercaderia_id = :id group by ic.compra_id");
			 
			Query query = XPersistence.getManager().createNativeQuery(sqlText.toString());
			query.setParameter("id", this.getId());			
			List<?> list = query.getResultList();
			for(Object result: list){
				String id = (String)result;
				trs.add((FacturaCompra)XPersistence.getManager().find(FacturaCompra.class, id));
			}
		}		
	}
	
	@org.hibernate.annotations.Formula("(select f.numero from CompraElectronica f where f.recepcion_id = id and f.estado = 1 limit 1)")
	@ReadOnly
	private String numeroFactura;

	public String getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
	}
	
	public EmpresaExterna empresaExternaInventario() {
		if (this.getProveedor() != null){
			return (EmpresaExterna)XPersistence.getManager().find(EmpresaExterna.class, this.getProveedor().getId());
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
			Query query = XPersistence.getManager().createQuery("from ItemRecepcionMercaderia where recepcionMercaderia = :recepcion and producto = :producto");
			query.setParameter("producto", producto);
			query.setParameter("recepcion", this);
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

	@Override
	public void prepararParaCancelarPendiente() {
		for(ItemRecepcionMercaderia item: this.getItems()){
			item.setNoEntregados(item.getCantidad());
			item.setCantidad(BigDecimal.ZERO);
		} 		
	}
}
