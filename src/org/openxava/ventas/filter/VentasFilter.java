package org.openxava.ventas.filter;

import java.util.*;

import org.openxava.base.filter.*;
import org.openxava.filters.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class VentasFilter extends EmpresaFilter{
	
	public Object filter(Object o) throws FilterException {
		Object superFilter = super.filter(o);
		
		Vendedor vendedorUsuario = Vendedor.buscarVendedorUsuario(Users.getCurrent());
		
		List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) superFilter));
		if ((vendedorUsuario != null) && (!vendedorUsuario.getGerencia())){
			// se filtra por vendedor
			c.add(0, vendedorUsuario.getId());
			c.add(0, Boolean.FALSE);
		}
		else{
			// se incluyen todos
			c.add(0, "");
			c.add(0, Boolean.TRUE);
		}
		
		return c.toArray();	
	}
}
