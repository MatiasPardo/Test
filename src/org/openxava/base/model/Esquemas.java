package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.jpa.*;

public class Esquemas {
	
	/*final private static ThreadLocal<Esquema> current = new ThreadLocal<Esquema>();
	
	public static Esquema getEsquemaApp(){
		Esquema miEsquema = current.get();
		if (miEsquema == null){
			miEsquema = Esquemas.buscarEsquema(); 
			current.set(miEsquema);
		}
		return miEsquema;			
	}*/
	
	public static Esquema getEsquemaApp(){
		return buscarEsquema();
	}
	
	private static Esquema buscarEsquema(){
		Query query = XPersistence.getManager().createQuery("from Esquema");
		query.setFlushMode(FlushModeType.COMMIT);
		return (Esquema)query.getSingleResult();
	}
}
