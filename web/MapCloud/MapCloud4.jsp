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
		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCsNnBIQQxagkRQFUj51z0XaPtN5QS4_w0&libraries=drawing"></script>
		
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
	
	out.println("	<div id=\"map\" height=\"460px\" width=\"100%\"></div>	");
	out.println("	    <div id=\"form\">	");
	out.println("	      <table>	");
	out.println("	      <tr><td>Name:</td> <td><input type='text' id='name'/> </td> </tr>	");
	out.println("	      <tr><td>Address:</td> <td><input type='text' id='address'/> </td> </tr>	");
	out.println("	      <tr><td>Type:</td> <td><select id='type'> +	");
	out.println("	                 <option value='bar' SELECTED>bar</option>	");
	out.println("	                 <option value='restaurant'>restaurant</option>	");
	out.println("	                 </select> </td></tr>	");
	out.println("	                 <tr><td></td><td><input type='button' value='Save' onclick='saveData()'/></td></tr>	");
	out.println("	      </table>	");
	out.println("	    </div>	");
	out.println("	    <script>	");
	
/*	out.println("	  var address1 = "+direcciones.get(0).getLatitud()+";	");
	out.println("	  var address2 = "+direcciones.get(0).getLongitud()+";	");
	out.println("	  var labels =  new Array();							");
	out.println("	      labels =  "+label+";								");
	out.println("	  var descriptions = new Array();						");
	out.println("	      descriptions =  "+description+";					");
	out.println("	  var zoom = "+mapCloud.getZoom()+";					");
*/
	out.println("	      var map;	");
	out.println("	      var marker;	");
	out.println("	      var infowindow;	");
	out.println("	      var messagewindow;	");
	out.println("		");
	out.println("	      function initMap() {	");
	out.println("	        var california = {lat: 37.4419, lng: -122.1419};	");
	out.println("	        map = new google.maps.Map(document.getElementById('map'), {	");
	out.println("	          center: california,	");
	out.println("	          zoom: 13	");
	out.println("	        });	");
	out.println("		");
	out.println("	        infowindow = new google.maps.InfoWindow({	");
	out.println("	          content: document.getElementById('form')	");
	out.println("	        });	");
	out.println("		");
	out.println("	        messagewindow = new google.maps.InfoWindow({	");
	out.println("	          content: document.getElementById('message')	");
	out.println("	        });	");
	out.println("		");
	out.println("	        google.maps.event.addListener(map, 'click', function(event) {	");
	out.println("	          marker = new google.maps.Marker({	");
	out.println("	            position: event.latLng,	");
	out.println("	            map: map	");
	out.println("	          });	");
	out.println("		");
	out.println("		");
	out.println("	          google.maps.event.addListener(marker, 'click', function() {	");
	out.println("	            infowindow.open(map, marker);	");
	out.println("	          });	");
	out.println("	        });	");
	out.println("	      }	");
	out.println("		");
	out.println("	      function saveData() {	");
	out.println("	        var name = escape(document.getElementById('name').value);	");
	out.println("	        var address = escape(document.getElementById('address').value);	");
	out.println("	        var type = document.getElementById('type').value;	");
	out.println("	        var latlng = marker.getPosition();	");
	out.println("	        var latlng = marker.getPosition();	");
	out.println("	        var url = 'phpsqlinfo_addrow.php?name=' + name + '&address=' + address +	");
	out.println("	                  '&type=' + type + '&lat=' + latlng.lat() + '&lng=' + latlng.lng();	");
	out.println("		alert(type);											");
	out.println("	        downloadUrl(url, function(data, responseCode) {		");
	out.println("																");
	out.println("	          if (responseCode == 200 && data.length <= 1) {	");
	out.println("	            infowindow.close();								");
	out.println("	            messagewindow.open(map, marker);				");
	out.println("	          }	");
	out.println("	        });	");
	out.println("	      }	");
	out.println("		");
	out.println("	      function downloadUrl(url, callback) {	");
	out.println("	        var request = window.ActiveXObject ?	");
	out.println("	            new ActiveXObject('Microsoft.XMLHTTP') :	");
	out.println("	            new XMLHttpRequest;	");
	out.println("		");
	out.println("	        request.onreadystatechange = function() {	");
	out.println("	          if (request.readyState == 4) {	");
	out.println("	            request.onreadystatechange = doNothing;	");
	out.println("	            callback(request.responseText, request.status);	");
	out.println("	          }	");
	out.println("	        };	");
	out.println("		");
	out.println("	        request.open('GET', url, true);	");
	out.println("	        request.send(null);	");
	out.println("	      }	");
	out.println("		");
	out.println("	      function doNothing () {	");
	out.println("	      }	");
	out.println("		");

	out.println("	    </script>	"); 
	
	//----------------------------------	libraries		---------------------------------------------------------
	
	out.println("	    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCsNnBIQQxagkRQFUj51z0XaPtN5QS4_w0&libraries=drawing&callback=initMap\"	");
	out.println("	         async defer></script>	");

	out.println("	    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCsNnBIQQxagkRQFUj51z0XaPtN5QS4_w0&libraries=geometry&callback=initMap\"	");
	out.println("	         async defer></script>	");
	
	out.println(" <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyCsNnBIQQxagkRQFUj51z0XaPtN5QS4_w0&callback=initMap\" ");
	out.println("		async defer></script> ");
		    

    %>
</body>