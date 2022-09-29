package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.actions.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.clasificadores.model.*;
import org.openxava.compras.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.calculators.*;
import org.openxava.impuestos.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.validators.ProductoValidator;
import org.openxava.calculators.*;

@Entity

@Views({
	@View(name="Simple",
		members="codigo, nombre"),
	@View(name="Despacho",
		members="codigo, nombre; despachos"),
	@View(members=
		"Principal{Principal[codigo, nombre, activo;" +
				"unidadMedida, tipo, codigoAnterior;" + 
				"imagen];" + 
		"Clasificadores[#" +
			"marca, genero, linea;" +
			"rubro, familia, subfamilia;" +
			"color, fabricante, cajaProducto;" + 
			"modelo, categoria;" + 
		"];" +
		"VisibleEn[#" +
			"compras, ventas;" +
		"];" +
		"tasaIva;" +
		/*"Inventario[despacho;" +
		 			"stock, reservado, pedidos, comprados;" +
		 			"disponible];" + */
		"Inventario[despacho, lote;" +
		 			"calculos];" +  			
		"unidadesMedida;" +
		"productoNumero1, productoNumero2, productoNumero3;" +			
		"}" +
		"Contabilidad{" + 
			"cuentaContableVentas;" +
			"cuentaContableCompras;" + 
			"centroCostos}" +
		"Impuestos{" +
			"regimenRetencionGanancias; impuestoInterno;}" + 
		"Composicion{" + 
			"composicion;}" +
		"Proveedores{" +
			"proveedor;" + 
			"codigoProveedor}"
		),
	@View(name="TransaccionesAsociadas",
		members="codigo, nombre;" +
				"pedidos{pedidosVenta}" + 
				"facturacion{facturasCreditosDebitos}" + 
				"remitos{remitos}" + 
				"ajustes{ajustesInventario}"
		),
	@View(name="CarritoCompras", 
		members="codigo, nombre;" +
				"imagen")
})


@Tabs({
	@Tab(properties="codigo, nombre, calculos.stock, calculos.reservado, calculos.pedidos, calculos.comprados, calculos.disponible, tipo, marca.nombre, genero.nombre, linea.nombre, rubro.nombre, familia.nombre, color.nombre"
		),
	@Tab(name="Multiseleccion", 
		properties="codigo, nombre, tipo, codigoAnterior, marca.codigo"),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS),
	@Tab(name="CarritoCompras", 
		properties="codigo, imagen.foto"),
	@Tab(name="ImportacionCSV", 
		properties="codigo, nombre, tasaIva.porcentaje, marca.codigo, genero.codigo, linea.codigo, rubro.codigo, familia.codigo, color.codigo, fabricante.codigo, " +
				"cuentaContableVentas.codigo, cuentaContableCompras.codigo, regimenRetencionGanancias.codigo, productoNumero1, productoNumero2, productoNumero3, " + 
				"cajaProducto.codigo, codigoAnterior, proveedor.codigo, codigoProveedor, subfamilia.codigo, modelo.codigo, categoria.codigo, impuestoInterno.codigo, " +
				"despacho, lote")	
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Producto"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	),
	@EntityValidator(
			value=ProductoValidator.class, 
			properties= {
				@PropertyValue(name="tipo"), 
				@PropertyValue(name="lote"),
				@PropertyValue(name="despacho")				
			}
	)
})


public class Producto extends ObjetoEstatico{

	public static Producto buscarInteres() {
		Query query = XPersistence.getManager().createQuery("from Producto where tipo = :interes");
		query.setParameter("interes", TipoProducto.Interes);
		query.setFlushMode(FlushModeType.COMMIT);
		try{
			return (Producto)query.getSingleResult();
		}
		catch(NoResultException e){
			throw new ValidationException("Falta definir un producto de tipo interes");
		}
		catch(NonUniqueResultException e){
			throw new ValidationException("Hay más de un producto de tipo interes");
		}
		
	}
	
	@Column(length=25) 
	@ReadOnly
	private String codigoAnterior;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceViews({
		@ReferenceView(value="Foto", notForViews="CarritoCompras"),
		@ReferenceView(value="FotoSoloLectura", forViews="CarritoCompras"),
	})
	@AsEmbedded
	private ObjetoImagen imagen;
	
