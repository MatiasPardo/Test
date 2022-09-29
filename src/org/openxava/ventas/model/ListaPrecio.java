package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.calculators.*;
import org.openxava.ventas.validators.*;

@Entity

@Views({
	@View(members="usuario, fechaCreacion, activo;" + 
			"codigo;" +
			"nombre;" + 
			"moneda, principal, costo;" +
			"Precios{precios}ImportacionCSV{formatoImportacionCSV};"
			),
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
			baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=ListaPrecioValidator.class, 
			properties= {
				@PropertyValue(name="id"),
				@PropertyValue(name="principal", from="principal"),
				@PropertyValue(name="costo", from="costo")
			}
	),
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="ListaPrecio"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	)
})


public class ListaPrecio extends ObjetoEstatico{
	
	public static BigDecimal costoDefault(Transaccion tr, Producto producto, UnidadMedida unidadMedida, BigDecimal cantidad){
		BigDecimal importe = null;
		ListaPrecio lista = ListaPrecio.buscarListaPrecioPrincipal(Boolean.TRUE);
		if (lista != null){
			Precio precio = lista.buscarObjetoPrecio(producto.getId(), unidadMedida.getId(), cantidad);
			if (precio != null){
				importe = tr.convertirImporteEnMonedaTr(lista.getMoneda(), precio.getCosto());
			}
		}
		return importe;
	}
	
