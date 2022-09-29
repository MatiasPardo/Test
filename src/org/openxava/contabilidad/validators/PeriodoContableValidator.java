package org.openxava.contabilidad.validators;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class PeriodoContableValidator implements IValidator{

	private EstadoPeriodoContable estado;
	
	private Date desde;
	
	private Date hasta;
	
	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getDesde() != null) && (this.getHasta() != null)){
			if (this.getDesde().compareTo(this.getHasta()) > 0){
				errors.add("Fecha desde debe ser menor a fecha hasta");
			}
			else{
				if (Is.equal(this.getEstado(), EstadoPeriodoContable.Cerrado)){
					Date diaActual = UtilERP.trucarDateTime(new Date());					
					if (this.getHasta().compareTo(diaActual) > 0){
						errors.add("No se puede cerrar un periodo que no termino");
					}
				}
			}
		}		
	}

	public EstadoPeriodoContable getEstado() {
		return estado;
	}

	public void setEstado(EstadoPeriodoContable estado) {
		this.estado = estado;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}
}
