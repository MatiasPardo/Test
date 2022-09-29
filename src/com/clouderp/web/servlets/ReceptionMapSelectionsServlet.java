package com.clouderp.web.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openxava.hibernate.XHibernate;
import org.openxava.jpa.XPersistence;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IMapPolygonAction;
import com.clouderp.maps.model.MapCloud;
import com.google.gson.*;


public class ReceptionMapSelectionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private String resultPolygonAction = "";
	
    public ReceptionMapSelectionsServlet() {
        super();
          
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(this.resultPolygonAction);		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder resultado = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader( request.getInputStream() ) );
		String linea;
			while ((linea = rd.readLine()) != null) {
			resultado.append(linea);
			}
		rd.close();
		MapCloud map = (MapCloud) request.getSession().getAttribute("clouderp.map");
		JsonObject obj = new JsonObject();
		JsonParser parser = new JsonParser();
		obj = (JsonObject) parser.parse(resultado.toString());		
		JsonArray ids = obj.get("ids").getAsJsonArray();
		
		
		Map<String, Object> codigosDirecciones = new HashMap<String, Object>();
		for(int i=0; i < ids.size(); i ++){
			if (!codigosDirecciones.containsKey(ids.get(i))){
				codigosDirecciones.put(ids.get(i).getAsString(), null);
			}			
		}
		
		Collection<AddressCloud> addressSelected = new LinkedList<AddressCloud>();
		for(AddressCloud address: map.getAddress()){
			if (codigosDirecciones.containsKey(address.getCodigo())){
				addressSelected.add(address);
			}
		}
		
		this.resultPolygonAction = this.executeMapPolygonAction(request, addressSelected, map);
			
		doGet(request, response);
	}
	
	private String executeMapPolygonAction(HttpServletRequest request, Collection<AddressCloud> address, MapCloud map){
		String result = "";
		try{
			String organization = (String) request.getSession().getAttribute("naviox.organization");
			XPersistence.setDefaultSchema(organization);
			
			Class<?> classPolygonAction = (Class<?>)request.getSession().getAttribute("clouderp.mapaction");
			IMapPolygonAction action = (IMapPolygonAction) classPolygonAction.newInstance();
			result = action.doPolygonAction(address, map, request.getSession());
			
			XPersistence.commit();
			XHibernate.commit();
		}
		catch(Exception e){
			result = "Error: " + e.toString();
			
			XPersistence.rollback();
			XHibernate.rollback();
		}
		
		return result;
	}
}
