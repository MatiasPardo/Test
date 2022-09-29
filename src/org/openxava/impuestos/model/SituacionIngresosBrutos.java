package org.openxava.impuestos.model;

public enum SituacionIngresosBrutos {
	
	Local(1, "D"), ConvenioMultilateral(2, "C"), NoInscripto(4, "");
	
	private Integer codigoArciba;
	
	private String codigoPadronIIBB;
	
	SituacionIngresosBrutos(Integer codigoArciba, String tipoContribPadron){
		this.codigoArciba = codigoArciba;
		this.codigoPadronIIBB = tipoContribPadron;
	}

	public Integer getCodigoArciba() {
		return codigoArciba;
	}

	public String getCodigoPadronIIBB() {
		return codigoPadronIIBB;
	}
}
