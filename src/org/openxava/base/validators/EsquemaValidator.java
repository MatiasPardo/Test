package org.openxava.base.validators;

import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class EsquemaValidator implements IValidator{
	
	private Boolean sucursalUnica;
	
	public Boolean getSucursalUnica() {
		return sucursalUnica;
	}

	public void setSucursalUnica(Boolean sucursalUnica) {
		this.sucursalUnica = sucursalUnica;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getSucursalUnica() == null){
			errors.add("Sucursal Unica no puede ser nulo");
		}
		else{
			
			Query query = XPersistence.getManager().createQuery("from Sucursal");
			List<?> res = query.getResultList();
			if (!res.isEmpty()){				
				boolean hayUsuarios = false;
				for(Object r: res){
					Sucursal sucursal = (Sucursal)r;
					if (!sucursal.getUsuarios().isEmpty()){
						hayUsuarios = true;
					}
				}
				
				if (!this.getSucursalUnica()){
					if (!hayUsuarios){
						errors.add("No hay usuarios habilitados en las sucursales. Si desactiva sucursal única no tendrá acceso a los módulos");
					}
				}
			}
			else{
				if (!this.getSucursalUnica()){
					errors.add("No hay sucursales definidas: Debe estar configurada la opción de sucursal única");
				}
			}
		}
	}

}
