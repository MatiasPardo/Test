package org.openxava.tesoreria.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;

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
				@PropertyValue(name="modelo", value="Banco"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
			}
	)
})

public class Banco extends ObjetoEstatico{
	
	@OneToMany(mappedBy="banco", cascade=CascadeType.ALL)
	private Collection<SucursalBanco> sucursales;

	public Collection<SucursalBanco> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Collection<SucursalBanco> sucursales) {
		this.sucursales = sucursales;
	}
}
