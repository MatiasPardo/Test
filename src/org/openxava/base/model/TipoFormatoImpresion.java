package org.openxava.base.model;

public enum TipoFormatoImpresion {
	
	PDF(".pdf"), Excel(".xls"), HTML(".html");
	
	private String extensionArchivo;
	
	TipoFormatoImpresion(String extensionArchivo){
		this.extensionArchivo = extensionArchivo;
	}
		
	public String agregarExtensionArchivo(String fileName){
		return fileName + this.extensionArchivo;
	}
}
