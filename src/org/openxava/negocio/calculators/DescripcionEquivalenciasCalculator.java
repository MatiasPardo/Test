package org.openxava.negocio.calculators;

import java.math.*;

import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class DescripcionEquivalenciasCalculator implements ICalculator{
	
	private String origenId;
	
	private String destinoId;
	
	private Object origen = null;
	
	private Object destino = null;
	
	private BigDecimal equivalencia;
	
	public Object getOrigen() {
		return origen;
	}
	
	public Object getDestino() {
		return destino;
	}

	public String getOrigenId() {
		return origenId;
	}

	public void setOrigenId(String origenId) {
		this.origenId = origenId;
		if (Is.emptyString(origenId)){
			this.origen = null;
		}
		else{
			this.origen = XPersistence.getManager().find(UnidadMedida.class, origenId);
		}
	}

	public String getDestinoId() {
		return destinoId;
	}

	public void setDestinoId(String destinoId) {
		this.destinoId = destinoId;
		if (Is.emptyString(destinoId)){
			this.destino = null;
		}
		else{
			this.destino = XPersistence.getManager().find(UnidadMedida.class, destinoId);
		}
	}

	public BigDecimal getEquivalencia() {
		return this.equivalencia == null ? BigDecimal.ZERO : this.equivalencia;
	}

	public void setEquivalencia(BigDecimal equivalencia) {
		this.equivalencia = equivalencia;
	}

	@Override
	public Object calculate() throws Exception {
		
		if ((this.getOrigen() != null) && (this.getDestino() != null)){
			String str = "1 " + this.getOrigen().toString() + " equivale a " + this.getEquivalencia().setScale(2, RoundingMode.UP).toString() + " " + this.getDestino().toString();
			if (str.length() > 100){
				str = str.substring(0, 99);
			}
			return str;
		}
		else{
			return new String("");
		}
		
	}

}
