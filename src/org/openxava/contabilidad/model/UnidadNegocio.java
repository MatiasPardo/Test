package org.openxava.contabilidad.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.clasificadores.model.Clasificador;

@Entity

@Views({
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class UnidadNegocio extends ObjetoEstatico{

	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'UnidadNegocio'" + Clasificador.CONDICION)
	private Clasificador unidadNegocioClasificador1;

	public Clasificador getUnidadNegocioClasificador1() {
		return unidadNegocioClasificador1;
	}

	public void setUnidadNegocioClasificador1(Clasificador unidadNegocioClasificador1) {
		this.unidadNegocioClasificador1 = unidadNegocioClasificador1;
	}
}
