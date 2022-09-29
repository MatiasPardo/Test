<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>CloudERP DashBoard</title>
	
	<link rel="stylesheet" href="DashBoard/Style/bootstrap.min.css"> 

	<link rel="stylesheet" href="DashBoard/Style/font-awesome.min.css">
	<link rel="stylesheet" href="DashBoard/Style/css.css">
	<link rel="stylesheet" href="DashBoard/Style/styles.css">

</head>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
		
<%@ page import="com.clouderp.dashboard.model.*" %>
		<%@ page import="java.util.*"%>
		<%@ page import="static java.lang.System.out" %>
<%
		DashBoard dashBoard= (DashBoard)session.getAttribute("clouderp_DashBoard");
		
		LinkedList<RowDashBoard> rows = new LinkedList<RowDashBoard>();
		rows.addAll(dashBoard.getRows());
		LinkedList<DataDashBoard> sparks = new LinkedList<DataDashBoard>();
		LinkedList<DataDashBoard> bars = new LinkedList<DataDashBoard>();
		LinkedList<DataDashBoard> donas = new LinkedList<DataDashBoard>();
		LinkedList<DataDashBoard> mixs = new LinkedList<DataDashBoard>();
		LinkedList<DataDashBoard> rads = new LinkedList<DataDashBoard>();

		for(RowDashBoard row: rows){
			//agregar validacion si la fila viene vacia
			for(DataDashBoard dashboard: row.getGraphics()){ 
			if(dashboard.getType().equals(TypeDataDashBoard.Spark)){
				sparks.add(dashboard);
			}	else if(dashboard.getType().equals(TypeDataDashBoard.Bar)){
				bars.add(dashboard);
				} 	else if(dashboard.getType().equals(TypeDataDashBoard.Dona)){
					donas.add(dashboard);
					} 	else if(dashboard.getType().equals(TypeDataDashBoard.Mixed)){
						mixs.add(dashboard);
						} 	else {
							rads.add(dashboard);
							}
			}
		}
		int i = 0;
		int k = 0;
		int z = 0;
		out.println(" <script>					");

		//**************mixed***************************/
		
		out.println(" var mixed = new Array();	");
		out.println(" var jsonItemM = JSON.parse('[]');	");
		for(DataDashBoard mix: mixs){
			out.println("  jsonItemM = JSON.parse('[]');	");
			for(SerieDashBoard serMix: mix.getSeries()){
				out.println(" var dataMix = new Array();	");
				out.println(" dataMix = "+serMix.getDatas()+"; 	");
				out.println(" jsonItemM.push({\"name\":\'"+serMix.getName()+"\',\"type\":\"line\",\"data\":dataMix}); 	");
			}
			out.println(" 	var mix"+k+"	= new Object();		");
			out.println(" 		mix"+k+".value = \""+mix.getValue()+"\";			");
			out.println(" 		mix"+k+".name = \""+mix.getName()+"\";			");
			out.println(" 		mix"+k+".labels = "+mix.getSeries().get(0).getLabels()+";			");
			out.println(" 		mix"+k+".serie = jsonItemM;");
			out.println("mixed.push(mix"+k+");");
			k++;
		}
		
		/*********************************************/
		
		//**************Rad***************************/
		
		
		out.println(" var rads = new Array();	");
		out.println(" var jsonItemR = JSON.parse('[]');	");
		
		k=0;
		for(DataDashBoard rad: rads){
			out.println("  jsonItemR = JSON.parse('[]');	");
			for(SerieDashBoard serRad: rad.getSeries()){
				out.println(" var datarad = new Array();	");
				out.println(" datarad = "+serRad.getDatas()+"; 	");
				out.println(" jsonItemR.push({\"name\":\'"+serRad.getName()+"\',\"data\":datarad}); 	");
				
			}
			out.println(" 	var rad"+k+"	= new Object();		");
			out.println(" 		rad"+k+".value = \""+rad.getValue()+"\";			");
			out.println(" 		rad"+k+".name = \""+rad.getName()+"\";			");
			out.println(" 		rad"+k+".labels = "+rad.getSeries().get(0).getLabels()+";			");
			out.println(" 		rad"+k+".serie = jsonItemR;");
			out.println("rads.push(rad"+k+");");
			k++;
		}

		
		/*********************************************/

		//sparkss************************************
		
		out.println(" var sparks = new Array();	");
		out.println(" var serieSpark = new Array();	");
		
		
		
		for(DataDashBoard spark: sparks){
//AGREGAR VALIDACION POR SI LA SERIE VIENE VACIA O EL SPARK??
			for(SerieDashBoard serieSpark: spark.getSeries()){
				out.println(" 	var serie"+k+"	= new Object();		");
				out.println(" 		serie"+k+".datas = "+serieSpark.getDatas()+";			");
				out.println(" 		serie"+k+".labels = "+serieSpark.getLabels()+";			");
				out.println(" 	serieSpark.push(serie"+k+");			");
				k++;
			}
			out.println(" 	var sparkn"+i+"	= new Object();		");
			out.println(" 		sparkn"+i+".value = \""+spark.getValue()+"\";			");
			out.println(" 		sparkn"+i+".name = \""+spark.getName()+"\";			");
			out.println(" 		sparkn"+i+".serie = serieSpark;");
			out.println("sparks.push(sparkn"+i+");");
			i++;

		}


		out.println("</script>					");
		//**********************************************/


		/******dona ************/
		out.println(" <script>					");

		out.println(" var donas = new Array();	");
		out.println(" var serieDona = new Array();	");
		k=0;
		for(DataDashBoard dona: donas){
			
//AGREGAR VALIDACION POR SI LA SERIE VIENE VACIA O LA DONA??

			for(SerieDashBoard serieDona: dona.getSeries()){
				out.println(" 	var serie"+k+"	= new Object();		");
				out.println(" 		serie"+k+".datas = "+serieDona.getDatas()+";			");
				out.println(" 		serie"+k+".name = \""+serieDona.getName()+"\";			");
				out.println(" 	serieDona.push(serie"+k+");			");
				k++;
			}
			out.println(" 																			");
			out.println(" 	var dona"+z+"	= new Object();											");
			out.println(" 		dona"+z+".value = \""+dona.getValue()+"\";							");
			out.println(" 		dona"+z+".name = \""+dona.getName()+"\";							");
			out.println(" 		dona"+z+".labels = "+dona.getSeries().get(0).getLabels()+";			");
			out.println(" 		dona"+z+".serie =	serieDona;										");
			out.println("donas.push(dona"+z+");														");
			z++;

		}
		out.println(" </script>										");
		/****************************/
		/******bar **********************************************/
		out.println(" <script>										");
		out.println(" var bars = new Array();						");
		out.println(" var jsonItem = JSON.parse('[]');				");
		
		k=0;
		for(DataDashBoard bar: bars){
			out.println("  jsonItem = JSON.parse('[]');												");
//AGREGAR VALIDACION POR SI LA SERIE VIENE VACIA O EL BAR??
			for(SerieDashBoard serBar: bar.getSeries()){
				out.println(" var databar = new Array();											");
				out.println(" databar = "+serBar.getDatas()+"; 										");
				out.println(" jsonItem.push({\"name\":\'"+serBar.getName()+"\',\"data\":databar}); 	");
			}
			out.println(" 	var bar"+k+"	= new Object();											");
			out.println(" 		bar"+k+".value = \""+bar.getValue()+"\";							");
			out.println(" 		bar"+k+".name = \""+bar.getName()+"\";								");
			out.println(" 		bar"+k+".labels = "+bar.getSeries().get(0).getLabels()+";			");
			out.println(" 		bar"+k+".serie = jsonItem;											");
			out.println("bars.push(bar"+k+");														");
			k++;
		}
		//out.println("  alert(bars[0].serie); 	");				

		out.println(" </script>					");

		/**************************************/
		
