package org.openxava.inventario.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoFrame;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Tabs({
	@Tab(properties="articulo.codigo, articulo.nombre, sucursal.nombre, stockMinimo, loteCompra")	
})


@View(members="producto;" + 
		"stockMinimo, loteCompra;" + 
		"sucursal;")

public class ProductoComprasWMS extends ObjetoNegocio{

	public static ProductoComprasWMS buscar(String codigoProducto, String codigoSucursal) {
		if (Is.emptyString(codigoProducto)){
			throw new ValidationException("Falta asignar código de producto");
		}
		if (Is.emptyString(codigoSucursal)){
			throw new ValidationException("Falta asignar código de sucursal");
		}
		Query query = XPersistence.getManager().createQuery("from ProductoComprasWMS where producto.producto.codigo = :producto and sucursal.codigo = :sucursal");
		query.setParameter("producto", codigoProducto);
		query.setParameter("sucursal", codigoSucursal);
		query.setFlushMode(FlushModeType.COMMIT);
		try{
			return (ProductoComprasWMS)query.getSingleResult();
		}
		catch(NonUniqueResultException e){
			throw new ValidationException("Hay mas de uno definido: código de producto " + codigoProducto + " y la sucursal " + codigoSucursal);
		}
		catch(NoResultException e){
			return null;
		}
	}
	
	@ReferenceView("Simple")
	@NoCreate @NoModify
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoFrame
	private ProductoWMS producto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Producto articulo;
	
	private BigDecimal stockMinimo;
	
	private BigDecimal loteCompra;

	@NoCreate @NoModify
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Sucursal sucursal;
	
	public ProductoWMS getProducto() {
		return producto;
	}

	public void setProducto(ProductoWMS producto) {
		this.producto = producto;
	}

	public BigDecimal getStockMinimo() {
		return stockMinimo;
	}

	public void setStockMinimo(BigDecimal stockMinimo) {
		this.stockMinimo = stockMinimo;
	}

	public BigDecimal getLoteCompra() {
		return loteCompra;
	}

	public void setLoteCompra(BigDecimal loteCompra) {
		this.loteCompra = loteCompra;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Producto getArticulo() {
		return articulo;
	}

	public void setArticulo(Producto articulo) {
		this.articulo = articulo;
	}

	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		this.sincronizarProducto();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.sincronizarProducto();
	}
	
	private void sincronizarProducto() {
		if (this.getProducto() != null){
			this.setArticulo(this.getProducto().getProducto());
		}		
	}
}
