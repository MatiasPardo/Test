package org.openxava.reclamos.filter;

import java.util.*;

import org.openxava.filters.*;
import org.openxava.reclamos.model.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class ObjetoReclamoFilter implements IFilter{

	public static final String BASECONDICION = "(true = ? or ${seguridad.id} = ?)";
	
	public Object filter(Object o) throws FilterException {		
		String idGrupo = "";		
		Boolean administrador = Boolean.FALSE;
		GrupoUsuarioObjetoReclamo grupo = GrupoUsuarioObjetoReclamo.buscarGrupo(Users.getCurrent());
		if (grupo != null){
			idGrupo = grupo.getId();
			administrador = grupo.getAdministrador();
		}
		if (o == null) {
			return new Object [] { administrador, idGrupo };
		}
		else if (o instanceof Object []) {			
			List<Object> c = new ArrayList<Object>(Arrays.asList((Object []) o));
			c.add(0, idGrupo);			
			c.add(0, administrador);
			return c.toArray();			
		} 
		else {
			return new Object [] { administrador, idGrupo, o	};
		}		
	}
}
