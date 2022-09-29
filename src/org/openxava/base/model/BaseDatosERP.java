package org.openxava.base.model;

import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.jpa.*;

public class BaseDatosERP {
	
	public static void getColumnasTabla(String tabla, String esquema, ArrayList<String> columnas){
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT column_name FROM information_schema.columns ");
		sql.append("WHERE table_schema ilike :esquema ");       
		sql.append("AND table_name ilike :tabla "); 
		sql.append("ORDER BY ordinal_position asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("esquema", esquema);
		query.setParameter("tabla", tabla);
		
		List<?> results = query.getResultList();
		for(Object res: results){
			columnas.add((String)res);
		}		
	}

	public static String valorBoolean(boolean bool) {
		if (bool){
			return "'t'";
		}
		else{
			return "'f'";
		}		
	}

	public static String valorInteger(Integer numero){
		return String.format("%d", numero);
		
	}
	
	public static String valorDateTime(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm");
		return "'" + format.format(date) + "'";
	}

	public static String valorString(String str) {
		return "'" + str + "'";
	}
	
	public static String crearGUID(){
		return "replace(cast(uuid_in(md5(random()\\:\\:text || now()\\:\\:text)\\:\\:cstring) as text), '-', '')";
	}
}	
