<!doctype html>

<html>

<head>
    <meta charset="utf-8"></meta>
    <title>FlowCloud</title>
    <script src="FlowCloud/javascript/cytoscape.js"></script>
    <script src="FlowCloud/javascript/jquery.min.js"></script>
	<script src="FlowCloud/javascript/jquery.qtip.min.js"></script>
	<script src="FlowCloud/javascript/cytoscape-qtip.js"></script>
    <script src="FlowCloud/javascript/dagre.min.js"></script>
    <script src="FlowCloud/javascript/cytoscape-dagre.js"></script>
    <link href="FlowCloud/estilos/jquery.qtip.min.css" rel="stylesheet" type="text/css" />
    <link href="FlowCloud/estilos/style.css" rel="stylesheet" />
    
</head>

		<%@ page import="org.openxava.flowchart.model.*" %>
		<%@ page import="java.util.*"%>
		<%@ page import="static java.lang.System.out" %>
		<%
		
		FlowCloud flowCloud = (FlowCloud)session.getAttribute("clouderp_flowchart");
		LinkedList<LinkCloud> conexiones = new LinkedList<LinkCloud>();
		conexiones.addAll(flowCloud.getLink());
		List<String> nameVinculo = new LinkedList<String>();
		List<String> vinculo = new LinkedList<String>();

		
		//poder leer el link que estoy parado
		%>
