package org.openxava.impuestos.model;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(name="Simple",
		members="descripcion, porcentaje")	
})

public class TasaImpuesto {
	
	// Posibles tasas:
	public static final String IVA1 = "IVA1"; // 21%
	public static final String IVA2 = "IVA2"; // 10.5%
	public static final String IVA3 = "IVA3"; // 27%
	public static final String IVA4 = "IVA4"; // 0%
	public static final String IVA5 = "IVA5"; // 5%
	public static final String IVA6 = "IVA6"; // 2.5%
	
	public static TasaImpuesto buscarTasaPorPorcentaje(BigDecimal porcentaje){
		Query query = XPersistence.getManager().createQuery("from TasaImpuesto where porcentaje = :porcentaje");
		query.setParameter("porcentaje", porcentaje);
		try{
			return (TasaImpuesto)query.getSingleResult();
		} catch(Exception e){
			throw new ValidationException("No se pudo encontrar la tasa de iva " + porcentaje);
		}
	}
		
	@Id @Hidden
	@Column(length=10)
	private String codigo;
	
	@ReadOnly
	@Column(length=50)
	@SearchKey
	private String descripcion;
	
	@ReadOnly
	private BigDecimal porcentaje;
	
	@Column(length=2)
	@ReadOnly
	private String codigoAfip;
	
	private Boolean principal = Boolean.FALSE;
	
	public BigDecimal getPorcentaje() {
		return porcentaje == null? BigDecimal.ZERO: this.porcentaje;
	}

	public void setPorcentaje(BigDecimal porcentaje) {
		this.porcentaje = porcentaje;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public String getCodigoAfip() {
		return codigoAfip;
	}
	public void setCodigoAfip(String codigoAfip) {
		this.codigoAfip = codigoAfip;
	}
	
	@PreRemove 
	private void validateOnRemove() {
		// no se pueden borrar
		throw new IllegalStateException("");
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
}

