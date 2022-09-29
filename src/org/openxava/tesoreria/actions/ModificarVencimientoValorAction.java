package org.openxava.tesoreria.actions;

import java.util.*;

import org.openxava.tesoreria.model.*;
import org.openxava.validators.*;

public class ModificarVencimientoValorAction extends ModificarAtributoValorAction {

	@Override
	protected void modificarAtributo(Valor valor) {
		Date fechaVencimiento = (Date)getView().getValue("fechaVencimiento");
		if (fechaVencimiento != null){
			valor.cambiarFechaVencimiento(fechaVencimiento);
		}
		else{
			throw new ValidationException("Fecha de Vencimiento no asignada");
		}
	}

}
