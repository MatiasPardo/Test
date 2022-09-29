package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})


public class Subzona extends ObjetoEstatico{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Zona zona;

	public Zona getZona() {
		return zona;
	}

	public void setZona(Zona zona) {
		this.zona = zona;
	}
	
}
