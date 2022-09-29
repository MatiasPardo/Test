package org.openxava.contabilidad.model;

import java.util.*;

public interface IParametrosReporteContable {
	
	public Date getDesde();
	
	public Date getHasta();
	
	public EjercicioContable ejercicio();
	
	public void validarRangoFechas(Date fechaDesde, Date fechaHasta);
}
