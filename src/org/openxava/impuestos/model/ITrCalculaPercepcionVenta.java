package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

public interface ITrCalculaPercepcionVenta {
	
	public Cliente getCliente();
	
	public PosicionAnteImpuesto getPosicionIva();
	
	public BigDecimal getSubtotal();
	
	public Date getFecha();
	
	public Domicilio domicilioCalculoPercepcion();
	
	public boolean revierteTransaccion();
}
