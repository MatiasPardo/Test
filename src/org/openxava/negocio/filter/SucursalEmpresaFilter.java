package org.openxava.negocio.filter;

import java.util.*;

import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.filters.*;


import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class SucursalEmpresaFilter extends EmpresaFilter{
	
	public final static String BASECONDITION_SUCURSAL = "(? member of e.sucursal.usuarios or true = ?)";
	
	public final static String BASECONDITION_EMPRESASUCURSAL = BASECONDITION_SUCURSAL + " and " + EmpresaFilter.BASECONDITION;
	
	public final static String SUCURSAL_CONDITION_ITEMS1 = "(? member of e.";
	
	public final static String SUCURSAL_CONDITION_ITEMS2 = ".sucursal.usuarios or true = ?)";
	
	@Override
	public Object filter(Object o) throws FilterException {
		
		Object filters = super.filter(o);
		
		User usuario = getUsuario();		
		Boolean sucursalUnica = Esquema.getEsquemaApp().getSucursalUnica();
		
		if (filters == null) {
			return new Object [] { usuario, sucursalUnica };
		}
		else if (filters instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) filters));
			c.add(0, sucursalUnica);
			c.add(0, usuario);			
			return c.toArray();			
		} 
		else {
			Object o2 = new Object [] { usuario, sucursalUnica, filters};
			return o2;
		}		
		
	}
}
