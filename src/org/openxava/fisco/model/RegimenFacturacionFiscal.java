package org.openxava.fisco.model;

public enum RegimenFacturacionFiscal {
	Comun(Regionalidad.AR, true), Exportacion(Regionalidad.AR, false), FCE(Regionalidad.AR, true);
	
	private Regionalidad region;
	
	private boolean calculaImpuestos;
	
	public Regionalidad getRegion(){
		return this.region;
	}
	
	public boolean getCalculaImpuestos(){
		return this.calculaImpuestos;
	}
	
	RegimenFacturacionFiscal(Regionalidad regionalidad, boolean calculaImpuestos){
		this.region = regionalidad;
		this.calculaImpuestos = calculaImpuestos;
	}
}
