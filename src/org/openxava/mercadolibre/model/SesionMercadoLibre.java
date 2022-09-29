package org.openxava.mercadolibre.model;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.Mercadolibre.ConexionMeli;
import org.openxava.jpa.XPersistence;
import org.openxava.util.*;

import com.tiendanube.base.TiendaNube;

public class SesionMercadoLibre {
	
	private ConexionMeli conexionMeli = null;
	
	private TiendaNube tiendaNube;
	
	public ConexionMeli conectar(ConfiguracionMercadoLibre configuracionMercadLibre){		
		if (this.conexionMeli == null){
			this.conexionMeli = new ConexionMeli();
		}else if(!Is.equal(this.conexionMeli.getAppID(),configuracionMercadLibre.getAppId())){
			this.conexionMeli = new ConexionMeli();
		}
		configuracionMercadLibre.conectarMercadoLibre(this.conexionMeli);

		return this.conexionMeli;
	}
	
	public TiendaNube conectarTiendaNube(ConfiguracionMercadoLibre configuracionTiendaNube){	
		if (this.tiendaNube == null){
			this.tiendaNube = configuracionTiendaNube.conectarTiendaNube();
		}else if (!Is.equal(this.tiendaNube.getClient().getApiCredentials().getStoreId(),configuracionTiendaNube.getStoreID())){
			this.tiendaNube = configuracionTiendaNube.conectarTiendaNube();
		}
		return this.tiendaNube;

	}
	
	public NotificacionML bloquearNotificacion(String id) throws Exception{
		Query query = XPersistence.getManager().createQuery("from NotificacionML where id = :id");
		query.setParameter("id", id);
		query.setMaxResults(1);
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		
		return (NotificacionML)query.getSingleResult();		
	}
	
}
