package org.openxava.negocio.filter;

import java.util.*;

import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.filters.*;

import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class TransferenciaDepositosFilter extends SucursalEmpresaFilter{
	
	private final static String BASECONDITION_DEPOSITODESTINO = "((? member of e.destino.sucursal.usuarios or true = ?) and e.estado in (1, 2))";
	
	public final static String BASECONDITION_TRANSFERENCIA = "( " + TransferenciaDepositosFilter.BASECONDITION_DEPOSITODESTINO + " or " + 
							SucursalEmpresaFilter.BASECONDITION_SUCURSAL + ") and " +
							EmpresaFilter.BASECONDITION;
	
	
	@Override
	public Object filter(Object o) throws FilterException {
		Object filters = super.filter(o);
		
		User usuario = getUsuario();		
		Boolean sucursalUnica = Esquema.getEsquemaApp().getSucursalUnica();
		
		if (filters instanceof Object []) {			
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
