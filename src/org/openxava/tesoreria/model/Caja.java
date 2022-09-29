package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.validators.*;

@Entity

@Views({
	@View(members="codigo;" +
			"nombre;" +
			"activo, principal, permitirEfectivoNegativo;" +
			"empresa, sucursal;" + 
			"valoresPosibles;"),
	@View(name="Simple", members="codigo, nombre"),
	@View(name="SimpleConEmpresa", members="codigo, nombre, empresa")
})

@Tabs({
	@Tab(filter=SucursalEmpresaFilter.class,
			baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + ObjetoEstatico.CONDITION_ACTIVOS ),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Tesoreria"),
				@PropertyValue(name="idMessage", value="codigo_repetido_tesoreria")
				
			}
	),
	@EntityValidator(
			value=PrincipalSucursalEmpresaValidator.class, 
			properties= {
				@PropertyValue(name="idEntidad", from="id"), 
				@PropertyValue(name="modelo", value="Caja"),
				@PropertyValue(name="empresa", from="empresa"),
				@PropertyValue(name="sucursal", from="sucursal"),
				@PropertyValue(name="principal")
			}
	)
})

public class Caja extends Tesoreria{
	
	@Override
	public boolean permiteTipoValor(TipoValorConfiguracion tipoValor){
		boolean permite = super.permiteTipoValor(tipoValor);
		if (permite){
			permite = tipoValor.getComportamiento().permiteCaja();
		}
		return permite;
	}
	
	@Override
	public Boolean esCuentaBancaria() {
		return Boolean.FALSE;
	}
}
