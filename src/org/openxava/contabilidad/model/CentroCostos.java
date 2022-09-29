package org.openxava.contabilidad.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.UnicidadValidator;
import org.openxava.calculators.FalseCalculator;
import org.openxava.contabilidad.validators.CentroCostosValidator;

@Entity

@Views({
	@View(name="Simple",
		members="codigo, nombre"),
	@View(members="Principal{codigo, activo; nombre;" +
		"distribuye, unidadNegocio; distribucion}" + 
			"Auditoria{fechaCreacion; usuario}")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
		value=CentroCostosValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 				
			@PropertyValue(name="distribuye"),
			@PropertyValue(name="distribucion"),
			@PropertyValue(name="unidadNegocio"),
		}
	),
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="CentroCostos"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	)
})
public class CentroCostos extends ObjetoEstatico{
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean distribuye = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", forTabs="combo")
	@NoCreate @NoModify
	private UnidadNegocio unidadNegocio;
	
	@ElementCollection(fetch=FetchType.LAZY)
	@ListProperties("distribucionCostos.nombre, porcentaje")
	private Collection<DistribucionCentroCosto> distribucion;

	public Boolean getDistribuye() {
		return distribuye == null ? Boolean.FALSE : distribuye;
	}

	public void setDistribuye(Boolean distribuye) {
		if (distribuye != null){
			this.distribuye = distribuye;
		}
		else{
			this.distribuye = Boolean.FALSE;
		}
	}

	public Collection<DistribucionCentroCosto> getDistribucion() {
		return distribucion;
	}

	public void setDistribucion(Collection<DistribucionCentroCosto> distribucion) {
		this.distribucion = distribucion;
	}

	public Collection<DistribucionCentroCosto> distribucionCompletaCentroCostos() {
		Collection<DistribucionCentroCosto> distribucionCompleta = new LinkedList<DistribucionCentroCosto>();
		this.armarDistribucionCentroCostosCompleta(this, new BigDecimal(1), distribucionCompleta);
		return distribucionCompleta;
	}	
	
	private void armarDistribucionCentroCostosCompleta(CentroCostos centro, BigDecimal coeficiente, Collection<DistribucionCentroCosto> distribucionCompleta){
		if (centro.getDistribuye()){
			for(DistribucionCentroCosto distribucion: centro.getDistribucion()){
				this.armarDistribucionCentroCostosCompleta(distribucion.getDistribucionCostos(), coeficiente.multiply(distribucion.getPorcentaje().divide(new BigDecimal(100), 16, RoundingMode.HALF_EVEN)), distribucionCompleta);
			}
		}
		else{
			DistribucionCentroCosto nuevaDistribucion = new DistribucionCentroCosto();
			nuevaDistribucion.setDistribucionCostos(centro);
			nuevaDistribucion.setPorcentaje(coeficiente.multiply(new BigDecimal(100)));
			distribucionCompleta.add(nuevaDistribucion);
		}
	}

	public UnidadNegocio getUnidadNegocio() {
		return unidadNegocio;
	}

	public void setUnidadNegocio(UnidadNegocio unidadNegocio) {
		this.unidadNegocio = unidadNegocio;
	}
}
