package org.openxava.negocio.calculators;

import org.openxava.base.model.*;
import org.openxava.calculators.*;


@SuppressWarnings("serial")
public class Moneda1Calculator implements ICalculator {

	@Override
	public Object calculate() throws Exception {
		Empresa empresaDefault = Empresa.buscarEmpresaPorNro(1);
		if (empresaDefault != null){
			return empresaDefault.getMoneda1();
		}
		else{
			return null;
		}
	}

}
