package org.openxava.mercadolibre.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.RowStyle;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Entity

@Tab(
		defaultOrder="${date_sent} desc",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="processed", value="true")
	})

public class NotificacionML {
	
	public static final String ORDERS = "/orders/";
	
	@Id @GeneratedValue(generator="system-uuid") @Hidden 
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(length=32)
	private String id;
	
	@Column(length=25)
	@ReadOnly
	private String sent;
	
	@Column(length=25)
	@ReadOnly
	private String recived;
	
	@Column(length=50)
	@ReadOnly
	private String resource;
	
	@Column(length=25)
	@ReadOnly
	private int user_id;
	
	@Column(length=25)
	@ReadOnly
	private String topic;
	
	@Column(length=25)
	@ReadOnly
	private long applications_id;
	
	@Stereotype("DATETIME")
	private Date date_sent;
	
	@Stereotype("DATETIME")
	private Date date_recived;

	private boolean	processed = false;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSent() {
		return sent;
	}

	public void setSent(String sent) {
		if (sent != null){
			this.setDate_sent(this.convertirADate(sent));
		}
		this.sent = sent;
	}

	public String getRecived() {
		return recived;
	}

	public void setRecived(String recived) {
		if(recived != null){
			this.setDate_recived(this.convertirADate(recived));
		}
		this.recived = recived;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public long getApplications_id() {
		return applications_id;
	}

	public void setApplications_id(long applications_id) {
		this.applications_id = applications_id;
	}

	public Date getDate_sent() {
		return date_sent;
	}

	public void setDate_sent(Date date_sent) {
		this.date_sent = date_sent;
	}

	public Date getDate_recived() {
		return date_recived;
	}

	public void setDate_recived(Date date_recived) {
		this.date_recived = date_recived;
	}
	
	private Date convertirADate(String string){
		String fecha = string.substring(0,10);
		String hora = string.substring(11,19);
		String gmt = string.substring(23,24);
		String fechaHoraGmt = new String(fecha+" "+hora+" "+gmt);
		SimpleDateFormat fech = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss XXX");
		try{
			return fech.parse(fechaHoraGmt);
		}
		catch(Exception e){
			throw new ValidationException("Error al convertir fecha: " + string.toString());
		}
	}
	
	public boolean getProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public static void registrarNotificacion(String stringJson, String esquema) throws Exception 
	{
		if (Is.emptyString(esquema)){
			throw new ValidationException("Esquema no asignado");
		}
		try{

			XPersistence.setDefaultSchema(esquema);
			
			NotificacionML notificacion = new NotificacionML();
			JsonElement jsonNotificacion = JsonParser.parseString(stringJson);
			JsonObject notificacionJson = jsonNotificacion.getAsJsonObject();
			notificacion.setResource(notificacionJson.get("resource").getAsString());
			notificacion.setUser_id(notificacionJson.get("user_id").getAsInt());
			notificacion.setTopic(notificacionJson.get("topic").getAsString());
			notificacion.setApplications_id(notificacionJson.get("application_id").getAsLong());
			notificacion.setSent(notificacionJson.get("sent").getAsString());
			notificacion.setRecived(notificacionJson.get("received").getAsString());
			ConfiguracionMercadoLibre.validarNotificacion(String.valueOf(notificacion.getApplications_id()));
			
			XPersistence.getManager().persist(notificacion);
			
			XPersistence.commit();
//			XHibernate.commit();
			
		}
		catch(Exception e){
			XPersistence.rollback();
			XPersistence.setDefaultSchema(esquema);
			NotificacionML.registrarError(stringJson, e.getMessage());
		}
	}
	
	public static void registrarError(String request, String exception) throws Exception 
	{
		ErroresMercadoLibre error = new ErroresMercadoLibre(request, exception);
		XPersistence.getManager().persist(error);
		XPersistence.commit();
//		XHibernate.commit();		
	}

	public String idResource() {
		if (this.getResource() != null){
			int index = this.getResource().indexOf("/orders/");
			if (index > -1){
				return this.getResource().substring(index + ORDERS.length());
			}
			throw new ValidationException("No se pudo obtener el id del recurso " + this.getResource());
		}
		else{
			throw new ValidationException("Resource no asignado");
		}
	}	
}
