package com.clouderp.maps.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;

import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.google.gson.*;

public class MapCloud {
	
	private final static String URL_GEOCODING = "https://maps.googleapis.com/maps/api/geocode/json";
	
	private String name;
	
	private double zoom = 13;
	
	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private String key;
	
	private Collection<AddressCloud> address = new LinkedList<AddressCloud>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<AddressCloud> getAddress() {		
		return address;
	}

	public void setAddress(Collection<AddressCloud> address) {
		if (address != null){
			this.address = address;
		}
	}

	public AddressCloud addAddress(double latitud, double longitud) {
		AddressCloud address = new AddressCloud();
		address.setLongitud(longitud);
		address.setLatitud(latitud);
		this.getAddress().add(address);
		return address;
	}
			
	public void geocoding(AddressCloud address) throws Exception{
		String direccion = address.completAddress();
		if (Is.emptyString(direccion)){
			throw new ValidationException("Dirección vacía");
		}
		if (Is.emptyString(this.getKey())){
			throw new ValidationException("Falta asignar Key");
		}
		
		URL url = new URL(MapCloud.URL_GEOCODING + "?address="+direccion+"&key=" + this.getKey());
		URLConnection con = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		try{
			String linea1;
			String linea = new String();
			while ((linea1 = in.readLine()) != null) 
			{
			    	linea = linea+linea1;
			}
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(linea);
			JsonObject results = element.getAsJsonObject();
			double lat = results.get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble();
			double lng = results.get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsDouble();
			
			String locationType = results.get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location_type").getAsString();
			
			
			if(locationType.equals("ROOFTOP") ||
				locationType.equals("RANGE_INTERPOLATED")) {
				address.setLatitud(lat);
				address.setLongitud(lng);
			}
		}
		finally{
			in.close();
		}
	}	
}