	@Required
	private TipoProducto tipo;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
							properties={@PropertyValue(name="entidad", value="UnidadMedida")})
	@NoCreate @NoModify
	private UnidadMedida unidadMedida;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
    @NoCreate @NoModify 
    @ReferenceView("Simple")
    @DefaultValueCalculator(TasaImpuestoIvaDefaultCalculator.class)
    private TasaImpuesto tasaIva;
	
	@NoCreate @NoModify
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Marca marca;
		
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Genero genero;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Linea linea;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Rubro rubro;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Familia familia;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Subfamilia subfamilia;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Color color;

	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Fabricante fabricante;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private CajaProducto cajaProducto;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private CategoriaProducto categoria;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Modelo modelo;
	
	@DefaultValueCalculator(value=ValoresDefectoEsquemaCalculator.class, 
				properties={ @PropertyValue(name="atributo", value="UtilizaDespacho") })
	private Boolean despacho = Boolean.FALSE;
	
	@DefaultValueCalculator(value=ValoresDefectoEsquemaCalculator.class, 
			properties={ @PropertyValue(name="atributo", value="UtilizaLote") })
	private Boolean lote = Boolean.FALSE;
	
	@ManyToMany
	@CollectionView(value="Simple")
	private Collection<UnidadMedida> unidadesMedida;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ValoresDefectoEsquemaCalculator.class, 
							properties={ @PropertyValue(name="atributo", value="CuentaContableVentasProducto") })
	private CuentaContable cuentaContableVentas;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ValoresDefectoEsquemaCalculator.class, 
							properties={ @PropertyValue(name="atributo", value="CuentaContableComprasProducto") })
	private CuentaContable cuentaContableCompras;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean compras = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean ventas = Boolean.TRUE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@SearchListCondition("${tipo} = 3")
	@ReferenceView("Simple")
	@DefaultValueCalculator(value=ValoresDefectoEsquemaCalculator.class, 
		properties={ @PropertyValue(name="atributo", value="RetGananciasProducto") })
	private Impuesto regimenRetencionGanancias;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@SearchListCondition("${tipo} = 9")
	@ReferenceView("Simple")
	private Impuesto impuestoInterno;
	
	@OneToMany(mappedBy="producto", cascade=CascadeType.ALL)
	@ListProperties("componente.codigo, componente.activo, componente.nombre, cantidad, unidadMedida.codigo")
	private Collection<ComponenteProducto> composicion;
	
	private BigDecimal productoNumero1;
	
	private BigDecimal productoNumero2;
	
	private BigDecimal productoNumero3;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Proveedor proveedor;
	
	@Column(length=25) 
	private String codigoProveedor;
	
	@OneToOne(optional=true, fetch=FetchType.LAZY, targetEntity=MetricasProducto.class, mappedBy="producto")
	@ReadOnly
	@NoFrame
	@ReferenceView("Producto")
	private MetricasProducto calculos;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CentroCostos centroCostos;
	
	public ObjetoImagen getImagen() {
		return imagen;
	}

	public void setImagen(ObjetoImagen imagen) {
		this.imagen = imagen;
	}

	public BigDecimal getProductoNumero1() {
		return productoNumero1;
	}

	public void setProductoNumero1(BigDecimal productoNumero1) {
		this.productoNumero1 = productoNumero1;
	}

	public BigDecimal getProductoNumero2() {
		return productoNumero2;
	}

	public void setProductoNumero2(BigDecimal productoNumero2) {
		this.productoNumero2 = productoNumero2;
	}

	public BigDecimal getProductoNumero3() {
		return productoNumero3;
	}

	public void setProductoNumero3(BigDecimal productoNumero3) {
		this.productoNumero3 = productoNumero3;
	}

	public Impuesto getRegimenRetencionGanancias() {
		return regimenRetencionGanancias;
	}

	public void setRegimenRetencionGanancias(Impuesto regimenRetencionGanancias) {
		this.regimenRetencionGanancias = regimenRetencionGanancias;
	}

	public Impuesto getImpuestoInterno() {
		return impuestoInterno;
	}

	public void setImpuestoInterno(Impuesto impuestoInterno) {
		this.impuestoInterno = impuestoInterno;
	}

	public TipoProducto getTipo() {
		return tipo == null ? TipoProducto.Producto : this.tipo;
	}

	public void setTipo(TipoProducto tipo) {
		this.tipo = tipo;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public TasaImpuesto getTasaIva() {
		return tasaIva;
	}

	public void setTasaIva(TasaImpuesto tasaIva) {
		this.tasaIva = tasaIva;
	}
	
	public Marca getMarca() {
		return marca;
	}
	
	public Genero getGenero() {
		return genero;
	}

	public void setGenero(Genero genero) {
		this.genero = genero;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	public Linea getLinea() {
		return linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	public Rubro getRubro() {
		return rubro;
	}

	public void setRubro(Rubro rubro) {
		this.rubro = rubro;
	}

	public Familia getFamilia() {
		return familia;
	}

	public void setFamilia(Familia familia) {
		this.familia = familia;
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Fabricante getFabricante() {
		return fabricante;
	}

	public void setFabricante(Fabricante fabricante) {
		this.fabricante = fabricante;
	}

	public CajaProducto getCajaProducto() {
		return cajaProducto;
	}

	public void setCajaProducto(CajaProducto cajaProducto) {
		this.cajaProducto = cajaProducto;
	}

	public Boolean getCompras() {
		return compras;
	}

	public void setCompras(Boolean compras) {
		this.compras = compras;
	}

	public Boolean getVentas() {
		return ventas;
	}

	public void setVentas(Boolean ventas) {
		this.ventas = ventas;
	}

	@SuppressWarnings("unchecked")
	public Collection<ComponenteProducto> getComposicion() {
		return composicion == null ? Collections.EMPTY_LIST : this.composicion;
	}

	public void setComposicion(Collection<ComponenteProducto> composicion) {
		this.composicion = composicion;
	}

	/*
	@Transient
	private BigDecimal stockTemp = null;
	
	@Transient
	private BigDecimal reservadoTemp = null;
	
	private void buscarStock(){
		if ((stockTemp == null) || (stockTemp == null)){
			this.stockTemp = BigDecimal.ZERO;
			this.reservadoTemp = BigDecimal.ZERO;
			if (!Is.emptyString(this.getId())){
				String sql = "select coalesce(sum(i.stock), 0) stock, coalesce(sum(i.reservado), 0) reservado from " + Esquema.concatenarEsquema("inventario") + " i " +
							 "join " + Esquema.concatenarEsquema("deposito") + " d on d.id = i.deposito_id and d.participaDisponible = :participa " + 
							 "where producto_id = :producto";
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("producto", this.getId());
				query.setParameter("participa", Boolean.TRUE);
				Object[] result = (Object[])query.getSingleResult();
				this.stockTemp = (BigDecimal)result[0];
				this.reservadoTemp = (BigDecimal)result[1];
			}			
		}
	}
	
	public BigDecimal getStock(){
		if (this.getTipo().equals(TipoProducto.Producto)){
			this.buscarStock();
			return this.stockTemp;
		}
		else{
			return BigDecimal.ZERO;
		}
	}
		
	public BigDecimal getReservado(){
		if (this.getTipo().equals(TipoProducto.Producto)){
			this.buscarStock();
			return this.reservadoTemp;
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	
	@Transient
	BigDecimal pedidosTemp = null;
	
	public BigDecimal getPedidos(){
		if (this.getTipo().equals(TipoProducto.Producto)){
			if (pedidosTemp == null){
				this.pedidosTemp = BigDecimal.ZERO;
				if (!Is.emptyString(this.getId())){
					String sql = "select coalesce(sum(i.pendientePreparacion * i.equivalencia), 0) stock from " + Esquema.concatenarEsquema("estadisticapedidoventa") + " i " +
							   "join " + Esquema.concatenarEsquema("pedidoventa") + " p on p.id = i.venta_id and p.estado = :estado " +
							   "where i.pendientePreparacion > 0 and i.producto_id = :producto"; 
					
					Query query = XPersistence.getManager().createNativeQuery(sql);
					query.setParameter("producto", this.getId());
					query.setParameter("estado", Estado.Confirmada.ordinal());
					this.pedidosTemp = (BigDecimal)query.getSingleResult();	   
				}
			}
			return pedidosTemp;		 
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	/*
	@Transient
	private BigDecimal stockOrdenCompra = null;
	
	public BigDecimal getComprados(){
		if (this.getTipo().equals(TipoProducto.Producto)){
			if (stockOrdenCompra == null){
				stockOrdenCompra = BigDecimal.ZERO;
				if (!Is.emptyString(this.getId())){
					String sql = "select coalesce(sum(case when o.estado = 1 then i.pendienteRecepcion * i.equivalencia else i.cantidad * i.equivalencia end), 0) proyectado " + 
							"from {h-schema}itemordencompra i " + 
							"join {h-schema}ordencompra o on o.id = i.ordencompra_id and o.participaStock = :participa " + 
							"where i.producto_id = :producto and o.estado != :anulado and o.estado != :cancelado";
					Query query = XPersistence.getManager().createNativeQuery(sql);				
					query.setFlushMode(FlushModeType.COMMIT);
					query.setParameter("producto", this.getId());
					query.setParameter("anulado", Estado.Anulada.ordinal());
					query.setParameter("cancelado", Estado.Cancelada.ordinal());
					query.setParameter("participa", Boolean.TRUE);
					this.stockOrdenCompra = (BigDecimal) query.getSingleResult();
				}
			}
			return stockOrdenCompra;
		}
		else{
			return BigDecimal.ZERO;		
		}
	}
	
	public BigDecimal getDisponible(){
		if (this.getTipo().equals(TipoProducto.Producto)){
			return this.getStock().subtract(this.getReservado()).subtract(this.getPedidos()).add(this.getComprados());
		}
		else{
			return BigDecimal.ZERO;
		}
	}*/
	
	public Boolean getDespacho() {
		return despacho == null ? Boolean.FALSE: this.despacho;
	}
	
	public void setDespacho(Boolean despacho) {
		this.despacho = despacho;
	}
	
	public Boolean getLote() {
		return lote == null ? Boolean.FALSE: this.lote;
	}

	public void setLote(Boolean lote) {
		this.lote = lote;
	}

	public CuentaContable getCuentaContableVentas() {
		return cuentaContableVentas;
	}

	public void setCuentaContableVentas(CuentaContable cuentaContableVentas) {
		this.cuentaContableVentas = cuentaContableVentas;
	}

	public CuentaContable getCuentaContableCompras() {
		return cuentaContableCompras;
	}

	public void setCuentaContableCompras(CuentaContable cuentaContableCompras) {
		this.cuentaContableCompras = cuentaContableCompras;
	}

	public boolean stockObligatorio() {
		if (this.getDespacho() || this.getLote()){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Collection<UnidadMedida> getUnidadesMedida() {
		return unidadesMedida;
	}

	public void setUnidadesMedida(Collection<UnidadMedida> unidadesMedida) {
		this.unidadesMedida = unidadesMedida;
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("unidadMedida");
		propiedadesSoloLectura.add("despacho");
		propiedadesSoloLectura.add("lote");
	}
	
	@Condition(
			"${producto.id} = ${this.id}"
	)
	@ReadOnly
	@ListProperties("despacho.codigo, deposito.nombre, stock, reservado, disponible")
	public Collection<Inventario> getDespachos(){
		return null;
	}

	public Lote loteMasViejo(String idDeposito) {
		if (this.getLote()){
			Query query = XPersistence.getManager().createQuery("from Inventario i where i.producto.id = :producto and i.deposito.id = :deposito and i.stock > 0 order by i.lote.fechaVencimiento asc");
			query.setParameter("producto", this.getId());
			query.setParameter("deposito", idDeposito);
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return ((Inventario)result.get(0)).getLote();
			}
			else{
				return null;
			}
		}
		else{
			return null;
		}
	}
		
	public DespachoImportacion ultimoDespacho(String idDeposito) {
		if (this.getDespacho()){ 
			Query query = XPersistence.getManager().createQuery("from Inventario i where i.producto.id = :producto and i.deposito.id = :deposito order by i.despacho.fechaCreacion desc");
			query.setParameter("producto", this.getId());
			query.setParameter("deposito", idDeposito);
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return ((Inventario)result.get(0)).getDespacho();
			}
			else{
				return null;
			}
		}
		else{
			return null;
		}		
	}
	
	public DespachoImportacion ultimoDespachoGeneral(){
		if (this.getDespacho()){ 
			Query query = XPersistence.getManager().createQuery("from Inventario i where i.producto.id = :producto order by i.despacho.fechaCreacion desc");
			query.setParameter("producto", this.getId());			
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return ((Inventario)result.get(0)).getDespacho();
			}
			else{
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	@OneToMany(mappedBy="producto")
	@ReadOnly
	@ListProperties("venta.fecha, venta.numero, venta.empresa.nombre, venta.cliente.codigo, venta.cliente.nombre, cantidad, precioUnitario, suma2, suma2, venta.usuario, venta.fechaCreacion")
	@OrderBy("venta.fecha desc")
	private Collection<EstadisticaPedidoVenta> pedidosVenta;
	
	@OneToMany(mappedBy="producto")
	@ReadOnly
	@ListProperties("venta.fecha, venta.numero, venta.tipoOperacion, venta.empresa.nombre, venta.cliente.codigo, venta.cliente.nombre, cantidad, precioUnitario, subtotal1, subtotal2, venta.usuario, venta.fechaCreacion")
	@OrderBy("venta.fecha desc")
	private Collection<ItemVentaElectronica> facturasCreditosDebitos;
	
	@OneToMany(mappedBy="producto")
	@ReadOnly
	@ListProperties("remito.fecha, remito.numero, remito.empresa.nombre, remito.cliente.codigo, remito.cliente.nombre, cantidad, remito.usuario, remito.fechaCreacion")
	@OrderBy("remito.fecha desc")
	private Collection<ItemRemito> remitos;
	
	@OneToMany(mappedBy="producto")
	@ReadOnly
	@ListProperties("ajusteInventario.fecha, ajusteInventario.numero, ajusteInventario.empresa.nombre, cantidad, ajusteInventario.usuario, ajusteInventario.fechaCreacion")
	@OrderBy("ajusteInventario.fecha desc")
	private Collection<ItemAjusteInventario> ajustesInventario;
	
	public Collection<ItemVentaElectronica> getFacturasCreditosDebitos() {
		return facturasCreditosDebitos;
	}

	public void setFacturasCreditosDebitos(Collection<ItemVentaElectronica> facturasCreditosDebitos) {
		this.facturasCreditosDebitos = facturasCreditosDebitos;
	}

	public Collection<EstadisticaPedidoVenta> getPedidosVenta() {
		return pedidosVenta;
	}

	public void setPedidosVenta(Collection<EstadisticaPedidoVenta> pedidosVenta) {
		this.pedidosVenta = pedidosVenta;
	}

	public Collection<ItemRemito> getRemitos() {
		return remitos;
	}

	public void setRemitos(Collection<ItemRemito> remitos) {
		this.remitos = remitos;
	}

	public Collection<ItemAjusteInventario> getAjustesInventario() {
		return ajustesInventario;
	}

	public void setAjustesInventario(Collection<ItemAjusteInventario> ajustesInventario) {
		this.ajustesInventario = ajustesInventario;
	}
	
	public void BOM(Collection<Producto> componentes){
		if (this.getComposicion() != null){
			Map<String, Object> productosProcesados = new HashMap<String, Object>();
			componentes.add(this);
			productosProcesados.put(this.getId(), null);
			for (ComponenteProducto componente: this.getComposicion()){
				agregarComponenteABOM(componente.getComponente(), componentes, productosProcesados);
			}
		}
	}
	
	private void agregarComponenteABOM(Producto componente, Collection<Producto> estructuraBOM, Map<String, Object> productosProcesados){
		if (!productosProcesados.containsKey(componente.getId())){
			estructuraBOM.add(componente);
			productosProcesados.put(componente.getId(), null);
			if (componente.getComposicion()!= null){
				for(ComponenteProducto comp: componente.getComposicion()){
					agregarComponenteABOM(comp.getComponente(), estructuraBOM, productosProcesados);
				}				
			}
		}
	}
	
	public void BOMNivelesSuperiores(Map<String, Producto>  nivelesSuperiores){
		agregarNivelSuperiorBOM(this, nivelesSuperiores);
	}
	
	private void agregarNivelSuperiorBOM(Producto producto, Map<String, Producto> productosProcesados){
		Query query = XPersistence.getManager().createQuery("from ComponenteProducto where componente.id = :id");
		query.setParameter("id", producto.getId());
		List<?> componentes = query.getResultList();
		for (Object componente: componentes){
			Producto prodNivelSuperior = ((ComponenteProducto)componente).getComponente();
			if (!productosProcesados.containsKey(prodNivelSuperior.getId())){
				productosProcesados.put(prodNivelSuperior.getId(), prodNivelSuperior);
				this.agregarNivelSuperiorBOM(prodNivelSuperior, productosProcesados);
			}
		}
	}
	
	public void explotarComponentes(Collection<ComponenteProducto> componentes, Cantidad cantidad){
		Map<String, Object> procesados = new HashMap<String, Object>();
		procesados.put(this.getId(), null);
		explotarComponentesProducto(this, cantidad, componentes, procesados);		
	}
	
	private void explotarComponentesProducto(Producto producto, Cantidad cantidad, Collection<ComponenteProducto> componentes, Map<String, Object> procesados){
		if (!producto.getComposicion().isEmpty()){
			BigDecimal cantidadUnidadMedidaProducto = cantidad.convertir(producto.getUnidadMedida());			
			for(ComponenteProducto componente: producto.getComposicion()){
				
				BigDecimal cantidadComponente = componente.getCantidad().multiply(cantidadUnidadMedidaProducto);
				if (componente.getComponente().getComposicion().isEmpty()){
										
					// se crea un objeto para el componente del nivel inferior, con las cantidades que corresponde
					ComponenteProducto componenteNivelInferior = new ComponenteProducto();
					componenteNivelInferior.copiarPropiedades(componente);
					componenteNivelInferior.setCantidad(cantidadComponente);
					
					fusionarComponentes(componentes, componenteNivelInferior);
				}
				else if (!procesados.containsKey(componente.getComponente().getId())){
					// Procesados para evitar una recursividad infinita. 
					// Aunque esta validado que no se graben estructuras circulares, es un cheque preventivo.
					procesados.containsKey(componente.getComponente().getId());
					
					Cantidad q = new Cantidad();
					q.setUnidadMedida(componente.getUnidadMedida());
					q.setCantidad(cantidadComponente);
					explotarComponentesProducto(componente.getComponente(), q, componentes, procesados);
					
					procesados.remove(componente.getComponente().getId());
				}
			}
		}		
	}
	
	private void fusionarComponentes(Collection<ComponenteProducto> componentes, ComponenteProducto componenteAFusionar){
		boolean fusionado = false;
		for(ComponenteProducto componente: componentes){
			if ((componente.getComponente().equals(componenteAFusionar.getComponente())) && 
				(componente.getUnidadMedida().equals(componenteAFusionar.getUnidadMedida()))){
				// se puede fusionar, mismo producto misma unidad de medida
				componente.setCantidad(componente.getCantidad().add(componenteAFusionar.getCantidad()));
				fusionado = true;
				break;
			}
		}
		
		if (!fusionado){
			componentes.add(componenteAFusionar);
		}
	}

	public String getCodigoAnterior() {
		return codigoAnterior;
	}

	public void setCodigoAnterior(String codigoAnterior) {
		this.codigoAnterior = codigoAnterior;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	public String getCodigoProveedor() {
		return codigoProveedor;
	}

	public void setCodigoProveedor(String codigoProveedor) {
		this.codigoProveedor = codigoProveedor;
	}

	public MetricasProducto getCalculos() {
		return calculos;
	}

	public void setCalculos(MetricasProducto calculos) {
		this.calculos = calculos;
	}

	public Subfamilia getSubfamilia() {
		return subfamilia;
	}

	public void setSubfamilia(Subfamilia subfamilia) {
		this.subfamilia = subfamilia;
	}

	public CategoriaProducto getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaProducto categoria) {
		this.categoria = categoria;
	}

	public Modelo getModelo() {
		return modelo;
	}

	public void setModelo(Modelo modelo) {
		this.modelo = modelo;
	}
	
	public boolean usaAtributoInventario() {
		return getDespacho() || getLote();
	}

	public boolean unidadMedidaPermitida(UnidadMedida unidad) {
		boolean permite = false;
		if (this.getUnidadMedida().equals(unidad)){
			return true;
		}
		else if (this.getUnidadesMedida() != null){
			permite = this.getUnidadesMedida().contains(unidad);
		}
		return permite;
	}

	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
	
	public BigDecimal agregarIva(BigDecimal importe){
		BigDecimal importeMasIva = importe;
		BigDecimal porcentajeIva = this.getTasaIva().getPorcentaje();
		if (porcentajeIva.compareTo(BigDecimal.ZERO) > 0) {
			// se agrega el iva
			importeMasIva = importe.add(importe.multiply(porcentajeIva)
					.divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
		}
		return importeMasIva;
	}
}
