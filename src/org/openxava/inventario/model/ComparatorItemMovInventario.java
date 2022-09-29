package org.openxava.inventario.model;

import java.util.*;

public class ComparatorItemMovInventario implements Comparator<IItemMovimientoInventario>{

	@Override
	public int compare(IItemMovimientoInventario o1, IItemMovimientoInventario o2) {
		int comparacion = o1.getDeposito().getFechaCreacion().compareTo(o2.getDeposito().getFechaCreacion());
		if (comparacion == 0){
			comparacion = o1.getDeposito().getId().compareTo(o2.getDeposito().getId());
		}
		if (comparacion == 0){
			comparacion = o1.getProducto().getFechaCreacion().compareTo(o2.getProducto().getFechaCreacion());
			if (comparacion == 0){
				comparacion = o1.getProducto().getId().compareTo(o2.getProducto().getId());
			}
		}
		if (comparacion == 0){
			if (o1.getDespacho() == null){
				if (o2.getDespacho() != null){
					comparacion = -1;
				}
			}
			else if (o2.getDespacho() == null){
				if (o1.getDespacho() != null){
					comparacion = 1;
				}
			}
			else{
				comparacion = o1.getDespacho().getFechaCreacion().compareTo(o2.getDespacho().getFechaCreacion());
				if (comparacion == 0){
					comparacion = o1.getDespacho().getId().compareTo(o2.getDespacho().getId());
				}
			}
		}
		if (comparacion == 0){
			if (o1.getLote() == null){
				if (o2.getLote() != null){
					comparacion = -1;
				}
			}
			else if (o2.getLote() == null){
				if (o1.getLote() != null){
					comparacion = 1;
				}
			}
			else{
				comparacion = o1.getLote().getFechaCreacion().compareTo(o2.getLote().getFechaCreacion());
				if (comparacion == 0){
					comparacion = o1.getLote().getId().compareTo(o2.getLote().getId());
				}
			}
		}
		return comparacion;
	}

	
}
