package org.openxava.base.model;

import java.util.*;


public class ComparatorTrazabilidad implements Comparator<Trazabilidad>{

	@Override
	public int compare(Trazabilidad o1, Trazabilidad o2) {
		if (o1.getComprobanteOrigen().equals(o2.getComprobanteDestino())){
			return 1;
		}
		else if (o1.getComprobanteDestino().equals(o2.getComprobanteOrigen())){
			return -1;
		}
		
		int comparacion = o1.getFechaCreacion().compareTo(o2.getFechaCreacion());
		if (comparacion == 0){
			comparacion = o1.getComprobanteOrigen().compareTo(o2.getComprobanteOrigen());
		}
		return comparacion;
	}

}
