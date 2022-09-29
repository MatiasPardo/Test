package org.openxava.inventario.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;

@Entity

@Views({
	@View(members="codigo, codigoInterno; fechaCreacion; usuario; aduana; origen; trazabilidad"),	
	@View(name="Simple",
		members="codigo"),
	@View(name="Inventario", 
		members="codigo; trazabilidad"),
})

@Tab(name="ImportacionCSV",
	properties="codigo, codigoInterno, aduana.codigo, origen.codigo")

public class DespachoImportacion extends ObjetoNegocio{
	
	public static DespachoImportacion buscar(String codigoDespacho) {
		Query query = XPersistence.getManager().createQuery("from " + DespachoImportacion.class.getSimpleName() + " where codigo = :codigo");
		query.setParameter("codigo", codigoDespacho);
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (DespachoImportacion)query.getResultList().get(0);
		}
		else{
			return null;
		}		
	}
	
	@Column(length=50) @Required
	@SearchKey	
    private String codigo;
	
	@Column(length=25)
	private String codigoInterno;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoModify @NoCreate
	private Aduana aduana;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoModify @NoCreate
	private Pais origen;
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public Aduana getAduana() {
		return aduana;
	}

	public void setAduana(Aduana aduana) {
		this.aduana = aduana;
	}

	public Pais getOrigen() {
		return origen;
	}

	public void setOrigen(Pais origen) {
		this.origen = origen;
	}



	@OneToMany(mappedBy="despacho", fetch=FetchType.LAZY)
	@ReadOnly
	@ListProperties("fechaCreacion, fechaComprobante, tipoComprobante, numero, producto.codigo, producto.nombre, cantidad, deposito.nombre")
	@OrderBy("fechaCreacion desc")
	public Collection<Kardex> trazabilidad;

	public Collection<Kardex> getTrazabilidad() {
		return trazabilidad;
	}

	public void setTrazabilidad(Collection<Kardex> trazabilidad) {
		this.trazabilidad = trazabilidad;
	}

	public String getCodigoInterno() {
		return codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}
}
