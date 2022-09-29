package org.openxava.base.model;

public interface IImportadorItemCSV {

	public void iniciarImportacionCSV();
	
	public ItemTransaccion crearItemDesdeCSV(String[] values);
	
	public void finalizarImportacionCSV();

}
