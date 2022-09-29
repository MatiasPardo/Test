package org.openxava.base.actions;

import java.io.*;
import java.util.*;

import org.apache.commons.fileupload.*;
import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


import com.csvreader.*;
 
public class ProcesarCSVItemTransaccionAction extends ViewBaseAction implements IProcessLoadedFileAction, INavigationAction, IChainAction {

	private List<?> fileItems;
	
	private String fileErrorId = null;
	
	private final static Integer MAXITEMS = 1000; 
	
	private final static Integer MAXSHOWERRORS = 100;
	
	@Override
	public void execute() throws Exception {
		if (! this.fileItems.isEmpty()){
			
			Transaccion transaccion = (Transaccion)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
			if (!(transaccion instanceof IImportadorItemCSV)){
				throw new ValidationException("La transacción no implementá la lógica de importación de items");
			}
			
			Iterator<?> it = this.fileItems.iterator();
			while (it.hasNext()){
				FileItem fileItem = (FileItem) it.next();
				if (!fileItem.isFormField()){
					// es el archivo CSV
					CsvReader csvReader = null;
					try {
						InputStream data = fileItem.getInputStream();
						Reader reader = new InputStreamReader(data); 
						csvReader = new CsvReader(reader, ';');
						Integer i = new Integer(1);
						IImportadorItemCSV importador = (IImportadorItemCSV)transaccion;
						int errores = 0;
						
						importador.iniciarImportacionCSV();
						while (csvReader.readRecord() && (i <= MAXITEMS)){
							if (i > 1){
								try{
									ItemTransaccion item = importador.crearItemDesdeCSV(csvReader.getValues());
									if (this.getErrors().isEmpty()){
										item.recalcular();									
										XPersistence.getManager().persist(item);
									}
								}
								catch(Exception e){
									errores ++;
									String error = e.getMessage();
									if (Is.emptyString(error)){
										error = e.toString(); 
									}
									if (errores <= MAXSHOWERRORS){
										addError("Error fila " + i.toString() + ": " + error);
									}
								}
							}
							i++;
						}
											
						if (this.getErrors().isEmpty()){
							transaccion.grabarTransaccion();
							this.commit();							
							importador.finalizarImportacionCSV();
							
							addMessage("Importación Finalizada");
							if (i > MAXITEMS){
								addMessage("Se importaron los primeros " + MAXITEMS.toString() + ". Los demás debe generar otro csv");
							}
						}
						else{
							this.rollback();
							
							//generarArchivoErrores(errores);
						}
					} 
					catch (Exception e){
						addError("Error importación CSV: " + e.getMessage());
					}
					finally{
						if (csvReader != null){
							csvReader.close();
						}
					}
					break;
				}
			}
		}
		closeDialog(); 		
	}
	
	/*private void generarArchivoErrores(StringBuilder errores){
		String fileName = "IMPORTACION";
		SimpleDateFormat format = new SimpleDateFormat("_yyyyMMdd_hh_mm_a");
		fileName = fileName.concat(format.format(new Date()));
		
		IFilePersistor filePersistor = FilePersistorFactory.getInstance();
		AttachedFile file = new AttachedFile();
		file.setName(fileName);
		file.setLibraryId(Esquema.getEsquemaApp().getNombre());
		file.setData(errores.toString().getBytes());
		filePersistor.save(file);
		
		String separador = "_OX_";
		this.fileErrorId = file.getId() + separador + file.getName() + separador + file.getLibraryId();	
	}*/
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setFileItems(List fileItems) {
		this.fileItems = fileItems;		
	}
	
	@Override
	public String[] getNextControllers() throws Exception {
		return DEFAULT_CONTROLLERS;		
	}
	

	@Override
	public String getCustomView() throws Exception {
		return DEFAULT_VIEW;
	}

	

	@Override
	public String getNextAction() throws Exception {
		if (Is.emptyString(this.fileErrorId)){
			return "Transaccion.editar";
		}
		else{
			return null;
		}		
	}
}