	public static ListaPrecio buscarListaPrecioPrincipal(Boolean costo){
		Boolean listaCosto = costo;
		ListaPrecio lista = null;
		if (listaCosto == null) listaCosto = Boolean.FALSE;
		
		if (Esquemas.getEsquemaApp().getListaPrecioUnica()){
			ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
			calculator.setEntidad(ListaPrecio.class.getSimpleName());
			try{
				lista = (ListaPrecio)calculator.calculate();
			}
			catch(Exception e){				
			}
			
		}
		else{
			Query query = XPersistence.getManager().createQuery("from ListaPrecio where costo = :costo and principal = :principal");
			query.setParameter("costo", listaCosto);
			query.setParameter("principal", Boolean.TRUE);
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				lista = (ListaPrecio)result.get(0);
			}
		}	
		return lista;
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
							properties={@PropertyValue(name="entidad", value="Moneda")})
	private Moneda moneda;
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	private Boolean costo = false;
	
	@ReadOnly
	@Hidden
	private Boolean precioBaseCosto = Boolean.FALSE;
	
	@OneToMany(mappedBy="listaPrecio", cascade=CascadeType.REMOVE)
	@ListProperties("producto.codigo, producto.nombre, producto.marca.nombre, precioBase, porcentaje, importe, porCantidad, desde, hasta, unidadMedida.nombre, " +
				"precioAnterior, fechaUltimaModificacion, usuarioModificacion")	
	private Collection<Precio> precios;
	
	@Condition("${listaPrecio.id} = ${this.id}")
	@ListProperties("producto.codigo, precioBase, porcentaje, cantidadDesde, cantidadHasta, unidadMedida.codigo")
	@ReadOnly
	public Collection<Precio> getFormatoImportacionCSV(){
		return null;
	}
	
	public Boolean getCosto() {
		return costo;
	}

	public void setCosto(Boolean costo) {
		this.costo = costo;
	}

	public Collection<Precio> getPrecios() {
		return precios;
	}

	public void setPrecios(Collection<Precio> precios) {
		this.precios = precios;
	}
		
	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public BigDecimal buscarPrecio(String idProducto, String idUnidadMedida, BigDecimal cantidad){
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createQuery("from Precio p where " +  		
				"p.listaPrecio.id = :idListaPrecio AND " + 
				"p.producto.id = :idProducto AND " + 
				"p.unidadMedida.id = :idUnidadMedida AND " +
				"(p.porCantidad = :porCantidad OR " + 
				"(p.desde <= :cantidad AND p.hasta >= :cantidad))");
		query.setParameter("idListaPrecio", this.getId());
		query.setParameter("idProducto", idProducto);
		query.setParameter("porCantidad", false);
		query.setParameter("cantidad", cantidad);
		query.setParameter("idUnidadMedida", idUnidadMedida);
		query.setMaxResults(1);
		try{
	    	Precio precio = (Precio)query.getSingleResult();
	    	return precio.getImporte();
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	return null;
	    }
	}
	
	public Precio buscarObjetoPrecio(String idProducto, String idUnidadMedida, BigDecimal cantidad){
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createQuery("from Precio p where " +  		
				"p.listaPrecio.id = :idListaPrecio AND " + 
				"p.producto.id = :idProducto AND " +
				"p.unidadMedida.id = :idUnidadMedida AND " +
				"(p.porCantidad = :porCantidad OR " + 
				"(p.desde <= :cantidad AND p.hasta >= :cantidad))");
		query.setParameter("idListaPrecio", this.getId());
		query.setParameter("idProducto", idProducto);
		query.setParameter("porCantidad", false);
		query.setParameter("cantidad", cantidad);
		query.setParameter("idUnidadMedida", idUnidadMedida);
		query.setMaxResults(1);
		
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Precio)result.get(0);
		}
		else{
			return null;
		}
	    
	}
	
	public Precio buscarObjetoPrecioPorCantidad(String idProducto, String idUnidadMedida, BigDecimal desde, BigDecimal hasta){
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createQuery("from Precio p where " +  		
				"p.listaPrecio.id = :idListaPrecio AND " + 
				"p.producto.id = :idProducto AND " + 
				"p.unidadMedida.id = :idUnidadMedida AND " +
				"p.porCantidad = :porCantidad AND p.desde = :desde AND p.hasta = :hasta");
		query.setParameter("idListaPrecio", this.getId());
		query.setParameter("idProducto", idProducto);
		query.setParameter("porCantidad", true);		
		query.setParameter("desde", desde);
		query.setParameter("hasta", hasta);
		query.setParameter("idUnidadMedida", idUnidadMedida);
		query.setMaxResults(1);
		try{
	    	Precio precio = (Precio)query.getSingleResult();
	    	return precio;
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	return null;
	    }
	}
	
	public Precio buscarObjetoPrecioSinCantidad(String idProducto, String idUnidadMedida){
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createQuery("from Precio p where " +  		
				"p.listaPrecio.id = :idListaPrecio AND " + 
				"p.producto.id = :idProducto AND " + 
				"p.unidadMedida.id = :idUnidadMedida AND " +
				"p.porCantidad = :porCantidad");
		query.setParameter("idListaPrecio", this.getId());
		query.setParameter("idProducto", idProducto);
		query.setParameter("porCantidad", false);
		query.setParameter("idUnidadMedida", idUnidadMedida);		
		query.setMaxResults(1);
		try{
	    	Precio precio = (Precio)query.getSingleResult();
	    	return precio;
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	return null;
	    }
	}
	
	public static BigDecimal buscarObjetoPrecioPorLista(String idListaPrecio,String idProducto, String idUnidadMedida){
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createNativeQuery(
				"select importe "+
				"from "+Esquema.concatenarEsquema("Precio p where ") +  		
				"p.listaPrecio_id = :idListaPrecio AND " + 
				"p.producto_id = :idProducto AND " + 
				"p.unidadMedida_id = :idUnidadMedida AND " +
				"p.porCantidad = :porCantidad");
		query.setParameter("idListaPrecio", idListaPrecio);
		query.setParameter("idProducto", idProducto);
		query.setParameter("porCantidad", false);
		query.setParameter("idUnidadMedida", idUnidadMedida);		
		query.setMaxResults(1);
		try{
	    	return (BigDecimal) query.getSingleResult();
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	return null;
	    }
	}
	
	public Precio actualizarPrecioPorCantidad(Producto producto, UnidadMedida unidadMedida, BigDecimal importeBase, BigDecimal porcentaje, BigDecimal desde, BigDecimal hasta){
		if (desde.compareTo(hasta) > 0){
			throw new ValidationException("Cantidades mal definidades: " + desde.toString() + " es mayor a " + hasta.toString());
		}
		UnidadMedida uMedida = unidadMedida;
		if (uMedida == null){
			uMedida = producto.getUnidadMedida();
		}
		Precio precio = buscarObjetoPrecioPorCantidad(producto.getId(), uMedida.getId(), desde, hasta);
		if(precio == null){
			precio = this.crearPrecio(producto, unidadMedida);
			precio.setPorCantidad(Boolean.TRUE);
			precio.setDesde(desde);
			precio.setHasta(hasta);
		}
		precio.setPrecioBase(importeBase);
		precio.setPorcentaje(porcentaje);
		PrecioImporteFinalCalculator calculator = new PrecioImporteFinalCalculator();
		calculator.setPorcentaje(porcentaje);
		calculator.setPrecioBase(importeBase);
		try{
			precio.setImporte((BigDecimal)calculator.calculate());
		}
		catch(Exception e){
		}
		
		return precio;
	}
	
	public Precio actualizarPrecio(Producto producto, UnidadMedida unidadMedida, BigDecimal importeBase, BigDecimal porcentaje){
		Precio precio = buscarObjetoPrecioSinCantidad(producto.getId(), unidadMedida.getId());
		if (precio == null){
			precio = this.crearPrecio(producto, unidadMedida);
		}
		precio.setPrecioBase(importeBase);
		precio.setPorcentaje(porcentaje);
		PrecioImporteFinalCalculator calculator = new PrecioImporteFinalCalculator();
		calculator.setPorcentaje(porcentaje);
		calculator.setPrecioBase(importeBase);
		try{
			precio.setImporte((BigDecimal)calculator.calculate());
		}
		catch(Exception e){
		}
		return precio;
	}
	
	public Precio crearPrecio(Producto producto, UnidadMedida unidadMedida){
		Precio precio = new Precio();
		precio.setProducto(producto);
		precio.setListaPrecio(this);
		precio.setPorCantidad(Boolean.FALSE);
		if (unidadMedida != null){
			precio.setUnidadMedida(unidadMedida);
		}
		else{
			precio.setUnidadMedida(producto.getUnidadMedida());
		}
		return precio;
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("moneda");
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Boolean getPrecioBaseCosto() {
		return precioBaseCosto;
	}

	public void setPrecioBaseCosto(Boolean precioBaseCosto) {
		if (precioBaseCosto == null){
			this.precioBaseCosto = Boolean.FALSE;
		}
		else{
			this.precioBaseCosto = precioBaseCosto;
		}
	}
	
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		
		this.setPrecioBaseCosto(Esquemas.getEsquemaApp().getListaPrecioUnica());
	}
}
