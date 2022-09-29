package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public interface ITransaccionCtaCte{
	
	public Date CtaCteFecha();
		
	public BigDecimal CtaCteImporte();
	
	public BigDecimal CtaCteNeto();
	
	public String CtaCteTipo();
	
	public OperadorComercial CtaCteOperadorComercial();
	
	public IResponsableCuentaCorriente CtaCteResponsable();
	
	public Date CtaCteFechaVencimiento();
	
	public String CtaCteNumero();
	
	public Transaccion CtaCteTransaccion();

	public CuentaCorriente CtaCteNuevaCuentaCorriente();
	
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte);
		
	public boolean generadaPorDiferenciaCambio();

	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio);
	
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas);
	
	public Integer CtaCteCoeficiente();
	
	public boolean generaCtaCte();
}
