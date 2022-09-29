package org.openxava.negocio.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.validators.*;
import org.openxava.validators.*;

@Entity

@Tabs({
	@Tab(properties="origen.codigo, equivalencia, destino.codigo, descripcion")
})

@EntityValidators({
	@EntityValidator(
		value=UnicidadEquivalenciasValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="origen"),
			@PropertyValue(name="destino")
		}
	)
	
})

public class EquivalenciaUnidadesMedida extends ObjetoNegocio{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private UnidadMedida origen;
	
	@Digits(integer=16, fraction=6)
	@PropertyValidator(value=NotZeroValidator.class)
	private BigDecimal equivalencia;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private UnidadMedida destino;
	
	@Column(length=100)
	@DefaultValueCalculator(value=DescripcionEquivalenciasCalculator.class, 
					properties={@PropertyValue(name="origenId", from="origen.id"),
								@PropertyValue(name="destinoId", from="destino.id"),
								@PropertyValue(name="equivalencia")})
	@ReadOnly
	private String descripcion = "";
		
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public UnidadMedida getOrigen() {
		return origen;
	}

	public void setOrigen(UnidadMedida origen) {
		this.origen = origen;
	}

	public UnidadMedida getDestino() {
		return destino;
	}

	public void setDestino(UnidadMedida destino) {
		this.destino = destino;
	}

	public BigDecimal getEquivalencia() {
		return this.equivalencia == null ? BigDecimal.ZERO : this.equivalencia;
	}

	public void setEquivalencia(BigDecimal equivalencia) {
		this.equivalencia = equivalencia;
	} 
}
