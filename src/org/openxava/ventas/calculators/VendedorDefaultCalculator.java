package org.openxava.ventas.calculators;

import org.openxava.calculators.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class VendedorDefaultCalculator implements ICalculator{

	@Override
	public Object calculate() throws Exception {
		return Vendedor.buscarVendedorUsuario(Users.getCurrent());		
	}
	
}
