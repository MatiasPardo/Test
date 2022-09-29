<!DOCTYPE html>


<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">

  <title>POTENCIA NATURAL DE UN COMPLEJO</title>
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>

  <script src="calculadoraPote.js" type="text/javascript"> </script>

  <div class="container" id="navegador">                
    <ul class="pager" id="seccion1">
      <li class="previous"><a href="calculadora.jsp">Menú Principal</a></li>
    </ul>
  </div>
  
</head>

<body>

<div align="center">
  <h1>Potencia natural de un número complejo: z1^n</h1>
</div>

<nav>

<div class="container">
  <h2>Primer número: z1</h2>
  <p>Elegir modo de ingreso de z1. En forma binomial: (a;b) o bien a + b j. Por defecto el signo de los datos será positivo o en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo. Utilizar el '.' como separador decimal</p>
  <div class="container">
    <form>
      <label class="checkbox-inline">
        <input align="left" id="z1" type="radio" name="modoz1" value="1" onclick="handleClick(this);">Polar
      </label>
      <label class="checkbox-inline">
        <input align="left" id="z1"  type="radio" name="modoz1" value="2" checked onclick="handleClick(this);">Binomial
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


<!--EXPONENTE -->
<div class="container">
  <h2>Exponente: </h2>
  <p>Ingresar el exponente. El mismo debe ser un número natural. Se truncará la parte decimal de ingresar un entero positivo.</p>
  
  <br>

  <form class="form-horizontal">
    <div class="form-group">
      <label class="control-label col-sm-2" for="email">Exponente natural:</label>
      <div class="col-sm-10">
        <!--<input type="email" class="form-control" id="email" placeholder="Enter email" name="email"> -->
        <input id="e" type="text" size="10" class="form-control">
      </div>
    </div>
    
  </form>
</div>

<br>


<div class="container" align="center">
  <button id="potencia" type="button" class="btn btn-primary btn-lg" style='width:160px; height:50px'>Potencia</button>
  <button id="borrar" type="button" class="btn btn-danger btn-lg" style='width:160px; height:50px'>Borrar campos</button>
</div>

<div align="center" id="tbox2">
  <div class="box-style box-style02">
    <div class="content">
      <span id="mostrar"><h1></h1></span>
    </div>
  </div>
</div>  
    
<div align="center" id="tbox2">
  <div class="box-style box-style02">
    <div class="content">
      <span id="mostrar2"><h1></h1></span>
    </div>
  </div>
</div>  
    
<div align="center" id="tbox2">
  <div class="box-style box-style02">
    <div class="content">
      <span id="mostrar3"><h1></h1></span>
    </div>
  </div>
</div>

<div class="container">                
  <ul class="pager">
    <li class="previous"><a href="calculadora.jsp">Menú Principal</a></li>
    <!--<li class="next"><a href="#">Next</a></li> -->
  </ul>
</div>

<!-- VISTA VIEJA -->
<!--
<div align="center">
<h1>POTENCIA NATURAL DE UN COMPLEJO: z1 ^ N.</h1>
</div>

<nav>

  



     


<div class="box-style box-style01">
<div  class="content">
<table width="100%" height="100%">                  
  	
<tbody>
<tr>
<td height="150" colspan="4" align="left">
<h1>Elegir modo de ingreso de z1. En forma binomial: (a;b) o bien a + b j. Por defecto el signo de los datos será positivo o en forma polar:[ R,Arg ]. El argumento debe ingresarse en radianes. Por defecto el signo del mismo será positivo. Utilizar el '.' como separador decimal</h1> <br>
<input width="55%" height="100" align="left" id="z1" type="radio" name="modoz1" value="1" onclick="handleClick(this);">Polar <br>
<input width="55%" height="100" align="left" id="z1"  type="radio" name="modoz1" value="2" checked onclick="handleClick(this);">Binomial
</td>
</tr>

 

<tr>


<td width="25%" height="50" align="center">
<h1 align="center">Parte Real: <br> <input id="a" type="text" size="10"> </h1>
</td>



<td width="25%" align="center"><h1 align="center">Parte Imaginaria(sin j): <br>
   <input id="b" type="text" size="10"></h1>
  </td> 
  </tr>
  
  


  </tbody>
  </table>	
  
  
  <table width="100%" height="57%">                  
  	
<tbody>
 
<td width="25%" height="50" align="center">
<h1 align="center">Módulo: <br> <input id="r1" type="text" size="10"> </h1>
</td>

<td width="25%" height="50" align="center">
<h1 align="center">Argumento(Rad): <br> <input id="arg1" type="text" size="10"> </h1>
</td>
 </tbody>
 </table>	
  
<table width="100%" height="57%">                  
<tbody>  

<tr>
<td height="150" colspan="4" align="left">
<h1>
Ingresar el exponente. El mismo debe ser un número natural. Se truncará la parte decimal de ingresar un entero positivo.<br></h1>

</td>
</tr>


<tr>
<td width="25%" align="center"><h1 align="center">Exponente:<br> <input id="e" type="text" size="10"></h1>
</td>
</tr>

                    	
                  

				
			  </div>
			</div>
		







<div id="tbox3">
 
<div class="box-style box-style03">

<div class="content"><br>
          
<table width="100%" height="27%">
<tbody><tr><td width="11.11%" height="153" align="center">
<button id="potencia">Potencia</button></td>
               
					
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
		
		
		  <div align="center" id="tbox2">
			<div class="box-style box-style02">
				<div class="content">
				<span id="mostrar2"><h1></h1></span>
				</div>
			</div>
		</div>	
		
		
		  <div align="center" id="tbox2">
			<div class="box-style box-style02">
				<div class="content">
				<span id="mostrar3"><h1></h1></span>
				</div>
			</div>
		</div>	
		 
	
<ul id="myList">
</ul>

-->



</nav>
</body>
</html>