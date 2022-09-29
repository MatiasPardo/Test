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
		<script type="text/javascript" src="../map/markerclusterer.js"></script>
		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&libraries=drawing"></script>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
		<%@ page import="com.clouderp.maps.model.*" %>
		<%@ page import="java.util.*"%>
		<%@ page import="static java.lang.System.out" %>
<%
		//<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
		MapCloud mapCloud = (MapCloud)session.getAttribute("clouderp.map");
		LinkedList<AddressCloud> direcciones = new LinkedList<AddressCloud>();
		direcciones.addAll(mapCloud.getAddress());
				
		List<String> label = new LinkedList<String>();	
		List<String> description = new LinkedList<String>();
		List<String> id = new LinkedList<String>();
		List<String> markerBool = new LinkedList<String>();
		
		for(AddressCloud address: direcciones){
			label.add("\""+address.getLabel()+"\"");
			description.add("\""+address.getDescription()+"\"");
			id.add("\""+address.getCodigo()+"\"");
			}
		for(AddressCloud address: direcciones){
				if(address.isAsignado()){
					String bool = new String(""+address.isAsignado()+"");
					markerBool.add("\""+bool+"\"");
				}else { markerBool.add("\""+"false"+"\""); }
			}
		
		
		%>
<body>
<%
	out.println("<button class=\"myButton\" id=\"button1\">Enviar Direcciones</button>							");
	out.println("	 <div id=\"map\"></div>																		");
	out.println("	    <script>																				");
	out.println("	    var marker;																				");
	out.println("	    var infowindow;																			");
	out.println("	    var messagewindow;																		");
	out.println("	  	var address1 = "+direcciones.get(0).getLatitud()+";										");
	out.println("	  	var address2 = "+direcciones.get(0).getLongitud()+";									");
	out.println("	  	var labels =  new Array();																");
	out.println("	      	labels =  "+label+";																");
	out.println("	  	var descriptions = new Array();															");
	out.println("	      	descriptions =  "+description+";													");
	out.println("	  	var ids = new Array();																	");
	out.println("	      	ids =  "+id+";																		");
	out.println("	  	var zoom = "+mapCloud.getZoom()+";														");
	out.println("	    var jsondata;																			");
	out.println("	    var json;																				");
	out.println("	    var image = new Array();																");
	out.println("	    var markerBool = new Array();															");
	out.println("	    	markerBool = "+markerBool+";														");
	out.println(" 												");
	out.println("		   										");
	out.println("		    									");
	out.println("	  	var imageGris = {																		");
	out.println("	       		url: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',			");
/*	out.println("	       		url: 'C:/Desarrollo/MiOpenxava/workspace/CloudERP/web/Map/blue-dot.png',			");
	out.println("	       		size: new google.maps.Size(120, 120),	 										");
	out.println("	       		origin: new google.maps.Point(0, 0),	 										");
	out.println("	       		anchor: new google.maps.Point(10, 34),			 								");
	out.println("	       		scaledSize: new google.maps.Size(35, 35),			 							");
*/	out.println("	       		labelOrigin: new google.maps.Point(15, -5)										");
	out.println("	       		};																				");
	out.println("	  	var imageRoja = {																		");
	out.println("	       		url: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png',					");
