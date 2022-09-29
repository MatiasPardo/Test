package org.openxava.tesoreria.model;

import java.math.*;

public enum TipoValor {
	Efectivo(true, true, true, 1), 
	ChequeTercero(true, true, false, 1), 
	ChequePropio(false, true, false, -1), 
	TransferenciaBancaria(false, true, true, 1),
	ChequePropioAutomatico(false, true, true, -1),
	TarjetaCreditoCobranza(true, false, true, 1);
	
	
	private boolean caja = true;
	
	private boolean cuentaBancaria = true;

	private boolean consolida = false;
	
	private int coeficiente = 1;
	
	TipoValor(boolean caja, boolean cuentaBancaria, boolean consolida, int coeficiente){
		this.caja = caja;
		this.cuentaBancaria = cuentaBancaria;
		this.consolida = consolida;
		this.coeficiente = coeficiente;
	}
	
	public boolean permiteCaja() {
		return caja;
	}

	public boolean permiteCuentaBancaria() {
		return cuentaBancaria;
	}
	
	public boolean consolidaAutomaticamente(){
		return consolida;
	}
	
	public BigDecimal coeficienteConsolidacion(){
		return new BigDecimal(this.coeficiente);
	}
}