%>
<body>

	<div id="wrapper">

		<div class="content-area">
			<div class="container-fluid">
				<div class="main">

							<%	int a = 1;
								int b = 1;
								int c = 1;
								int d = 1;
								int e = 1;
								for(RowDashBoard row: rows){
									if(row.getType().equals(TypeRowDashBoard.Spark)){
										out.println("	<div class=\"row sparkboxes mt-4 mb-4\">		");
											}else if(row.getType().equals(TypeRowDashBoard.Common)){
											out.println("	<div class=\"row mt-5 mb-4\">				");
											}
								for(DataDashBoard dashboard: row.getGraphics()){
										if(dashboard.getType().equals(TypeDataDashBoard.Spark)){
												out.println("	<div class=\"col-md-auto\">				");
												out.println("	<div class=\"box box1\">				");
												out.println("	<div id=\"spark"+a+"\"></div>			");
												out.println("	</div>									");
												out.println("											");
												a++;
										}else if(dashboard.getType().equals(TypeDataDashBoard.Bar)){
												out.println("	<div class=\"col-md-6\">				");
												out.println("	<div class=\"box\">						");
												out.println("	<div id=\"bar"+b+"\"> </div>			");
												out.println("	</div>									");
												b++;
										}else if(dashboard.getType().equals(TypeDataDashBoard.Mixed)){
												out.println("	<div class=\"col-md-6\">				");
												out.println("	<div class=\"box\">						");
												out.println("	<div id=\"mixed"+c+"\"> </div>			");
												out.println("	</div>									");
												c++;
										}else if(dashboard.getType().equals(TypeDataDashBoard.Radar)){
												out.println("	<div class=\"col-md-6\">				");
												out.println("	<div class=\"box\">						");
												out.println("	<div id=\"rad"+d+"\"> </div>			");
												out.println("	</div>									");
												d++;
										}else {
												out.println("	<div class=\"col-md-6\">				");
												out.println("	<div class=\"box\">						");
												out.println("	<div id=\"donut"+e+"\"> </div>			");
												out.println("	</div>									");
												e++;
										}
									out.println("	</div>		");
							}
								out.println("	</div>		");

							}
		
							%>	
							
						</div>
					</div>
				</div>
			</div>

	<script src="DashBoard/js/jquery.slim.min.js"></script>
	<script src="DashBoard/js/apexcharts@latest.js"></script>
	<script src="DashBoard/js/scripts.js"></script>

	<script>

	</script>
</body>

</html>