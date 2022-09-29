package org.openxava.base.model;

import java.io.*;

import org.openxava.application.meta.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class ConfiguracionERP {
	
	public static String esquemaDB(){
		if (Is.emptyString(Users.getCurrentUserInfo().getOrganization())){
			return "public";
		}
		else{
			return Users.getCurrentUserInfo().getOrganization();
		}
	}
	
	public static String pathConfigAplicacionDefault(){
		Object applicationName = MetaApplications.getApplicationsNames().iterator().next();				
		return new String("C:\\ConfigFEArg\\").concat(applicationName.toString()).concat("\\");
	}
	
	public static String pathConfig(){
		String organization = Users.getCurrentUserInfo().getOrganization();
		if (!Is.emptyString(organization)){
			String path = new String("C:\\ConfigFEArg\\").concat(organization).concat("\\");
			if (path.equalsIgnoreCase(ConfiguracionERP.pathConfigAplicacionDefault())){
				path = new String("C:\\ConfigFEArg\\").concat(organization).concat("SAS").concat("\\");
			}
			return path;
		}
		else{
			return ConfiguracionERP.pathConfigAplicacionDefault();
		}		
	}
	
	@SuppressWarnings("resource")
	public static String fullFileNameReporte(String nombreReporte){
		String fileName = ConfiguracionERP.pathConfig().concat(nombreReporte);
		if (Is.equalAsString(Users.getCurrent(), Esquema.getEsquemaApp().nombreUsuarioTest())){
			fileName = ConfiguracionERP.pathConfig().concat("Test_").concat(nombreReporte);
		}
		try{
			new FileInputStream(fileName);
			return fileName;
		}
		catch(FileNotFoundException e){
			String fileNameGenerico = ConfiguracionERP.pathConfigAplicacionDefault().concat(nombreReporte);
			if (Is.equalAsString(Users.getCurrent(), Esquema.getEsquemaApp().nombreUsuarioTest())){
				fileNameGenerico = ConfiguracionERP.pathConfig().concat("Test_").concat(nombreReporte);
			}
			try{
				new FileInputStream(fileNameGenerico);
				return fileNameGenerico;
			}
			catch(FileNotFoundException e2){
				throw new ValidationException("No se encontró " + fileName + " ni tampoco " + fileNameGenerico);
			}
		}		
	}
}
