package org.openxava.negocio.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@Views({
	@View(name="Simple", members="codigo, nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class Pais extends ObjetoEstatico{

	private int codigoAfip;

	public int getCodigoAfip() {
		return codigoAfip;
	}

	public void setCodigoAfip(int codigoAfip) {
		this.codigoAfip = codigoAfip;
	}
	
}
