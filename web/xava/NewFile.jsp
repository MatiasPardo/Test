<html>
<head>
    <meta charset="utf-8">
    <title>javascript - Obtener el valor de un input type=text de varias maneras</title>
    <script>
    
    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }
    
    function capturar()
    {
        // obtenemos e valor por el numero de elemento
        var s=document.forms["form1"].elements[0].value;
        // Obtenemos el valor por el id
        var porId=document.getElementById("nombre").value;
        // Obtenemos el valor por el Nombre
        var porNombre=document.getElementsByName("nombre")[0].value;
        // Obtenemos el valor por el tipo de tag
        var porTagName=document.getElementsByTagName("input")[0].value;
        // Obtenemos el valor por el nombre de la clase
        var porClassName=getParameterByName('nombre');
 
        document.getElementById("resultado").innerHTML=" \
            Por elementos: "+s+" \
            <br>Por ID: "+porId+" \
            <br>Por Nombre: "+porNombre+" \
            <br>Por TagName: "+porTagName+" \
            <br>Por ClassName: "+porClassName;
    }
    </script>
 
    <style>
        form   {width:250px;height:180px;border:1px solid #ccc;padding:10px;}
    </style>
</head>
 
<body>
		<%
		String nom=request.getParameter("nombre");
		%>
	<table border="1">
            <tr>
                <th colspan="2">Datos que se reciben:</th>
            </tr>
            <!-- imprimiendo parametros que fueron rescatados -->
            <tr> 
                <td>Nombre:</td><td><%=nom%></td>
            </tr>
        </table>
    <h1>Obtener el valor de un input type=text de varias maneras</h1>
    <form id="form1">
        Nombre:<br><input type="text" name="nombre" value="jose" id="nombre" class="formulario">
 
        <p><input type="checkbox" name="acepto" id="acepto" class="formulario_check"> Acepto el contrato</p>
 
        <p>Deacuerdo: Si<input type="radio" name="deacuerdo" value="si"> No<input type="radio" name="deacuerdo" value="no"></p>
 
        <p>
        <select name="seleccion" id="seleccion" class="formulario_select">
            <option value="1">primera</option>
            <option value="2">segunda</option>
        </select>
        </p>
    </form>
    <input type="button" value="obtener el nombre" onclick="capturar()">
    <div id="resultado"></div>
 
    <p><a href="http://www.lawebdelprogramador.com">http://www.lawebdelprogramador.com</a></p>
</body>
</html>