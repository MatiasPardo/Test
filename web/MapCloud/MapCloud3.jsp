<!doctype html>

<html>

<head>
    <meta charset="utf-8"></meta>
    <title>MapCloud</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
	<style>
      /* Always set the map height explicitly to define the size of the div
       * element that contains the map. */
      #map {
        height: 100%;
      }
      /* Optional: Makes the sample page fill the window. */
      html, body {
        height: 100%;
        margin: 10;
        padding: 0;
      }
    </style>
    
</head>
		<script type="text/javascript" src="map/markerclusterer.js"></script>
		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&libraries=drawing"></script>
		
		<%@ page import="org.openxava.mapcloud.model.*" %>
		<%@ page import="java.util.*"%>
		<%@ page import="static java.lang.System.out" %>
		<%
		/*
		MapCloud mapCloud = (MapCloud)session.getAttribute("clouderp_MapCloud");
		LinkedList<AddressCloud> direcciones = new LinkedList<AddressCloud>();
		direcciones.addAll(mapCloud.getAddress());
		mapCloud.geocoding(direcciones.get(0));
		mapCloud.geocoding(direcciones.get(1));
		mapCloud.geocoding(direcciones.get(2));
		mapCloud.geocoding(direcciones.get(3));
		
		List<String> label = new LinkedList<String>();	
		List<String> description = new LinkedList<String>();
		
		
		
		for(AddressCloud address: direcciones){
			label.add("\""+address.getLabel()+"\"");
			}
		for(AddressCloud address: direcciones){
			description.add("\""+address.getDescription()+"\"");
			}
		*/
		%>
<body>
<%
	out.println("	 <div id=\"map\"></div>	");
	out.println("	    <script>	");
	
/*	out.println("	  var address1 = "+direcciones.get(0).getLatitud()+";	");
	out.println("	  var address2 = "+direcciones.get(0).getLongitud()+";	");
	out.println("	  var labels =  new Array();							");
	out.println("	      labels =  "+label+";								");
	out.println("	  var descriptions = new Array();						");
	out.println("	      descriptions =  "+description+";					");
	out.println("	  var zoom = "+mapCloud.getZoom()+";					");
*/
	out.println("	function initMap() {	");
	out.println("	        var map = new google.maps.Map(document.getElementById('map'), {	");
	out.println("	          center: {lat: 24.886, lng: -70.269},	");
	out.println("	          zoom: 5,	");
	out.println("	        });	");
	out.println("		");
	out.println("	        var triangleCoords = [	");
	out.println("	          {lat: 25.774, lng: -80.19},	");
	out.println("	          {lat: 18.466, lng: -66.118},	");
	out.println("	          {lat: 32.321, lng: -64.757}	");
	out.println("	        ];	");
	out.println("		");
	out.println("	        var bermudaTriangle = new google.maps.Polygon({paths: triangleCoords});	");
	out.println("		");
	out.println("	        google.maps.event.addListener(map, 'click', function(e) {	");
	out.println("	          var resultColor =	");
	out.println("	              google.maps.geometry.poly.containsLocation(e.latLng, bermudaTriangle) ?	");
	out.println("	              'blue' :	");
	out.println("	              'red';	");
	out.println("		");
	out.println("	          var resultPath =	");
	out.println("	              google.maps.geometry.poly.containsLocation(e.latLng, bermudaTriangle) ?	");
	out.println("	              // A triangle.	");
	out.println("	              \"m 0 -1 l 1 2 -2 0 z\" :	");
	out.println("	              google.maps.SymbolPath.CIRCLE;	");
	out.println("		");
	out.println("	          new google.maps.Marker({	");
	out.println("	            position: e.latLng,	");
	out.println("	            map: map,	");
	out.println("	            icon: {	");
	out.println("	              path: resultPath,	");
	out.println("	              fillColor: resultColor,	");
	out.println("	              fillOpacity: .2,	");
	out.println("	              strokeColor: 'white',	");
	out.println("	              strokeWeight: .5,	");
	out.println("	              scale: 10	");
	out.println("	            }	");
	out.println("	          });	");
	out.println("	        });	");
	out.println("	      }	");



	out.println("	    </script>	"); 
	
	
	out.println("	    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&libraries=drawing&callback=initMap\"	");
	out.println("	         async defer></script>	");

	out.println("	    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&libraries=geometry&callback=initMap\"	");
	out.println("	         async defer></script>	");
	
	out.println(" <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&callback=initMap\" ");
	out.println("		async defer></script> ");
		    

    %>
</body>