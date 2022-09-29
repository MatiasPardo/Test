package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;

@Entity

@Views({
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class ConceptoTesoreria extends ObjetoEstatico{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableFinanzas;

	public CuentaContable getCuentaContableFinanzas() {
		return cuentaContableFinanzas;
	}

	public void setCuentaContableFinanzas(CuentaContable cuentaContableFinanzas) {
		this.cuentaContableFinanzas = cuentaContableFinanzas;
	}
}
