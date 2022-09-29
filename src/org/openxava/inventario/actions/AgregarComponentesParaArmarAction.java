package org.openxava.inventario.actions;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;

public class AgregarComponentesParaArmarAction extends ModificarYGrabarTransaccionAction{

	@Override
	protected void modificarTransaccion(Transaccion transaccion) {
		((Armado)transaccion).agregarComponentesPorDefecto();		
	}

}
