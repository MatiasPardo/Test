package org.openxava.contratos.model;

public enum FrecuenciaCicloFacturacion {
	Mensual(1), Bimestral(2), Trimestral(3), Cuatrimestral(4), Semestral(6), Anual(12);
	
	private int meses;
	
	FrecuenciaCicloFacturacion(int meses){
		this.meses = meses;
	}

	public int getMeses() {
		return meses;
	}
}
