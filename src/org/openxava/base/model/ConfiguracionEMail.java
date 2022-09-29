package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.util.*;
import org.openxava.util.Emails.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(members="userMail, userPassword; configuracionSMTP")
})

public class ConfiguracionEMail extends ObjetoNegocio{

	@Column(length=50)
	@Required
	private String userMail;
	
	@Column(length=30)
	@Stereotype("PASSWORD")
	@Hidden
	@Required
	private String userPassword;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	private ConfiguracionSMTP configuracionSMTP;

	public String getUserMail() {
		return userMail;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public ConfiguracionSMTP getConfiguracionSMTP() {
		return configuracionSMTP;
	}

	public void setConfiguracionSMTP(ConfiguracionSMTP configuracionSMTP) {
		this.configuracionSMTP = configuracionSMTP;
	}

	public void enviarEMail(String emailPara, String emailCC, String asunto, String contenido, Attachment[] attachments) {
		if (Is.emptyString(emailPara) && Is.emptyString(emailCC)){
			throw new ValidationException("No hay destinatario");
		}
		
		try{
			Emails.sendFaceArg(this.getConfiguracionSMTP().getHost(), this.getConfiguracionSMTP().getPort(), this.getUserMail(), this.getUserPassword(), 
				this.getConfiguracionSMTP().getHostTrusted(), this.getConfiguracionSMTP().getStartTLSenable(), 
				this.getUserMail(), emailPara, emailCC, asunto, contenido, attachments);
		}
		catch(Exception e){
			throw new ValidationException("Error al enviar email: " + e.toString());
		}
				
	}
}
