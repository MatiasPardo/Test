package org.openxava.reclamos.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.clasificadores.model.*;
import org.openxava.negocio.validators.*;
import org.openxava.reclamos.filter.*;
import org.openxava.ventas.model.*;


@Views({
	@View(name="Simple", members= "codigo, nombre; calle, altura, potencia;" +
			 "items; zona, subzona, plano, numeroPiquete;" +
			 "EntreCalles[ calle1, calle2 ];"), 
	@View(members= "Principal[codigo, activo, principal; nombre; seguridad];" + 
			"Ubicacion[calle, altura;" + 
				"EntreCalles[ calle1, calle2 ];" +
			 "zona, subzona];" +
			 "Clasificadores[objetoReclamoClasificador1, objetoReclamoClasificador2, objetoReclamoClasificador3;" +
							"objetoReclamoClasificador4, objetoReclamoClasificador5];" +
			 "Otros[plano, numeroPiquete, potencia];" +			
			"items;")
})

@Tabs({
	@Tab(properties="codigo, nombre, calle, altura, calle1, calle2, zona.codigo, subzona.codigo, potencia",
		defaultOrder="${fechaCreacion} desc",
		filter=ObjetoReclamoFilter.class,
		baseCondition=ObjetoReclamoFilter.BASECONDICION + " and " + ObjetoEstatico.CONDITION_ACTIVOS
	),
	@Tab(name="ImportacionCSV",
		properties="codigo, nombre, calle, altura, calle1, calle2, zona.codigo, subzona.codigo, seguridad.codigo, " + 
			"potencia, numeroPiquete, plano," + 
			"objetoReclamoClasificador1.codigo, objetoReclamoClasificador2.codigo, " + 
			"objetoReclamoClasificador3.codigo, objetoReclamoClasificador4.codigo, objetoReclamoClasificador5.codigo",
		baseCondition=ObjetoEstatico.CONDITION_ACTIVOS
	),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="ObjetoReclamo"),
			@PropertyValue(name="principal")
		}
)


@Entity
public class ObjetoReclamo extends ObjetoEstatico{
	
	@Column(length=100)
	@DisplaySize(value = 15)
	private String calle;
	
	@Column(length=6)
	private Integer altura; 
	
	@Column(length=100)
	@DisplaySize(value = 15)
	private String calle1;
	
	@Column(length=100)
	@DisplaySize(value = 15)
	private String calle2;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="nombre")
	@NoModify
	@NoCreate
	private Zona zona;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties=("nombre"),
		depends="this.zona",
		condition="${zona.id} = ?")
	@NoModify
	@NoCreate
	private Subzona subzona;
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = Boolean.FALSE;

    @OneToMany(mappedBy="objetoReclamo",cascade=CascadeType.ALL)
	private Collection<ItemObjetoReclamo> items;

	@Column(length=10)
	private BigDecimal potencia;
	
	@Column(length=25)
	private String plano;
	
	@Column(length=25)
	private String numeroPiquete;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private GrupoUsuarioObjetoReclamo seguridad;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'ObjetoReclamo'" + Clasificador.CONDICION)
	private Clasificador objetoReclamoClasificador1;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 2 and ${tipoClasificador.modulo} = 'ObjetoReclamo'" + Clasificador.CONDICION)
	private Clasificador objetoReclamoClasificador2;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 3 and ${tipoClasificador.modulo} = 'ObjetoReclamo'" + Clasificador.CONDICION)
	private Clasificador objetoReclamoClasificador3;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 4 and ${tipoClasificador.modulo} = 'ObjetoReclamo'" + Clasificador.CONDICION)
	private Clasificador objetoReclamoClasificador4;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 5 and ${tipoClasificador.modulo} = 'ObjetoReclamo'" + Clasificador.CONDICION)
	private Clasificador objetoReclamoClasificador5;
	
	public String getPlano() {
		return plano;
	}

	public void setPlano(String plano) {
		this.plano = plano;
	}

	public String getNumeroPiquete() {
		return numeroPiquete;
	}

	public void setNumeroPiquete(String numeroPiquete) {
		this.numeroPiquete = numeroPiquete;
	}

	public Subzona getSubzona() {
		return subzona;
	}

	public void setSubzona(Subzona subzona) {
		this.subzona = subzona;
	}

	public Collection<ItemObjetoReclamo> getItems() {
		return items;
	}

	public void setItems(Collection<ItemObjetoReclamo> items) {
		this.items = items;
	}

	public Zona getZona() {
		return zona;
	}

	public void setZona(Zona zona) {
		this.zona = zona;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public Integer getAltura() {
		return altura;
	}

	public void setAltura(Integer altura) {
		this.altura = altura;
	}

	public String getCalle1() {
		return calle1;
	}

	public void setCalle1(String calle1) {
		this.calle1 = calle1;
	}

	public String getCalle2() {
		return calle2;
	}

	public void setCalle2(String calle2) {
		this.calle2 = calle2;
	}

	public BigDecimal getPotencia() {
		return potencia;
	}

	public void setPotencia(BigDecimal potencia) {
		this.potencia = potencia;
	}

	public GrupoUsuarioObjetoReclamo getSeguridad() {
		return seguridad;
	}

	public void setSeguridad(GrupoUsuarioObjetoReclamo seguridad) {
		this.seguridad = seguridad;
	}

	public Clasificador getObjetoReclamoClasificador1() {
		return objetoReclamoClasificador1;
	}

	public void setObjetoReclamoClasificador1(Clasificador objetoReclamoClasificador1) {
		this.objetoReclamoClasificador1 = objetoReclamoClasificador1;
	}

	public Clasificador getObjetoReclamoClasificador2() {
		return objetoReclamoClasificador2;
	}

	public void setObjetoReclamoClasificador2(Clasificador objetoReclamoClasificador2) {
		this.objetoReclamoClasificador2 = objetoReclamoClasificador2;
	}

	public Clasificador getObjetoReclamoClasificador3() {
		return objetoReclamoClasificador3;
	}

	public void setObjetoReclamoClasificador3(Clasificador objetoReclamoClasificador3) {
		this.objetoReclamoClasificador3 = objetoReclamoClasificador3;
	}

	public Clasificador getObjetoReclamoClasificador4() {
		return objetoReclamoClasificador4;
	}

	public void setObjetoReclamoClasificador4(Clasificador objetoReclamoClasificador4) {
		this.objetoReclamoClasificador4 = objetoReclamoClasificador4;
	}

	public Clasificador getObjetoReclamoClasificador5() {
		return objetoReclamoClasificador5;
	}

	public void setObjetoReclamoClasificador5(Clasificador objetoReclamoClasificador5) {
		this.objetoReclamoClasificador5 = objetoReclamoClasificador5;
	}

	public Boolean getPrincipal() {
		return principal == null ? Boolean.FALSE : principal;		
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
}
