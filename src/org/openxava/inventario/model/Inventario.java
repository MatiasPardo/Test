package org.openxava.inventario.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.mercadolibre.model.ItemPedidoML;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"Principal{producto;" +
				"deposito;" +
				"disponible, stock, reservado;" + 
				"despacho;" + 
				"lote;}" +
		"Reservas{Ordenes{ordenesPreparacion}" + 
				"Consignaciones{remitosPorConsignacion}" +
				"Sucursales{remitosSucursales}" + 
				"Ecommerce{pedidosEcommerce}" + 
				 "}"		
		)
})

@Tab(
	properties="producto.codigo, producto.nombre, deposito.codigo, stock, reservado, disponible, despacho.codigo, lote.codigo",
	filter=DepositoSucursalFilter.class,
	baseCondition="(${stock} != 0 or ${disponible} != 0 or ${reservado} != 0) and " + DepositoSucursalFilter.BASECONDITION
	)

public class Inventario extends ObjetoNegocio{

	public static void actualizarInventario(ArrayList<IItemMovimientoInventario> movimientos, boolean revierte, Collection<Kardex> registroKardex){
		if (!movimientos.isEmpty()){
			Map<String, Inventario> idsProcesados = new HashMap<String, Inventario>();
			movimientos.sort(new ComparatorItemMovInventario());
			
			boolean existenciaStockObligatoria = Esquema.getEsquemaApp().getStockObligatorio();
			Messages errores = new Messages();
			for(IItemMovimientoInventario movimiento: movimientos){
				try{
					if (movimiento.getProducto().getTipo().equals(TipoProducto.Producto)){
						Inventario.validarAtributosInventarioCompleto(movimiento);
						
						StringBuffer sql = new StringBuffer("select i.id from " + Esquema.concatenarEsquema("Inventario") + " i ");
						StringBuffer wheresql = new StringBuffer("where i.producto_id = :producto and i.deposito_id = '" + movimiento.getDeposito().getId() + "' ");
						StringBuffer ordersql = new StringBuffer();
						Producto producto = movimiento.getProducto();
						if (producto.getLote()){
							if (movimiento.getLote() != null){
								wheresql.append("and i.lote_id = '" + movimiento.getLote().getId() + "' ");								
							}
							sql.append("join " + Esquema.concatenarEsquema("Lote") + " l on l.id = i.lote_id ");
							ordersql.append("l.fechaVencimiento asc ");
						}
						if (producto.getDespacho()){
							if (movimiento.getDespacho() != null){
								wheresql.append("and i.despacho_id = '" + movimiento.getDespacho().getId() + "' ");						
							}
							sql.append("join " + Esquema.concatenarEsquema("DespachoImportacion") + " d on d.id = i.despacho_id ");
							if (ordersql.length() > 0){
								ordersql.append(", ");
							}
							ordersql.append("d.fechacreacion asc ");
						}
						sql.append(wheresql);
						if (ordersql.length() > 0){
							sql.append("order by ").append(ordersql);
						}						
						sql.append(" for update");				
						Query query = XPersistence.getManager().createNativeQuery(sql.toString());
						query.setParameter("producto", producto.getId());
						@SuppressWarnings("unchecked")
						List<String> list = query.getResultList();
						if (list.isEmpty()){
							Kardex kardex = Inventario.crearKardex(movimiento);
							movimiento.tipoMovimientoInventario(revierte).actualizarStockSinInventario(movimiento, kardex, existenciaStockObligatoria);
							if (kardex.getCantidad().compareTo(BigDecimal.ZERO) != 0){
								registroKardex.add(kardex);
							}
						}
						else{
							boolean actualizado = false;
							for(String id: list){
								Inventario inv;
								if (idsProcesados.containsKey(id)){
									inv = (Inventario)idsProcesados.get(id);
								}
								else{
									inv = (Inventario) XPersistence.getManager().find(Inventario.class, id);
									idsProcesados.put(id, inv);
								}
								Kardex kardex = Inventario.crearKardex(movimiento);
								actualizado = movimiento.tipoMovimientoInventario(revierte).actualizarStock(inv, movimiento, kardex, existenciaStockObligatoria);
								if (kardex.getCantidad().compareTo(BigDecimal.ZERO) != 0){
									registroKardex.add(kardex);
								}
								if (actualizado){
									break;
								}
							}
							if (!actualizado){
								throw new ValidationException("No hay inventario suficiente para " + producto.getCodigo());
							}
						}
					}
				}
				catch(ValidationException e){
					errores.add(e.getErrors());
				}
				catch(Exception e){
					if (!Is.emptyString(e.getMessage())){
						errores.add(e.getMessage());
					}
					else{
						errores.add(e.toString());
					}
				}
			}
			
			if (!errores.isEmpty()){
				throw new ValidationException(errores);
			}
		}
	}
	
