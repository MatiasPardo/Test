package org.openxava.cuentacorriente.model;

import javax.persistence.Entity;

import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.base.model.ObjetoEstatico;


@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class ClasificadorCuentaCorriente extends ObjetoEstatico{
		
}
