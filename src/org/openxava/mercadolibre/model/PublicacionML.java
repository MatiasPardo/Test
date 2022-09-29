package org.openxava.mercadolibre.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.Mercadolibre.*;
import org.openxava.annotations.*;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.validators.PublicacionMLValidator;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Table(
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"idMercadoLibre", "idProducto", "tipoEcommerce"})
	)

@Views({
	@View(members="tipoEcommerce, idProducto; " +
			"idMercadoLibre, estado;" + 
			"producto;" + 
			"fechaCreacion, usuario, configuracionEcommerce;" + 
			"AuditoriaPrecio[" +
			 "fechaActualizacionPrecio, usuarioActualizacionPrecio; ultimoPrecio];" +
			 "AuditoriaStock[" +
				 "fechaActualizacionStock, usuarioActualizacionStock; ultimoStock];"
			),
	@View(name="Simple", 
			members="idMercadoLibre, idProducto"),
	@View(name="Mercadolibre", members="tipoEcommerce; " + 
			"idMercadoLibre, estado;" + 
			"producto;" + 
			"fechaCreacion, usuario;")
})


@Tab(properties="idMercadoLibre, producto.codigo, producto.nombre, tipoEcommerce, estado, fechaCreacion")

@EntityValidator(
	value=PublicacionMLValidator.class, 
	properties= {
		@PropertyValue(name="idMercadoLibre"), 				
		@PropertyValue(name="producto"),
		@PropertyValue(name="estado"),
		@PropertyValue(name="id"),
		@PropertyValue(name="idProducto")
	})

public class PublicacionML extends ObjetoNegocio{
		
	public static PublicacionML buscar(String idItem, Ecommerce tipoEcommerce) {
		Query query = XPersistence.getManager().createQuery("from PublicacionML where idMercadoLibre = :id and tipoEcommerce =:tipoEcommerce ");  
		query.setParameter("id", idItem);
		query.setParameter("tipoEcommerce", tipoEcommerce);
		try{
			return (PublicacionML)query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No existe la publicacion de id " + idItem);
		}
	}
	
	public static PublicacionML buscarActiva(MLItemPedido item, Ecommerce tipoEcommerce) {
		StringBuilder sql = new StringBuilder("from PublicacionML where idMercadoLibre = :id and tipoEcommerce =:tipoEcommerce "
				+ "and estado =:estadoPublicacion");
		if(item.getIdVariante() != null){
			sql.append(" and idProducto = :idProducto");
		}
		Query query = XPersistence.getManager().createQuery(sql.toString());
		query.setParameter("id", item.getIdItem());
		query.setParameter("tipoEcommerce", tipoEcommerce);
		query.setParameter("estadoPublicacion", EstadoPublicacionML.Publicada);
		if(item.getIdVariante() != null){
			query.setParameter("idProducto", item.getIdVariante());
		}
		try{
			return (PublicacionML)query.getSingleResult();
		}
		catch(Exception e){
			if(item.getIdVariante() == null){
				throw new ValidationException("No existe la publicacion de id " + item.getIdItem());
			}else{
				throw new ValidationException("No existe la publicacion de id " + item.getIdItem() 
				+ " y id de variante: " + item.getIdVariante());
			}
		}
	}
	
