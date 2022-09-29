package org.openxava.reclamos.filter;

import java.util.*;

import org.openxava.base.filter.*;
import org.openxava.filters.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class ReclamoFilter extends EmpresaFilter {

	public final static String RECLAMOBASECONDITION = "(true = ? or ${objetoReclamo.seguridad.id} = ?) and " + EmpresaFilter.BASECONDITION;
	
	public Object filter(Object o) throws FilterException {
		Object superFilter = super.filter(o);
		
		List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) superFilter));
		GrupoUsuarioObjetoReclamo grupo = GrupoUsuarioObjetoReclamo.buscarGrupo(Users.getCurrent());		
		if (grupo != null){
			c.add(0, grupo.getId());
			c.add(0, grupo.getAdministrador());
		}
		else{
			// Filtro vacio
			c.add(0, "");
			c.add(0, Boolean.FALSE);
		}
		return c.toArray();
	}
}
