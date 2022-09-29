<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">

  <title>SUMA DE COMPLEJOS</title>

  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
  <script src="file:///C:/Users/u187925/Documents/calculadoraSuma.js" type="text/javascript"> </script>
<!--
<style>
  
  input[type="button"]:hover{
        background-color:slategray;
        cursor: pointer;
    }
    input[type="button"].arit{
        background-color:maroon;
    }
    input[type="button"].igual{
        background-color: green;
    }
    input[type="button"].clear{
        background-color:orangered;
    }
    form{
        background-color:white;
    }
    input[type="text"]{
        background-color:black;
        border:0px;
        width:250px;
        height: 40px;
        font-size: 20px;
        color: white;
        text-align: left;
    }
    input[name="a"]{
        height: 20px;
        font-size: 16px;
    }
    input[name="b"]{
        margin-bottom: 5px;
        font-size: 26px;
    }
    input[name="c"]{
        height: 20px;
        font-size: 16px;
    }
    input[name="d"]{
        margin-bottom: 5px;
        font-size: 26px;
    }
   
    body{
        background-color:#AAAA;
    }
	
    
</style>
<style type="text/css">
* { padding: 0; margin: auto; text-align: center  }
#cabecera { background-color: #fff0f0; }  
h1 { font: bold 1.5em arial; padding: 0.5em; }
#navegador { background-color: #663366; padding: 0.5em; }
#navegador li { margin: 0px 5px; padding: 0.1em 1em 0.5em 1em; 
           background-color: #9933cc; width: 12%;float: left;
           list-style-type: none; position: relative; }
#subseccion1, #subseccion2, #subseccion3, #subseccion4 
             { position: absolute; top: 100%; left: 0px;  
             background-color: #41338b; font:normal 0.8em arial;  
             padding: 0.2em 0.5em ; display: none; }				 
#navegador li:hover { background-color: #990033; }
#navegador li a:link, #navegador li a:visited { 
           color: #feffe4; text-decoration: none; }
#navegador li a:hover, #navegador li a:active { 
           color:#ffd7a9 ; text-decoration: none; }
.borrar { clear: both; }	

#principal h2 { font: bold 1.5em arial; padding: 0.5em }
#principal p { font: normal 0.9em arial; text-align: justify;
           text-indent: 3em; padding: 0.5em 2em; }
</style>
-->

<!--
<div id="navegador">
  <ul>
    <li id="seccion1" >
      <a href="calculadora.html">VOLVER</a> 
    </li>  		  
  </ul>
    <div class="borrar"></div>
</div> -->

<div id="navegador" class="container">                
  <ul class="pager">
    <li id="seccion1" class="previous"><a href="calculadora.html">Previous</a></li>
  </ul>
</div>

</head>


<body background="pizverde.png">
<div align="center">
  <h1>Sumar dos números complejos z1 + z2 </h1>
</div>

<nav>

<div class="container">
  <h2>Primer número</h2>
  <p>Elegir modo de ingreso de z1. En forma binomial: a + b j. Por defecto el signo de los datos será positivo o en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo.</p>
  <div class="container">
    <form>
      <label class="checkbox-inline">
        <input name="modo" type="checkbox" value="">Polar
      </label>
      <label class="checkbox-inline">
        <input name="modo" type="checkbox" value="">Binomial
      </label>
    </form>
  </div>
  <br>


  <form class="form-horizontal" >
    <div class="form-group">
      <label class="control-label col-sm-2" for="email">Parte real:</label>
      <div class="col-sm-10">
        <!--<input type="email" class="form-control" id="email" placeholder="Enter email" name="email"> -->
        <input id="a" type="text" size="10" class="form-control" placeholder="Ej: 4">
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Parte imaginaria (sin j) :</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="b" type="text" size="10" class="form-control" placeholder="Ej: 6">
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Módulo:</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="r1" type="text" size="10" class="form-control" >
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Argumento (radianes):</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="arg1" type="text" size="10" class="form-control" placeholder="Ej:" >
      </div>
    </div>
    
    
  </form>
</div>


