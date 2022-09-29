package org.openxava.base.validators;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@SuppressWarnings("serial")
public class ValidadorConfMailActivo implements IValidator{

	private Boolean activo = Boolean.FALSE;
	
	private ConfiguracionEMail configuracion;
	
	public Boolean getActivo() {
		return activo == null ? Boolean.FALSE : activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public ConfiguracionEMail getConfiguracion() {
		return configuracion;
	}

	public void setConfiguracion(ConfiguracionEMail configuracion) {
		this.configuracion = configuracion;
	}



	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getActivo()){
			if (this.getConfiguracion() == null){
				errors.add("Debe definir una configuración de email");
			}
		}
	}

}
