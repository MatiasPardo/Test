package org.openxava.base.actions;

import java.io.*;
import java.text.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.reportes.actions.*;
import org.openxava.util.*;
import org.openxava.util.Emails.*;
import org.openxava.validators.*;

public class EnvioEMailTransaccionAction extends ViewBaseAction{
	
	@Override
	public void execute() throws Exception {
		if (getView().isKeyEditable()){
			addError("primero_grabar");
		}
		else{
			Transaccion transaccion = (Transaccion) MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			if (transaccion.estadoValidoParaEnvioMail()){
				if (transaccion instanceof IDestinoEMail){
					IDestinoEMail trMail = (IDestinoEMail)transaccion;
					if (transaccion.configurador().getActivarEnvioMail()){
						ConfiguracionEMail configurador = trMail.emailConfigurador();
						String contenido = trMail.emailContenido();
						String asunto = trMail.emailAsunto();
						if (Is.emptyString(contenido)) contenido = asunto;
						
						String fullFileName = generarFileReporteTransaccion(transaccion);
						File file = new File(fullFileName);
						try{
							Attachment[] attachments = new Attachment[1];
							Attachment attach = new Attachment(file.getName(), file);
							attachments[0] = attach; 						
							configurador.enviarEMail(trMail.emailPara(), trMail.emailCC(), asunto, contenido, attachments);			
							addMessage("email enviado");
						}
						finally{
							try{
								file = new File(fullFileName);
								file.delete();								
							}
							catch(Exception e){							
							}
						}
					}
					else{
						addError("No esta activo la configuración de mail");
					}
				}
				else{
					addError("Comprobante no soporta envío de mail");
				}	
			}
			else{
				addError("No se puede enviar por mail en estado: " + transaccion.getEstado().toString());
			}
		}
	}
	
	private String generarFileReporteTransaccion(Transaccion transaccion){
		ReportTransaccionAction reportTransaccion = new ReportTransaccionAction();
		reportTransaccion.setView(this.getView());
		
		try{
			ReportGenerarArchivo report = new ReportGenerarArchivo();
						
			report.setJRXML(reportTransaccion.archivoJRXMLEnvioMail());
			report.setModelName(this.getModelName());
			report.setParametros(reportTransaccion.parametrosReporteEnvioMail());
			String fileName = transaccion.getId() + "_" + getView().getModelName() + "_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".pdf";		
			String fullFileName = ConfiguracionERP.pathConfig().concat(fileName);			
			report.generar(fullFileName);
			return fullFileName;
		}
		catch(Exception e){
			
			String error = e.getMessage();
			if (Is.emptyString(error)){
				error = e.toString();
			}
			throw new ValidationException("No se pudo generar el pdf: " + error);
		}
	}
}
