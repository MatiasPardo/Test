package org.openxava.tesoreria.model;

import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;

@Entity

@Views({
	@View(name="Simple", members="codigo, nombre"),
	@View(name="SimpleConEmpresa", members="codigo, nombre, empresa")	
})

@Tabs({
	@Tab(properties="codigo, nombre, activo, empresa.nombre",
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION + " and " + ObjetoEstatico.CONDITION_ACTIVOS ),
	@Tab(name="TesoreriaPorSucursal", 
		properties="codigo, nombre, activo, empresa.nombre",
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + ObjetoEstatico.CONDITION_ACTIVOS ),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + ObjetoEstatico.CONDITION_INACTIVOS)
})

public class Tesoreria extends ObjetoEstatico{
	
	public static final String CONDITIONVALORESDESCRIPTIONLIST = "${id} IN (select v.id from Tesoreria t join t.valoresPosibles v where t.id = ?)"; 
	
	public static Tesoreria buscarPrincipal(String modelo, Empresa empresa, Sucursal sucursal){
		Query query = XPersistence.getManager().createQuery("from " + modelo + " where empresa.id = :idEmpresa and principal = :principal and sucursal.id = :idSucursal");
		query.setParameter("idEmpresa", empresa.getId());
		query.setParameter("idSucursal", sucursal.getId());
		query.setParameter("principal", Boolean.TRUE);
		query.setMaxResults(1);
		query.setFlushMode(FlushModeType.COMMIT);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (Tesoreria)results.get(0);
		}
		else{
			return null;
		}
	}
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean principal = Boolean.FALSE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@DefaultValueCalculator(value=SucursalPrincipalCalculator.class)
	private Sucursal sucursal;
	
	@ManyToMany
	@Size(min=1)
	private Collection<TipoValorConfiguracion> valoresPosibles;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean multiplesChequeras = Boolean.FALSE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean permitirEfectivoNegativo = Boolean.TRUE;
	
	public Collection<TipoValorConfiguracion> getValoresPosibles() {
		return valoresPosibles;
	}

	public void setValoresPosibles(Collection<TipoValorConfiguracion> valoresPosibles) {
		this.valoresPosibles = valoresPosibles;
	}

	public TipoValorConfiguracion consolidaCon(TipoValorConfiguracion tipoValorConfiguracion) {		
		return null;
	}

	public boolean permiteTipoValor(TipoValorConfiguracion tipoValor) {
		boolean permite = false;
		if (this.getValoresPosibles() != null){
			permite = this.getValoresPosibles().contains(tipoValor);
		}
		return permite;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Boolean esCuentaBancaria() {
		return null;
	}
	
	public TipoValorConfiguracion tipoValorEfectivo(Moneda moneda){
		for(TipoValorConfiguracion tipo: this.getValoresPosibles()){
			if (tipo.getMoneda().equals(moneda)){
				if (tipo.getComportamiento().equals(TipoValor.Efectivo)){
					return tipo;
				}
			}
		}
		return null;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Boolean getMultiplesChequeras() {
		return multiplesChequeras == null ? Boolean.FALSE :this.multiplesChequeras;
	}

	public void setMultiplesChequeras(Boolean multiplesChequeras) {
		this.multiplesChequeras = multiplesChequeras;
	}

	public Boolean getPermitirEfectivoNegativo() {
		return permitirEfectivoNegativo == null ? Boolean.TRUE : this.permitirEfectivoNegativo;
	}

	public void setPermitirEfectivoNegativo(Boolean permitirEfectivoNegativo) {
		if (permitirEfectivoNegativo != null){
			this.permitirEfectivoNegativo = permitirEfectivoNegativo;
		}
	}
}
