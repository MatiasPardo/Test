package org.openxava.tesoreria.actions;

import org.openxava.tesoreria.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ModificarNumeroValorAction extends ModificarAtributoValorAction{

	@Override
	protected void modificarAtributo(Valor valor) {
		String nro = getView().getValueString("numero");
		if (!Is.emptyString(nro)){
			valor.cambiarNumero(nro);
		}
		else{
			throw new ValidationException("Número no asignado");
		}
	}

}
