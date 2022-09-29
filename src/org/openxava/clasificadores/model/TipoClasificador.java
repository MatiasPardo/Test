package org.openxava.clasificadores.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@View(name="Simple", members="codigo, nombre")

public class TipoClasificador extends ObjetoEstatico{
	
	@Column(length=80)
	private String modulo;
	
	@Required
	private Integer numero;
	
	@OneToMany(mappedBy="tipoClasificador", cascade=CascadeType.ALL) 
	private Collection<Clasificador> clasificadores = new ArrayList<Clasificador>();

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Collection<Clasificador> getClasificadores() {
		return clasificadores;
	}

	public void setClasificadores(Collection<Clasificador> clasificadores) {
		this.clasificadores = clasificadores;
	}	
}