<!--segundo numero -->
<div class="container">
  <h2>Segundo número</h2>
  <p>Elegir el modo de ingreso de z2. En forma binomial: a + b j. Por defecto el signo de los datos será positivo o bien en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo.</p>
  <div class="container">
    <form>
      <label class="checkbox-inline">
        <input name="modo" type="checkbox" value="">Polar
      </label>
      <label class="checkbox-inline">
        <input name="modo" type="checkbox" value="">Binomial
      </label>
    </form>
  </div>
  <br>


  <form class="form-horizontal">
    <div class="form-group">
      <label class="control-label col-sm-2" for="email">Parte real:</label>
      <div class="col-sm-10">
        <!--<input type="email" class="form-control" id="email" placeholder="Enter email" name="email"> -->
        <input id="c" type="text" size="10" class="form-control">
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Parte imaginaria (sin j) :</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="d" type="text" size="10" class="form-control" placeholder="">
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Módulo:</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="r2" type="text" size="10" class="form-control">
      </div>
    </div>
    <div class="form-group">
      <label class="control-label col-sm-2" for="pwd">Argumento (radianes):</label>
      <div class="col-sm-10">          
        <!--<input type="password" class="form-control" id="pwd" placeholder="Enter password" name="pwd">-->
        <input id="arg2" type="text" size="10" class="form-control">
      </div>
    </div>
    
    
  </form>
</div>

<br>


<div class="container" align="center">
  <button id="sumar" type="button" class="btn btn-primary btn-lg" style='width:160px; height:50px'>Sumar</button>
  <button id="borrar" type="button" class="btn btn-danger btn-lg" style='width:160px; height:50px'>Borrar campos</button>
</div>

<div class="container">                
  <ul class="pager">
    <li class="previous"><a href="calculadora.html">Previous</a></li>
    <!--<li class="next"><a href="#">Next</a></li> -->
  </ul>
</div>


<!-- VISTA VIEJA -->
<!--
<div class="box-style box-style01">
  <div  class="content">
    <table width="100%" height="100%">                    	
      <tbody>
        <tr>
          <td height="150" colspan="4" align="left">
            <h1>Elegir modo de ingreso de z1. En forma binomial: a + b j. Por defecto el signo de los datos será positivo o en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo</h1> <br>
            <input  type="radio" name="modo" value="polar">Polar <br>
            <input  type="radio" name="modo" value="binomial"> Binomial
          </td>
        </tr>
        <tr>
          <td width="25%" height="50" align="center">
            <h1 align="center">Parte Real: <br> <input id="a" type="text" size="10"> </h1>
          </td>
          <td width="25%" align="center">
            <h1 align="center">Parte Imaginaria(sin j): <br>
            <input id="b" type="text" size="10"></h1>
          </td> 
        </tr>
      </tbody>
    </table>

    <table width="100%" height="57%">                   	
      <tbody> 
        <tr>
          <td width="25%" height="50" align="center">
            <h1 align="center">Módulo: <br> <input id="r1" type="text" size="10"> </h1>
          </td>
          <td width="25%" height="50" align="center">
            <h1 align="center">Argumento(Rad): <br> <input id="arg1" type="text" size="10"> </h1>
          </td>
        </tr>
      </tbody>
    </table>	
    <table width="100%" height="57%">                  
      <tbody>  
        <tr>
          <td height="150" colspan="4" align="left">
            <h1>Elegir el modo de ingreso de z2. En forma binomial: a + b j. Por defecto el signo de los datos será positivo o bien en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo.
            </h1>
          </td>
        </tr>
        <tr>
          <td width="25%" align="center">
            <h1 align="center">Parte Real: <br> <input id="c" type="text" size="10">
            </h1>
          </td>
          <td width="25%" align="center">
            <h1 align="center">Parte Imaginaria(sin j): <br>
              <input id="d" type="text" size="10">
            </h1>
          </td>
        </tr>                  	
        <tr>
          <td width="25%" align="center">
            <h1 align="center">Módulo: <br> <input id="r2" type="text" size="10"></h1>
          </td>
          <td width="25%" align="center">
            <h1 align="center">Argumento(Rad): <br>
              <input id="arg2" type="text" size="10">
            </h1>
          </td>
        </tr>
      </tbody>
    </table>
	</div>
</div>
		


<div id="tbox3">
  <div class="box-style box-style03">
    <div class="content"><br>
      <table width="100%" height="27%">
        <tbody>
          <tr>
            <td width="11.11%" height="153" align="center">
              <button id="sumar">Suma</button>
            </td>
          </tr>                  
        </tbody>
      </table>
    </div>		

    <table width="100%" height="27%">
      <tbody width="31.11%" height="300" align="center">
        <button id="borrar">Borrar Campos</button>
      </tbody>
    </table>
		
    <div align="center" id="tbox2">
			<div class="box-style box-style02">
				<div class="content">
				  <span id="mostrar"><h1></h1></span>
				</div>
			</div>
		</div>
  </div>
</div>	
		
		 -->
<!--
<ul id="myList">
</ul> -->





</nav>
</body>
</html>