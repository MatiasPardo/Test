package org.openxava.base.model;

import java.util.*;

import org.openxava.negocio.model.*;
import org.openxava.tesoreria.model.*;

public interface ITransaccionValores {
	
	public void movimientosValores(List<IItemMovimientoValores> lista);
	
	public boolean revierteFinanzasAlAnular();

	public boolean actualizarFinanzasAlConfirmar();
	
	public void antesPersistirMovimientoFinanciero(MovimientoValores item);
	
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte);
	
	public OperadorComercial operadorFinanciero();
	
	public Sucursal getSucursal();
	
	public Sucursal getSucursalDestino();
}
