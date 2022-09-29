package org.openxava.cuentacorriente.model;

import java.util.*;

public class ComparatorCuentaCorrientePorFecha implements Comparator<CuentaCorriente>{

	@Override
	public int compare(CuentaCorriente arg0, CuentaCorriente arg1) {
		int comparacion = arg0.getFecha().compareTo(arg1.getFecha());
		if (comparacion == 0){
			comparacion = arg0.getFechaVencimiento().compareTo(arg1.getFechaVencimiento());
			if (comparacion == 0){
				comparacion = arg0.getFechaCreacion().compareTo(arg1.getFechaCreacion());
			}
		}
		return comparacion;
	}

}
