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
		<script type="text/javascript" src="MapCloud/javascript/markerclusterer.js"></script>
		
		<%@ page import="com.clouderp.maps.model.*" %>
		<%@ page import="java.util.*"%>
		<%@ page import="static java.lang.System.out" %>
		<%
		
		MapCloud mapCloud = (MapCloud)session.getAttribute("clouderp.map");
		LinkedList<AddressCloud> direcciones = new LinkedList<AddressCloud>();
		direcciones.addAll(mapCloud.getAddress());
		List<String> label = new LinkedList<String>();	
		List<String> description = new LinkedList<String>();	
		
		for(AddressCloud address: direcciones){
			label.add("\""+address.getLabel()+"\"");
			}
		for(AddressCloud address: direcciones){
			description.add("\""+address.getDescription()+"\"");
			}
		
		%>
<body>
<%
	out.println("	 <div id=\"map\"></div>	");
	out.println("	    <script>	");
	
	out.println("	  var address1 = "+direcciones.get(0).getLatitud()+";	");
	out.println("	  var address2 = "+direcciones.get(0).getLongitud()+";	");
	out.println("	  var labels =  new Array();							");
	out.println("	      labels =  "+label+";								");
	out.println("	  var descriptions = new Array();						");
	out.println("	      descriptions =  "+description+";					");

	out.println("	  var zoom = "+mapCloud.getZoom()+";					");

	out.println("	function initMap() {	");
	out.println("																					");
	out.println("	        var map = new google.maps.Map(document.getElementById('map'), {			");
	out.println("	          	zoom: zoom,															");
	out.println("				disableDefaultUI: true,												");
	out.println("				zoomControl: true,													"); 	
	out.println("				fullscreenControl: true,											");
	out.println("				mapTypeControl: false,    											"); 
	out.println("	          	center: {lat: address1, lng: address2},								");
	out.println("	          	styles: [ 														");
	out.println("	          {					");
	out.println("	            featureType: 'poi.business',	");
	out.println("	            stylers: [{visibility: 'off'}]	");
	out.println("	          },	");
	out.println("	          {	");
	out.println("	            featureType: 'transit',	");
	out.println("	            elementType: 'labels.icon',	");
	out.println("	            stylers: [{visibility: 'off'}]	");
	out.println("	          }	");
	out.println("	        ]	");
	out.println("	        																		");
	out.println("	        });																		");
	out.println("	var markers = locations.map(function(location, i) {	");
	out.println("	          return new google.maps.Marker({	");
	out.println("	          position: location,	");
	out.println("	          label: labels[i],	");
	out.println("			  title: descriptions[i]		");
	out.println("	          });	");
	out.println("	        })	");
	out.println("		");
	out.println("	var markerCluster = new MarkerClusterer(map, markers,	");
	out.println("	            {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m' });	");
	out.println("	      }	");
	
	out.println("	      var locations = [	");
	for(AddressCloud address: direcciones)
	{
	out.println("	        {lat: "+ address.getLatitud() +", lng: "+address.getLongitud()+"},	"); 
	}
	out.println("	      ]	");
	

	
	out.println("	    </script>	"); 
	out.println(" <script src=\"MapCloud/javascript/markerclusterer.js\"> "); 
	out.println("   </script> "); 
	out.println(" <script src=\"https://maps.googleapis.com/maps/api/js?key="+mapCloud.getKey()+"&callback=initMap\" ");
	out.println("		async defer></script> ");
		    
   
    
    /*
    
    
    */
    %>
</body>