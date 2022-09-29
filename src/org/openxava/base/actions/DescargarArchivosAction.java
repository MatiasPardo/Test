package org.openxava.base.actions;

import java.io.StringWriter;
import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.validators.*;
import org.openxava.web.editors.*;

public class DescargarArchivosAction extends ViewBaseAction implements IMultipleForwardAction{

	private List<String> filesID = new LinkedList<String>();
	
	private List<String> getFilesID(){
		return this.filesID;
	}
	
	// Graba el archivo devolviendo un FileID para la URL
	public static String grabarArchivo(String fileName, StringWriter datos){
		IFilePersistor filePersistor = FilePersistorFactory.getInstance();
		AttachedFile file = new AttachedFile();
		SimpleDateFormat format = new SimpleDateFormat(" yyyyMMdd_hh_mm_a");		
		file.setName(fileName.concat(format.format(new Date())));
		file.setLibraryId(Esquema.getEsquemaApp().getNombre());
		file.setData(datos.toString().getBytes());
		filePersistor.save(file);
		
		String separador = "_OX_";
		return file.getId() + separador + file.getName() + separador + file.getLibraryId();
	}
	
	@Override
	public String[] getForwardURIs() {
		if (!this.getFilesID().isEmpty()){
			String[] URIs = new String[this.getFilesID().size()];
			int i = 0;
			for(String fileID: this.filesID){
				String uri = "/xava/xfile" + 
						"?application=" + getRequest().getParameter("application") +
						"&module=" + getRequest().getParameter("module") +
						"&time=" + System.currentTimeMillis() +
						"&fileId=" + fileID;
				URIs[i] = uri;
				i++;
			}
			return URIs;
		}
		else{
			throw new ValidationException("No hay archivos para descargar");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		ArrayList<String> ids = (ArrayList<String>) getRequest().getAttribute(("facearg_filesID"));
		if (ids == null){
			throw new ValidationException("Falta asignar el parámetro facearg_filesID");
		}
		else if (ids.isEmpty()){
			throw new ValidationException("Vacio el parámetro facearg_filesID");
		}
		else{
			this.filesID = ids;
		}
		getRequest().removeAttribute("facearg_filesID");
	}	
}
