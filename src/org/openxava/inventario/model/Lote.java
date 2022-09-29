package org.openxava.inventario.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Required;
import org.openxava.annotations.SearchKey;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.inventario.validators.LoteValidator;
import org.openxava.jpa.XPersistence;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members="codigo; fechaVencimiento; producto"),
	@View(name="Simple", members="codigo, fechaVencimiento"),
	@View(name="Inventario", members="codigo, fechaVencimiento"),
})

@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"codigo", "producto_id"})
})

@EntityValidator(
	value=LoteValidator.class, 
	properties= {
		@PropertyValue(name="producto"), 
	}
)

@Tabs({
	@Tab(name="ImportacionCSV", 
			properties="codigo, fechaVencimiento, producto.codigo, producto.nombre, fechaCreacion")
})

public class Lote extends ObjetoNegocio{

	public static Lote buscarPorCodigo(String codigoLote, String codigoProducto) {
		Query query = XPersistence.getManager().createQuery("from Lote where codigo = :lote and producto.codigo = :producto");
		query.setParameter("lote", codigoLote);
		query.setParameter("producto", codigoProducto);
		query.setMaxResults(1);
		List<?> res = query.getResultList();
		if (!res.isEmpty()){
			return (Lote)res.get(0);
		}
		else{
			return null;
		}
	}
	
	public static Lote crearLote(String codigoLote, Producto producto, Date fechaVencimiento){
		Lote loteNuevo = new Lote();
		loteNuevo.setCodigo(codigoLote);
		loteNuevo.setProducto(producto);
		loteNuevo.setFechaVencimiento(fechaVencimiento);
		XPersistence.getManager().persist(loteNuevo);
		return loteNuevo;
	}
	
	@Column(length=50) 
	@Required
	@SearchKey
    private String codigo;
	
	@Required
	@DefaultValueCalculator(CurrentDateCalculator.class)
	private Date fechaVencimiento;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Producto producto;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}
	
	public Boolean soloLectura(){
		return !this.esNuevo();
	}	
}
