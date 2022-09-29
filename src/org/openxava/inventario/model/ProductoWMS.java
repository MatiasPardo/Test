package org.openxava.inventario.model;

import java.math.BigDecimal;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.clasificadores.model.Clasificador;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Tab(properties="producto.codigo, producto.nombre, fechaCreacion, usuario")

@Views({
	@View(members="Principal{" +	
			"producto;" +
			"Ventas[unidadesMinimasVenta];" +
			"compras;" +
			"Embalaje[" + 
				"bulto;" +
				"largoBulto, anchoBulto, profundidadBulto;" +
				"peso, pesoTerminada;" + 
			"]" + 
			"Clasificadores[productowmsClasificador1, productowmsClasificador2, productowmsClasificador3;" +
				"productowmsClasificador4, productowmsClasificador5, productowmsClasificador6;" + 
			"];" +
		"}" 	
	),
	@View(name="Simple", members="producto")
})

public class ProductoWMS extends ObjetoNegocio{

	public static ProductoWMS buscarProducto(String idProducto) {

		Query query = XPersistence.getManager().createQuery("from ProductoWMS where producto.id = :idProducto");
		query.setMaxResults(1);
		query.setParameter("idProducto", idProducto);
		query.setFlushMode(FlushModeType.COMMIT);
		try{
			return (ProductoWMS) query.getSingleResult();
		}catch (Exception e){
			return null;
		}
		
	}
	
	public static ProductoWMS buscarPorCodigoProducto(String codigoProducto) {
		Query query = XPersistence.getManager().createQuery("from ProductoWMS where producto.codigo = :codigo");
		query.setMaxResults(1);
		query.setParameter("codigo", codigoProducto);
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		try{
			return (ProductoWMS) query.getSingleResult();
		}catch (Exception e){
			throw new ValidationException("No esta definido " + codigoProducto);
		}		
	}
	
	@OneToOne(optional=false, fetch=FetchType.LAZY, orphanRemoval=false)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private Producto producto;
	
	private BigDecimal unidadesMinimasVenta;
	
	@OneToMany(mappedBy="producto", fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
	@ListProperties("sucursal.nombre, stockMinimo, loteCompra")
	private Collection<ProductoComprasWMS> compras;
	
	private Integer bulto;
	
	private BigDecimal largoBulto;
	
	private BigDecimal anchoBulto;
	
	private BigDecimal profundidadBulto;
	
	private BigDecimal peso;
	
	private BigDecimal pesoTerminada;

	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador1;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 2 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador2;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 3 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador3;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 4 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador4;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 5 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador5;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 6 and ${tipoClasificador.modulo} = 'ProductoWMS' and " + Clasificador.CONDICION)
	private Clasificador productowmsClasificador6;
	
		
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public BigDecimal getUnidadesMinimasVenta() {
		return unidadesMinimasVenta;
	}

	public void setUnidadesMinimasVenta(BigDecimal unidadesMinimasVenta) {
		this.unidadesMinimasVenta = unidadesMinimasVenta;
	}

	public Integer getBulto() {
		return bulto;
	}

	public void setBulto(Integer bulto) {
		this.bulto = bulto;
	}

	public BigDecimal getLargoBulto() {
		return largoBulto;
	}

	public void setLargoBulto(BigDecimal largoBulto) {
		this.largoBulto = largoBulto;
	}

	public BigDecimal getAnchoBulto() {
		return anchoBulto;
	}

	public void setAnchoBulto(BigDecimal anchoBulto) {
		this.anchoBulto = anchoBulto;
	}

	public BigDecimal getProfundidadBulto() {
		return profundidadBulto;
	}

	public void setProfundidadBulto(BigDecimal profundidadBulto) {
		this.profundidadBulto = profundidadBulto;
	}

	public BigDecimal getPeso() {
		return peso;
	}

	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}

	public BigDecimal getPesoTerminada() {
		return pesoTerminada;
	}

	public void setPesoTerminada(BigDecimal pesoTerminada) {
		this.pesoTerminada = pesoTerminada;
	}

	public Clasificador getProductowmsClasificador1() {
		return productowmsClasificador1;
	}

	public void setProductowmsClasificador1(Clasificador productowmsClasificador1) {
		this.productowmsClasificador1 = productowmsClasificador1;
	}

	public Clasificador getProductowmsClasificador2() {
		return productowmsClasificador2;
	}

	public void setProductowmsClasificador2(Clasificador productowmsClasificador2) {
		this.productowmsClasificador2 = productowmsClasificador2;
	}

	public Clasificador getProductowmsClasificador3() {
		return productowmsClasificador3;
	}

	public void setProductowmsClasificador3(Clasificador productowmsClasificador3) {
		this.productowmsClasificador3 = productowmsClasificador3;
	}

	public Collection<ProductoComprasWMS> getCompras() {
		return compras;
	}

	public void setCompras(Collection<ProductoComprasWMS> compras) {
		this.compras = compras;
	}

	public Clasificador getProductowmsClasificador4() {
		return productowmsClasificador4;
	}

	public void setProductowmsClasificador4(Clasificador productowmsClasificador4) {
		this.productowmsClasificador4 = productowmsClasificador4;
	}

	public Clasificador getProductowmsClasificador5() {
		return productowmsClasificador5;
	}

	public void setProductowmsClasificador5(Clasificador productowmsClasificador5) {
		this.productowmsClasificador5 = productowmsClasificador5;
	}

	public Clasificador getProductowmsClasificador6() {
		return productowmsClasificador6;
	}

	public void setProductowmsClasificador6(Clasificador productowmsClasificador6) {
		this.productowmsClasificador6 = productowmsClasificador6;
	}	
}