	public static PublicacionML buscarSinVariante(MLItemPedido itemMercadoLibre, Ecommerce mercadolibre) {
		Query query = XPersistence.getManager().createQuery("from PublicacionML where idMercadoLibre = :id and tipoEcommerce =:tipoEcommerce"
															+ " and estado =:estadoPublicacion and (idProducto is null or idProducto = :idProducto)");
		query.setParameter("id", itemMercadoLibre.getIdItem());
		query.setParameter("tipoEcommerce", mercadolibre);
		query.setParameter("estadoPublicacion", EstadoPublicacionML.Publicada);
		query.setParameter("idProducto", "");
		try{
			return (PublicacionML)query.getSingleResult();
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static PublicacionML crearPublicacion(String idML, Producto prod, ConfiguracionMercadoLibre configurador, String idVariante) {
		Query query = XPersistence.getManager().createQuery("from PublicacionML where idMercadoLibre = :id and idProducto = :idVariante");
		query.setParameter("id", idML);
		if (idVariante != null){
			query.setParameter("idVariante", idVariante);
		}
		else {
			query.setParameter("idVariante", "");
		}
		List<?> results = query.getResultList();
		if (!results.isEmpty() ){
			if(idVariante == null){
				
				throw new ValidationException("Ya existe la publicación " + idML);

			}else{
				throw new ValidationException("Ya existe la publicación " + idML + " con id variante: " + idVariante);
			}
		}
		
		PublicacionML nueva = new PublicacionML();
		nueva.setProducto(prod);
		nueva.setIdMercadoLibre(idML);
		nueva.setTipoEcommerce(configurador.getEcommerce());
		nueva.setConfiguracionEcommerce(configurador);
		nueva.setIdProducto(idVariante);
		XPersistence.getManager().persist(nueva);
		return nueva;
	}
	
	@Column(length=25)
	private String idMercadoLibre;
	
	@Required
	private Ecommerce tipoEcommerce;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify 
	@ReferenceView("Simple")
	@NoFrame
	private Producto producto;
	
	@Column(length=25)
	private String idProducto;
	
	@Required
	private EstadoPublicacionML estado = EstadoPublicacionML.Publicada;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify 
	@DescriptionsList(descriptionProperties="codigo")
	private ConfiguracionMercadoLibre configuracionEcommerce;

	public ConfiguracionMercadoLibre getConfiguracionEcommerce() {
		return configuracionEcommerce;
	}

	public void setConfiguracionEcommerce(ConfiguracionMercadoLibre configuracionEcommerce) {
		this.configuracionEcommerce = configuracionEcommerce;
	}

	public String getIdMercadoLibre() {
		return idMercadoLibre;
	}

	public void setIdMercadoLibre(String idMercadoLibre) {
		this.idMercadoLibre = idMercadoLibre;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public EstadoPublicacionML getEstado() {
		return estado;
	}

	public void setEstado(EstadoPublicacionML estado) {
		this.estado = estado;
	}
	
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("idMercadoLibre");		
		propiedadesSoloLectura.add("tipoEcommerce");
		propiedadesSoloLectura.add("idProducto");
	}
	
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("idMercadoLibre");
		propiedadesEditables.add("tipoEcommerce");
		propiedadesEditables.add("idProducto");
	}

	public Ecommerce getTipoEcommerce() {
		return tipoEcommerce;
	}

	public void setTipoEcommerce(Ecommerce tipoEcommerce) {
		this.tipoEcommerce = tipoEcommerce;
	}

	public String getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(String idProducto) {
		this.idProducto = idProducto;
	}
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaActualizacionPrecio = new Date();
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaActualizacionStock = new Date();
	
	@ReadOnly
	@Column(length=30) 
	private String usuarioActualizacionStock;
	
	@ReadOnly
	@Column(length=30) 
	private String usuarioActualizacionPrecio;
	
	@ReadOnly
	private BigDecimal ultimoPrecio;
	
	@ReadOnly
	private BigDecimal ultimoStock;

	public Date getFechaActualizacionPrecio() {
		return fechaActualizacionPrecio;
	}

	public void setFechaActualizacionPrecio(Date fechaActualizacionPrecio) {
		this.fechaActualizacionPrecio = fechaActualizacionPrecio;
	}

	public Date getFechaActualizacionStock() {
		return fechaActualizacionStock;
	}

	public void setFechaActualizacionStock(Date fechaActualizacionStock) {
		this.fechaActualizacionStock = fechaActualizacionStock;
	}

	public String getUsuarioActualizacionStock() {
		return usuarioActualizacionStock;
	}

	public void setUsuarioActualizacionStock(String usuarioActualizacionStock) {
		this.usuarioActualizacionStock = usuarioActualizacionStock;
	}

	public String getUsuarioActualizacionPrecio() {
		return usuarioActualizacionPrecio;
	}

	public void setUsuarioActualizacionPrecio(String usuarioActualizacionPrecio) {
		this.usuarioActualizacionPrecio = usuarioActualizacionPrecio;
	}

	public BigDecimal getUltimoPrecio() {
		return ultimoPrecio;
	}

	public void setUltimoPrecio(BigDecimal ultimoPrecio) {
		this.ultimoPrecio = ultimoPrecio;
	}

	public BigDecimal getUltimoStock() {
		return ultimoStock;
	}

	public void setUltimoStock(BigDecimal ultimoStock) {
		this.ultimoStock = ultimoStock;
	}
}
