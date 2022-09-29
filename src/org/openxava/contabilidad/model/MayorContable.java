package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;

@Entity

@Table(name="VIEW_MAYORCONTABLE")

@Views({
	@View(members="empresa; cuenta; mayor"),
	@View(name="Simple", members="empresa; cuenta"),
})

@Tab(properties="empresa.nombre, cuenta.codigo, cuenta.nombre",
	filter=EmpresaFilter.class, 
	baseCondition=EmpresaFilter.BASECONDITION,
	defaultOrder="${empresa.numero} asc, ${cuenta.codigo} asc"
	)

public class MayorContable {
	
	@Id
	@Hidden
	@Column(length=64)
	private String id;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReadOnly
	@ReferenceView(value="Mayor")
	private CuentaContable cuenta;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReadOnly @NoModify @NoCreate
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;

	@OneToMany(mappedBy="mayor", cascade=CascadeType.ALL)
	@ReadOnly
	@ListProperties("numero, fecha, detalle, observaciones, debe, haber, saldo")
	@Condition("${mayor.id} = ${this.id}")
	@OrderBy("fecha desc, fechaCreacion desc, asiento.id desc") 
	private Collection<ItemMayorContable> mayor;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public Empresa getEmpresa() {
		return empresa;
	}
 
	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Collection<ItemMayorContable> getMayor() {
		return mayor;
	}

	public void setMayor(Collection<ItemMayorContable> mayor) {
		this.mayor = mayor;
	}	
}
