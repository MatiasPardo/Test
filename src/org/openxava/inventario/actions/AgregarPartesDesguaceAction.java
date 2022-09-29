package org.openxava.inventario.actions;

import org.openxava.base.actions.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;

public class AgregarPartesDesguaceAction extends ModificarYGrabarTransaccionAction{
	
	protected void modificarTransaccion(Transaccion transaccion){
		((Desguace)transaccion).agregarPartesPorDefecto();
	}
	
}