	public static void validarAtributosInventarioCompleto(IItemMovimientoInventario movimiento){
		if (movimiento.tipoMovimientoInventario(false).requiereAtributosInventario()){
			List<String> atributos = new LinkedList<String>();
			if (!Inventario.atributosInventarioCompleto(movimiento, atributos)){
				throw new ValidationException(movimiento.getProducto().getCodigo() + ": Falta asignar " + atributos.toString());
			}
			
		}
	}
	
	public static boolean atributosInventarioCompleto(IItemMovimientoInventario movimiento, List<String> atributosNoAsignados){
		boolean completo = true;
		Producto producto = movimiento.getProducto();
		if (producto.getDespacho() && (movimiento.getDespacho() == null)){
			completo = false;
			if (atributosNoAsignados != null){
				atributosNoAsignados.add("despacho");
			}
		}
		if (producto.getLote() && (movimiento.getLote() == null)){
			completo = false;
			if (atributosNoAsignados != null){
				atributosNoAsignados.add("lote");
			}
		}
		
		return completo;
	}
	
	public static Inventario crearInventario(IItemMovimientoInventario movimiento){
		Inventario nuevoInventario = new Inventario();
		nuevoInventario.setDeposito(movimiento.getDeposito());
		nuevoInventario.setProducto(movimiento.getProducto());
		nuevoInventario.setDespacho(movimiento.getDespacho());
		nuevoInventario.setLote(movimiento.getLote());
		XPersistence.getManager().persist(nuevoInventario);
		return nuevoInventario;
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoFrame
	private Producto producto;
	
	@ReadOnly
	private BigDecimal stock = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal reservado = BigDecimal.ZERO;
	
	@ReadOnly
	private BigDecimal disponible = BigDecimal.ZERO;
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Deposito deposito;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Inventario")
	private DespachoImportacion despacho;

	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView(value="Inventario")
	private Lote lote;
	
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getStock() {
		return this.stock == null ? BigDecimal.ZERO: this.stock;
	}

	public void setStock(BigDecimal stock) {
		this.stock = stock;
		recalcularDisponible();
	}

	public BigDecimal getReservado() {
		return this.reservado == null?BigDecimal.ZERO : this.reservado;
	}

	public void setReservado(BigDecimal reservado) {
		this.reservado = reservado;
		recalcularDisponible();
	}

	public BigDecimal getDisponible() {
		return this.disponible == null? BigDecimal.ZERO : this.disponible;
	}

	public void setDisponible(BigDecimal disponible) {
		this.disponible = disponible;
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

	private void recalcularDisponible(){
		this.setDisponible(this.getStock().subtract(this.getReservado()));
	}
	
	@Override
	public String toString(){
		String nombre = ""; 
		if (this.getProducto() != null){
			nombre = this.getProducto().getCodigo();
		}
		if (this.getDespacho() != null){
			nombre += " - " + this.getDespacho().getCodigo();
		}
		if (this.getLote() != null){
			nombre += " - " + this.getLote().getCodigo();
		}
		return nombre;
	}

	public void completarAtributosInventario(IItemMovimientoInventario movimiento) {
		movimiento.setDespacho(this.getDespacho());
		movimiento.setLote(this.getLote());
	}
	
	public void completarAtributosInventario(Kardex kardex) {
		kardex.setDespacho(this.getDespacho());
		kardex.setLote(this.getLote());
	}

	public IItemMovimientoInventario explotarItem(IItemMovimientoInventario movimiento) {
		try {
			ObjetoNegocio nuevo = (ObjetoNegocio)movimiento.getClass().newInstance();
			nuevo.copiarPropiedades(movimiento);
			movimiento.crearItemGeneradoPorInventario((IItemMovimientoInventario)nuevo);
			XPersistence.getManager().persist(nuevo);
			return (IItemMovimientoInventario)nuevo;
		} catch (Exception e) {
			throw new ValidationException("Error al explotar item");
		}
	}
	
	@PrePersist
	protected void onPrePersist() {
		super.onPrePersist();
		
		/*// Validar repetidos
		String sql = "from Inventario i where i.producto.id = :producto and deposito.id = :deposito";
		String id = this.getId();
		if (!Is.emptyString(id)){
			sql += " and i.id != id";
		}
		if (this.getDespacho() != null){
			sql += " and i.despacho.id = :despacho";
		}
		if (this.getLote() != null){
			sql += " and i.lote.id = :lote";
		}
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("producto", this.getProducto().getId());
		query.setParameter("deposito", this.getDeposito().getId());
		if (this.getDespacho() != null){
			query.setParameter("despacho", this.getDespacho().getId());
		}
		if (this.getLote() != null){
			query.setParameter("lote", this.getLote().getId());
		}
		query.setMaxResults(1);
		
		try{
			Object obj = query.getSingleResult();
			if (obj != null){
				throw new ValidationException(this.toString() + " se esta ingresando por duplicado");
			}
		}
		catch(NoResultException e){
			
		}*/
	}

	public static BigDecimal buscarStock(IItemMovimientoInventario item) {
		BigDecimal stock = BigDecimal.ZERO;
		Producto producto = item.getProducto();
		Deposito deposito = item.getDeposito();
		if ((producto != null) && (deposito != null)){
			HashMap<String, String> atributosInventario = new HashMap<String, String>();
			if(item.getDespacho() != null){
				atributosInventario.put("despacho_id", item.getDespacho().getId());
			}
			if(item.getLote() != null){
				atributosInventario.put("lote_id", item.getLote().getId());
			}
			stock = Inventario.buscarStockPorId(deposito.getId(), producto.getId(), atributosInventario);
		}
		return stock;
	}

	public static BigDecimal buscarStockPorId(String idDeposito, String idProducto, HashMap<String, String> atributosInventario) {
		BigDecimal stock = BigDecimal.ZERO;
		if ((!Is.emptyString(idDeposito)) && (!Is.emptyString(idProducto))){
			String sql = "select coalesce(sum(i.disponible),0) from " + Esquema.concatenarEsquema("inventario") +
					" i where i.deposito_id = :deposito and i.producto_id = :producto";
			if (atributosInventario != null){
				Iterator<Map.Entry<String, String>> it = atributosInventario.entrySet().iterator();
				while (it.hasNext()){
					Map.Entry<String, String> entry = it.next();
					if (!Is.emptyString(entry.getValue())){
						sql += " and " + entry.getKey() + " = '" + entry.getValue() + "'";
					}
				}
			}
			Query query = XPersistence.getManager().createNativeQuery(sql);
			query.setParameter("producto", idProducto);
			query.setParameter("deposito", idDeposito);
			query.setMaxResults(1);
			try{
				stock = (BigDecimal)query.getSingleResult();
			}
			catch(Exception e){
			}
		}
		return stock;
	}
	
	private static Kardex crearKardex(IItemMovimientoInventario movimiento){
		Kardex nuevo = new Kardex();
		nuevo.setProducto(movimiento.getProducto());
		nuevo.setDeposito(movimiento.getDeposito());
		nuevo.setDespacho(movimiento.getDespacho());
		nuevo.setLote(movimiento.getLote());
		nuevo.setUnidadMedidaOperacion(movimiento.getUnidadMedida());
		return nuevo;
	}
	
	public void actualizarStock(Cantidad cantidad){
		BigDecimal cantidadEquivalente = cantidad.convertir(this.getProducto().getUnidadMedida());
		this.setStock(this.getStock().add(cantidadEquivalente));
	}

	public void actualizarReservado(Cantidad cantidad) {
		BigDecimal cantidadEquivalente = cantidad.convertir(this.getProducto().getUnidadMedida());
		this.setReservado(this.getReservado().add(cantidadEquivalente));
	}
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("ordenPreparacion.numero, ordenPreparacion.fecha, cantidad, usuario, fechaCreacion")
	public Collection<ItemOrdenPreparacion> getOrdenesPreparacion(){
		String sql = "from ItemOrdenPreparacion i where i.remitido = 'f' and i.ordenPreparacion.estado = :confirmada and i.ordenPreparacion.deposito.id = :deposito"; 
		sql += this.filtroProductoInventario();
		sql += " order by i.fechaCreacion desc";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("confirmada", Estado.Confirmada);
		query.setParameter("deposito", this.getDeposito().getId());
		this.parametrosProductoInventario(query);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("remito.numero, remito.fecha, pendienteLiquidacion, usuario, fechaCreacion")
	public Collection<ItemRemito> getRemitosPorConsignacion(){
		String sql = "from ItemRemito i where i.remito.porConsignacion = 't' and i.pendienteLiquidacion > 0 and i.remito.estado = :confirmada and i.remito.depositoPorConsignacion.id = :deposito"; 
		sql += this.filtroProductoInventario();
		sql += " order by i.fechaCreacion desc";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("confirmada", Estado.Confirmada);
		query.setParameter("deposito", this.getDeposito().getId());
		this.parametrosProductoInventario(query);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("remito.numero, remito.fecha, pendienteLiquidacion, usuario, fechaCreacion")
	public Collection<ItemRemito> getRemitosSucursales(){
		String sql = "from ItemRemito i where i.remito.porConsignacion = 'f' and i.pendienteLiquidacion > 0 and i.remito.estado = :confirmada and i.remito.depositoPorConsignacion.id = :deposito ";
		sql += " and i.remito.ordenPreparacion.tipoEntidadCreadaPor = 'SolicitudMercaderia' ";
		sql += this.filtroProductoInventario();
		sql += " order by i.fechaCreacion desc";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("confirmada", Estado.Confirmada);
		query.setParameter("deposito", this.getDeposito().getId());
		this.parametrosProductoInventario(query);
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("pedido.numero, pedido.fecha, cantidad, usuario, fechaCreacion")
	public Collection<ItemPedidoML> getPedidosEcommerce(){
		// falta filtrar por depósito
		String sql = "from ItemPedidoML i where i.pedido.estado = :confirmada ";
		sql += this.filtroProductoInventario();
				
		String sqlPedidosSinFactura = sql += " and i.pedido.factura is null ";
		Query query = XPersistence.getManager().createQuery(sqlPedidosSinFactura);
		query.setParameter("confirmada", Estado.Confirmada);
		this.parametrosProductoInventario(query);
		Collection<ItemPedidoML> result1 = query.getResultList();
		
		String sqlPedidosFacturasAbiertas = sql += " and i.pedido.factura.estado != :facturaConfirmada";
		query = XPersistence.getManager().createQuery(sqlPedidosFacturasAbiertas);
		query.setParameter("facturaConfirmada", Estado.Confirmada);
		query.setParameter("confirmada", Estado.Confirmada);
		this.parametrosProductoInventario(query);
		Collection<ItemPedidoML> result2 = query.getResultList();

		result2.addAll(result1);
		
		return result2;
	}
	
	private String filtroProductoInventario(){
		String filtro = " and i.producto.id = :producto";
		if (this.getProducto().getDespacho()){
			filtro += " and i.despacho.id = :despacho";
		}
		if (this.getProducto().getLote()){
			filtro += " and i.lote.id = :lote";
		}
		return filtro;
	}
	
	private void parametrosProductoInventario(Query query){
		query.setParameter("producto", this.getProducto().getId());
		if (this.getProducto().getDespacho()){
			query.setParameter("despacho", this.getDespacho().getId());
		}
		if (this.getProducto().getLote()){
			query.setParameter("lote", this.getLote().getId());
		}
	}
	
}
