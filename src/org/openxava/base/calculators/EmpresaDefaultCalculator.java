package org.openxava.base.calculators;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.calculators.*;


@SuppressWarnings("serial")
public class EmpresaDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		List<Empresa> lista = new LinkedList<Empresa>();
		Empresa.buscarObjetosEmpresasHabilitadas(lista);
		if (lista.size() == 1){
			return lista.get(0);
		}
		else{
			return null;
		}
	}

}
