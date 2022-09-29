package org.openxava.contabilidad.model;

import java.math.*;

public interface IGeneradorItemContable {

	CuentaContable igcCuentaContable();

	BigDecimal igcHaberOriginal();

	BigDecimal igcDebeOriginal();
	
	CentroCostos igcCentroCostos();

	UnidadNegocio igcUnidadNegocio();

	String igcDetalle();	

}
