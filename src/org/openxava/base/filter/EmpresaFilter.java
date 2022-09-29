package org.openxava.base.filter;

import java.util.*;

import org.openxava.filters.*;
import org.openxava.util.*;

import com.openxava.naviox.model.*;

@SuppressWarnings("serial")
public class EmpresaFilter implements IFilter{
	
	/*public final static String CLAUSULABASECONDITION = " in ( ?, ? )";
	
	public final static String BASECONDITION = "${empresa.id} " + CLAUSULABASECONDITION;*/ 
		
	public final static String BASECONDITION = "(? member of e.empresa.usuarios and e.empresa.activo = true)";
	
	public final static String CONDITION_ITEMS1 = "(? member of e."; 
	public final static String CONDITION_ITEMS2 = ".empresa.usuarios and e.";
	public final static String CONDITION_ITEMS3 = ".empresa.activo = true)";
	
	private User usuario = null;
	
	public User getUsuario() {
		if (this.usuario == null){
			this.usuario = User.find(Users.getCurrent()); 
		}
		return usuario;
	}
	
	public Object filter(Object o) throws FilterException {
		this.usuario = null;
		User usuario = getUsuario();	
		
		if (o == null) {
			return new Object [] { usuario };
		}
		else if (o instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) o));
			c.add(0, usuario);
			return c.toArray();			
		} 
		else {
			return new Object [] { usuario, o	};
		}		
	}
	
	/*public Object filter(Object o) throws FilterException {
		List<String> idsEmpresa = new LinkedList<String>();
		Empresa.buscarEmpresasHabilitadas(idsEmpresa);
		String id1 = "";
		String id2 = "";
		
		if (idsEmpresa.size() == 2){
			id1 = idsEmpresa.get(0);
			id2 = idsEmpresa.get(1);
		}
		else if (idsEmpresa.size() == 1){
			id1 = idsEmpresa.get(0);
		}
		
		
		if (o == null) {
			return new Object [] { id1, id2 };
		}
		else if (o instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) o));
			c.add(0, id1);
			c.add(0, id2);
			return c.toArray();			
		} 
		else {
			Object o2 = new Object [] { id1, o	};
			return new Object [] { id2, o2	};
		}		

	}*/
}
