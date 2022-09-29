package org.openxava.tesoreria.model;

import java.util.*;

import org.openxava.base.model.*;

public class ComparatorItemIngresoValoresPorEmpresa implements Comparator<ItemIngresoValores>{

	@Override
	public int compare(ItemIngresoValores arg0, ItemIngresoValores arg1) {
		Empresa empresa0 = arg0.getEmpresa();
		Empresa empresa1 = arg1.getEmpresa();
		
		int comparacion = empresa0.getFechaCreacion().compareTo(empresa1.getFechaCreacion());
		if (comparacion == 0){
			comparacion = empresa0.getId().compareTo(empresa1.getId());
		}
		
		return comparacion;
	}

}
