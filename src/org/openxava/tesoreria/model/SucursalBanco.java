package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.negocio.model.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="SucursalBanco"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	)
})

public class SucursalBanco extends ObjetoEstatico{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	private Banco banco;
	
	private Integer numeroSucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    @AsEmbedded
    private Domicilio domicilio;

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public Integer getNumeroSucursal() {
		return numeroSucursal;
	}

	public void setNumeroSucursal(Integer numeroSucursal) {
		this.numeroSucursal = numeroSucursal;
	}
}
