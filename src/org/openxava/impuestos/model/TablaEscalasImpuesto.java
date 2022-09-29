package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.validators.*;

@Entity

@Tabs({
	@Tab(
		name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})


public class TablaEscalasImpuesto extends ObjetoEstatico{

	@ElementCollection
	@ListProperties("masDe, hasta, importeFijo, masPorcentaje")	
	private Collection<EscalaImpuesto> escalas;
	
	public Collection<EscalaImpuesto> getEscalas() {
		return escalas;
	}

	public void setEscalas(Collection<EscalaImpuesto> escalas) {
		this.escalas = escalas;
	}

	public EscalaImpuesto buscarEscala(BigDecimal importe) {
		EscalaImpuesto escala = null;
		if (this.getEscalas() != null){
			for(EscalaImpuesto esc: this.getEscalas()){
				if ((esc.getMasDe().compareTo(importe) <= 0) && (esc.getHasta().compareTo(importe) >= 0)){
					escala = esc;
					break;
				}
			}
		}
		if (escala == null){
			throw new ValidationException("No se encontró escala para el importe " + importe.toString() + " en el impuesto " + this.toString());
		}
		return escala;
		
	}
}
