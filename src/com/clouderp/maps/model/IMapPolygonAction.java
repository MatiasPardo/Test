package com.clouderp.maps.model;

import java.util.Collection;

import javax.servlet.http.HttpSession;

public interface IMapPolygonAction {
	
	public String doPolygonAction(Collection<AddressCloud> selected, MapCloud map, HttpSession session);
	
}
