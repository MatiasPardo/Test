package org.openxava.contabilidad.actions;

import java.util.*;

import javax.persistence.*;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;

public class SincronizarNivelesEnCuentaAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		Query query = XPersistence.getManager().createQuery("from CuentaContable");
		List<?> result = query.getResultList();
		for(Object object: result){
			CuentaContable cuenta = (CuentaContable)object;
			cuenta.sincronizarNiveles();			
		}
		XPersistence.commit();
		addMessage("Finalizado");
	}

}
