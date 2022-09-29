package org.openxava.negocio.filter;

import java.util.*;

import org.openxava.base.filter.*;
import org.openxava.filters.*;

import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class SucursalDestinoEmpresaFilter extends SucursalEmpresaFilter{
	
	public final static String BASECONDITION_EMPRESASUCURSALDESTINO = "(? member of e.sucursalDestino.usuarios or " + BASECONDITION_SUCURSAL + ") and " + EmpresaFilter.BASECONDITION;
	
	public final static String BASECONDITION_EMPRESASUCURSALDESTINO_TRANSACCIONES = "( (? member of e.sucursalDestino.usuarios and e.estado in (1, 2)) or " + BASECONDITION_SUCURSAL + ") and " + EmpresaFilter.BASECONDITION;
	
	@Override
	public Object filter(Object o) throws FilterException {
		Object filters = super.filter(o);
				
		User usuario = getUsuario();				
		if (filters == null) {
			return new Object [] { usuario };
		}
		else if (filters instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) filters));
			c.add(0, usuario);			
			return c.toArray();			
		} 
		else {
			Object o2 = new Object [] { usuario, filters};
			return o2;
		}		
	}
}
