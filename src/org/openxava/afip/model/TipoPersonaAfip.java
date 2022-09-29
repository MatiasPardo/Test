package org.openxava.afip.model;

public enum TipoPersonaAfip {
	Masculino("20"), Femenino("27"), Empresa("30");
	
	private String tipoCuit;
	
	public String getTipoCuit() {
		return tipoCuit;
	}

	TipoPersonaAfip(String tipoCuit){
		this.tipoCuit = tipoCuit;
	}
	
	public boolean esPersonaFisica(){
		return !this.equals(TipoPersonaAfip.Empresa);		
	}
}