/*	out.println("	       		size: new google.maps.Size(10, 10),	 											");
	out.println("	       		origin: new google.maps.Point(6, 6),	 										");
	out.println("	       		anchor: new google.maps.Point(10, 34),			 								");
	out.println("	       		scaledSize: new google.maps.Size(50, 60),			 							");
*/	out.println("	       		labelOrigin: new google.maps.Point(15, -5),									");
	out.println("	    		};																				");
	out.println("		for(var e=0; markerBool.length > e;e++){												");
	out.println("	    	if(markerBool[e] === \"true\") {     												");
	out.println("	       	image[e]=imageGris;																	");
	out.println("	    	}else {image[e] = imageRoja; }  													");
	out.println("	    }  																						");

			  
	out.println("	function initMap() {															");
	out.println("																					");
	out.println("	        var map = new google.maps.Map(document.getElementById('map'), {			");
	out.println("	          	zoom: zoom,															");
	out.println("				disableDefaultUI: true,												");
	out.println("				zoomControl: true,													"); 	
	out.println("				fullscreenControl: true,											");
	out.println("				mapTypeControl: false,    											"); 
	out.println("	          	center: {lat: address1, lng: address2},								");
	out.println("	          	styles: [ 															");
	out.println("	          	{																	");
	out.println("	            featureType: 'poi.business',										");
	out.println("	            stylers: [{visibility: 'off'}]										");
	out.println("	          	},																	");
	out.println("	          	{																	");
	out.println("	            featureType: 'transit',												");
	out.println("	            elementType: 'labels.icon',											");
	out.println("	            stylers: [{visibility: 'off'}]										");
	out.println("	          	}																	");
	out.println("	        	]																	");
	out.println("	        });																		");
	out.println("			var markers = locations.map(function(location, i) {						");
	out.println("	          	return new google.maps.Marker({										");
	out.println("	          	position: location,													");
	out.println("	          	animation: google.maps.Animation.DROP,								");
	out.println("	       		icon: image[i],	 													");
	out.println("	          	label: labels[i],													");
	out.println("			  	title: descriptions[i],												");
	out.println("	          	 });																	");
	out.println("	        })																		");
	out.println("			var markerCluster = new MarkerClusterer(map, markers,					");
	out.println("	            {imagePath: 'https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/m' });");
	out.println("	  		var drawingManager = new google.maps.drawing.DrawingManager({			");
	out.println("	    		drawingControl: true,												");
	out.println("	    		drawingControlOptions: {											");
	out.println("	      			position: google.maps.ControlPosition.TOP_CENTER,				");
	out.println("	      			drawingModes: ['polygon']										");
	out.println("					},																");
	out.println("				polygonOptions: {													");
	out.println("	 		 		strokeColor: '#AAB6611',										");
	out.println("          			strokeOpacity: 0.8,												");
	out.println("	          		strokeWeight: 3,												");
	out.println("	          		fillOpacity: 0.35,												");
	out.println("	    			fillColor: '#FF1011'}											");
	out.println("	  																				");
	out.println("	    	});																		");
	out.println("	  		drawingManager.setMap(map);												");
	out.println("			google.maps.event.addListener(drawingManager, 'overlaycomplete', function(event) {	");
	out.println("			var seleccionlatlng = new Array();										");
	out.println("			var seleccionlabels = new Array();										");
	out.println("			var selecciondescriptions = new Array();								");
	out.println("			var seleccionids = new Array();							");
	out.println("																");
	out.println("	  				var	polygon = event.overlay;							");
	out.println("	   				for(var i=0; locations.length > i;i++){					 ");
	out.println("	  					if(google.maps.geometry.poly.containsLocation(new google.maps.LatLng(locations[i]), polygon)){	");
	out.println("					 		seleccionlatlng.push(locations[i]);												");
	out.println("				 			seleccionlabels.push(labels[i]);												");
	out.println("				 			selecciondescriptions.push(descriptions[i]);									");
	out.println("				 			seleccionids.push(ids[i]);									");
	out.println("						 												");
	out.println("	   					}			 										");
	out.println("	   				}				 									");
	out.println("			json = JSON.stringify({ locations: seleccionlatlng, labels:seleccionlabels, descriptions: selecciondescriptions, ids: seleccionids});");
	out.println("	 		});																");
	out.println("	}							");
	
	out.println("	      var locations = [	");
	for(AddressCloud address: direcciones)
	{
	out.println("	        {lat: "+ address.getLatitud() +", lng: "+address.getLongitud()+"},	"); 
	}
	out.println("	      ];	");
	out.println("</script>							"); 
	out.println("<script>												"); 
	
	out.println("																		");
	out.println("	$('.myButton').click(function() {																	");
	out.println("		");
	out.println("		$.ajax({										");
	out.println("				method: \"POST\",						");
	out.println("				url: \"mapcloud/poligono\",				");
	out.println("				data: 	json,							");
	out.println("				success: function(data) {				");
	out.println("							alert(data);				");
	out.println("							}							");
	out.println("		}); 											");
	out.println("														");
	out.println("	 });												");
	out.println("       	</script>									"); 
	out.println(" <script src=\"https://developers.google.com/maps/documentation/javascript/examples/markerclusterer/markerclusterer.js\"> "); 
	out.println("   </script> "); 
	out.println(" <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCZBrnA3y9XduM-9S7aGhMs1Isaqmzt5lY&libraries=drawing&callback=initMap\" ");
	out.println("		async defer></script> ");
	out.println("	    </script>	"); 

    %>
</body>