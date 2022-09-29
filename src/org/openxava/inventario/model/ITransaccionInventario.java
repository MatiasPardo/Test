package org.openxava.inventario.model;

import java.util.*;

import org.openxava.base.model.EmpresaExterna;

public interface ITransaccionInventario {

	public ArrayList<IItemMovimientoInventario> movimientosInventario();
	
	public boolean revierteInventarioAlAnular();
	
	public EmpresaExterna empresaExternaInventario();
}