<body>
<%
	out.println("	    <script>				");
	out.println("    var loc = window.location;																							");
	out.println("    var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);										");
	out.println("    var absolut = loc.href.substring(0, loc.href.length - ((loc.pathname + loc.search + loc.hash).length - pathName.length));	");
	out.println("	 																						");
	out.println("	    </script>				");

	out.println("	 <div id=\"cy\"></div>		");
	out.println("	    <script>				");
	out.println("  ");
    out.println("	var cy = cytoscape({	");
    out.println("	          container: document.getElementById('cy'),	");
    out.println("	          style: 	");
    out.println("	[{	");
    out.println("	  'selector': 'core',	");
    out.println("	  'style': {	");
    out.println("	    'selection-box-color': '#AAD8FF',	");
    out.println("	    'selection-box-border-color': '#8BB0D0',	");
    out.println("	    'selection-box-opacity': '0.5'	");
    out.println("	  }	");
    out.println("	}, {	");
    out.println("	  'selector': 'node',	");
    out.println("	  'style': {	");
    out.println("	    'width': 'mapData(score, 100, 100.006769776522008331, 40, 600)',	");
    out.println("	    'height': 'mapData(score, 100, 100.006769776522008331, 40, 600)',	");
    out.println("	    'content': 'data(id)',	");
    out.println("	    'font-size': '12px',	");
    out.println("	    'text-valign': 'center',	");
    out.println("	    'text-halign': 'center',	");
    out.println("	    'background-color': '#555',	");
    out.println("	    'text-outline-color': '#555',	");
    out.println("	    'text-outline-width': '2px',	");
    out.println("	    'color': '#fff',	");
    out.println("	    'overlay-padding': '6px',	");
    out.println("	    'z-index': '10'	");
    out.println("	  }	");
    out.println("	}, 	");
    out.println("	{  'selector': 'node[?attr]',	");
    out.println("	  'style': {	");
    out.println("	    'shape': 'rectangle',	");
    out.println("	    'background-color': '#aaa',	");
    out.println("	    'text-outline-color': '#aaa',	");
    out.println("	    'width': '99px',	");
    out.println("	    'height': '16px',	");
    out.println("	    'font-size': '6px',	");
    out.println("	    'z-index': '1'	");
    out.println("	  }	");
    out.println("	},	");
    out.println("	  {'selector': 'node[?query]',	");
    out.println("	  'style': {	");
    out.println("	    'background-clip': 'none',	");
    out.println("	    'background-fit': 'contain'	");
    out.println("	  }	");
    out.println("	}, {	");
    out.println("	  'selector': 'node:selected',	");
    out.println("	  'style': {	");
    out.println("	    'border-width': '6px',	");
    out.println("	    'border-color': '#AAD8FF',	");
    out.println("	    'border-opacity': '0.5',	");
    out.println("	    'background-color': '#77828C',	");
    out.println("	    'text-outline-color': '#77828C'	");
    out.println("	  }	");
    out.println("	}, {	");
    out.println("	  'selector': 'edge',	");
    out.println("	  'style': {	");
    out.println("	    'target-arrow-shape': 'triangle',	");
  	out.println("	    'text': '#9dbaea',");
  	out.println("	    'font-size': '10',");
    out.println("	    'curve-style': 'bezier',	");
    out.println("	    'haystack-radius': '0.5',	");
    out.println("	    'opacity': '1',	");
    out.println("	    'line-color': '#d0b7d5',	");
    out.println("	    'width': 'mapData(weight, 6, 6, 5, 8)',	");
    out.println("	    'overlay-padding': '3px',	");
    out.println("	    'content': 'data(line_name)'	");
    out.println("	  }	");
    out.println("	},	");
    out.println("	  {'selector': 'node.unhighlighted',	");
    out.println("	  'style': {	");
    out.println("	    'opacity': '0.2'	");
    out.println("	  }	");
    out.println("	},");

    out.println("	{");
    out.println("	  'selector': 'edge.unhighlighted',	");
    out.println("	  'style': {	");
    out.println("	    'opacity': '0.05'	");
    out.println("	  }	");
    out.println("	}, {	");
    out.println("	  'selector': '.highlighted',	");
    out.println("	  'style': {	");
    out.println("	    'z-index': '999999'	");
    out.println("	  }	");
    out.println("	}, {	");
    out.println("	  'selector': 'node.highlighted',	");
    out.println("	  'style': {	");
    out.println("	    'border-width': '6px',	");
    out.println("	    'border-color': '#AAD8FF',	");
    out.println("	    'border-opacity': '0.5',	");
    out.println("	    'background-color': '#394855',	");
    out.println("	    'text-outline-color': '#394855'	");
    out.println("	  }	");
    out.println("	},	");
    out.println("	 { 'selector': 'edge.filtered',	");
    out.println("	  'style': {	");
    out.println("	    'opacity': '0'	");
    out.println("	  }}]}); 	");
    out.println("	        	");
    out.println("	        var i =1;	");
	for(LinkCloud link: conexiones){
	out.println("	    	");
    out.println("	        		");
    out.println("	        	i++	");
    out.println("	       	cy.add({	");
    out.println("	                data: { id: '"+link.getNodeStart().getName()+"',name:'"+link.getNodeStart().getDescription()+"',url:'"+link.getNodeStart().getLink()+"',shared_name:'"+link.getNodeStart().getNameLink()+"' }	");
    out.println("	                }	");
    out.println("	            );	");
    out.println("	        	cy.add({	");
    out.println("	                data: { id: '"+link.getNodeFinish().getName()+"',name:'"+link.getNodeFinish().getDescription()+"',url:'"+link.getNodeFinish().getLink()+"',shared_name:'"+link.getNodeFinish().getNameLink()+"'  }	");
    out.println("	                }	");
    out.println("	            );	");
    out.println("	            cy.add({	");
    out.println("	                data: {	");
    out.println("	                    id: 'edge' + i,	");
    out.println("	                    source: '"+link.getNodeStart().getName()+"',	");
    out.println("	                    target: '"+link.getNodeFinish().getName()+"'	");
    									if(link.getDescription()== null){
    									}else{	
    									out.println(",line_name: '"+link.getDescription()+"'	");
    									}

    out.println("	                }	");
    out.println("	            });	");
    }
	out.println("	   	");
    out.println("	 cy.nodes().forEach(function(n){	");
    				
    out.println("		      var name = n.data('name');	");	  //guardo el nombre del nodo
    out.println("		      var id = n.data('id');	");		 //guardo el id del nodo
    out.println("		      var url = n.data('url');	");		 //guardo la url del nodo
    out.println("		      var nameLink = n.data('shared_name');	");		 //guardo el name link del nodo 

    out.println("		      n.qtip({	");
    out.println("		        style: {	");
    out.println("		          classes: 'qtip-bootstrap',	");
    out.println("		          tip: {	");
    out.println("		            width: 16,	");
    out.println("		            height: 8	");
    out.println("		          }		");
    out.println("		        },		");
    out.println("		        position: {");
    out.println("		          my: 'top center',	");
    out.println("		          at: 'bottom center'	");
    out.println("		        },	");
    out.println("		        content: 	");
    out.println("		         [ {	");
    out.println("		            name: name,");
    out.println("		            url: url,");
    out.println("		            namelink: nameLink");
    out.println("		          }	");
    out.println("		        ].map(function( link ){																			");
    //out.println("  	          																								");
    out.println("			 return name+' <a target=\"_blank\" href=\"' + link.url + '\">'+ link.namelink +'</a>';				 ");
    out.println("		        }).join('<br />')	");
    out.println("		      });		");
    out.println("		    });			");
    out.println(" var layout = cy.layout({ name: 'dagre'});");
    out.println("	        layout.run();");
    out.println("	    </script>	");
    
    /*
    
    
    */
    %>
</body>