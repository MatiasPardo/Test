package org.openxava.negocio.filter;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.filters.*;
import org.openxava.util.*;

import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class DepositoSucursalFilter implements IFilter{

	public final static String BASECONDITION = "(? member of e.deposito.sucursal.usuarios or true = ?)";
	
	public User getUsuario() {
		return User.find(Users.getCurrent()); 		
	}

	
	@Override
	public Object filter(Object o) throws FilterException {
		User usuario = getUsuario();
		Boolean sucursalUnica = Esquema.getEsquemaApp().getSucursalUnica();
		
		if (o == null) {
			return new Object [] { usuario, sucursalUnica};
		}
		else if (o instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) o));
			c.add(0, sucursalUnica);
			c.add(0, usuario);
			return c.toArray();			
		} 
		else {
			return new Object [] { usuario, sucursalUnica, o};			
		}		
	}
}

