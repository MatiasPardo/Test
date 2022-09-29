package org.openxava.ventas.filter;

import java.util.*;

import org.openxava.filters.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class ClienteFilter implements IFilter{

	public final static String BASECONDITION = "(false = ? or e.vendedor.id = ?) and ${activo} = 't'";
	
	@Override
	public Object filter(Object o) throws FilterException {
		Vendedor vendedorUsuario = Vendedor.buscarVendedorUsuario(Users.getCurrent());
		Boolean filtrarPorVendedor = Boolean.FALSE;
		String idVendedor = "";
		if ((vendedorUsuario != null) && (!vendedorUsuario.getGerencia())){
			filtrarPorVendedor = Boolean.TRUE;
			idVendedor = vendedorUsuario.getId();
		}
		
		if (o == null) {
			return new Object [] { filtrarPorVendedor, idVendedor };
		}
		else if (o instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) o));
			c.add(0, idVendedor);
			c.add(0, filtrarPorVendedor);
			return c.toArray();			
		} 
		else {
			Object o2 = new Object [] { filtrarPorVendedor, idVendedor, o	};
			return o2;
		}		
	}

}
