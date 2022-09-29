// == ClosureCompiler ==
// @compilation_level ADVANCED_OPTIMIZATIONS
// @externs_url http://closure-compiler.googlecode.com/svn/trunk/contrib/externs/maps/google_maps_api_v3_3.js
// == / ClosureCompiler ==

/ **
 * @name MarkerClusterer para Google Maps v3
 * @version version 1.0.3
 * @autor Luke Mahe
 * @fileoverview
 * La biblioteca crea y administra cl�steres por nivel de zoom para grandes cantidades de
 * Marcadores.
 * /

/ **
 * @license
 * Licenciado bajo la Licencia Apache, Versi�n 2.0 (la "Licencia");
 * No puede utilizar este archivo, excepto en cumplimiento con la Licencia.
 * Puede obtener una copia de la licencia en
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * A menos que sea requerido por la ley aplicable o acordado por escrito, software
 * distribuido bajo la Licencia se distribuye "TAL CUAL",
 * SIN GARANT�AS O CONDICIONES DE NING�N TIPO, ya sea expresa o impl�cita.
 * Consulte la Licencia para el idioma espec�fico que rige los permisos y
 * Limitaciones bajo la Licencia.
 * /


/ **
 * Un agrupador de marcadores que agrupa los marcadores.
 *
 * @param {google.maps.Map} map El mapa de Google para adjuntar.
 * @param {Array. <google.maps.Marker> =} opt_markers Marcadores opcionales para agregar a
 * El cluster.
 * @param {Object =} opt_options admite las siguientes opciones:
 * 'gridSize': (n�mero) El tama�o de cuadr�cula de un cl�ster en p�xeles.
 * 'maxZoom': (n�mero) El nivel m�ximo de zoom que un marcador puede ser parte de un
 * cluster.
 * 'zoomOnClick': (booleano) Si el comportamiento predeterminado de hacer clic en un
 * cl�ster es hacer zoom en �l.
 * 'imagePath': (cadena) La URL base donde las im�genes representan
 * Se encontrar�n agrupaciones. La URL completa ser�:
 * {imagePath} [1-5]. {imageExtension}
 * Predeterminado: '../images/m'.
 * 'imageExtension': (cadena) El sufijo para im�genes representadas por la URL
 * Se encontrar�n agrupaciones. Ver _imagePath_ para m�s detalles.
 * Predeterminado: 'png'.
 * 'averageCenter': (boolean) Si el centro de cada grupo debe ser
 * El promedio de todos los marcadores en el cluster.
 * 'minimumClusterSize': (n�mero) El n�mero m�nimo de marcadores para estar en un
 * Cl�ster antes de que los marcadores est�n ocultos y una cuenta.
 * se muestra.
 * 'styles': (objeto) Un objeto que tiene propiedades de estilo:
 * 'url': (cadena) La url de la imagen.
 * 'altura': (n�mero) La altura de la imagen.
 * 'ancho': (n�mero) El ancho de la imagen.
 * 'ancla': (Array) La posici�n de ancla del texto de la etiqueta.
 * 'textColor': (cadena) El color del texto.
 * 'textSize': (n�mero) El tama�o del texto.
 * 'backgroundPosition': (cadena) La posici�n del fondo x, y.
 * @constructor
 * @extends google.maps.OverlayView
 * /
funci�n MarkerClusterer (map, opt_markers, opt_options) {
  // MarkerClusterer implementa la interfaz google.maps.OverlayView. Usamos el
  // extender la funci�n para extender MarkerClusterer con google.maps.OverlayView
  // porque puede que no siempre est� disponible cuando se define el c�digo, por lo que
  // B�scalo en el �ltimo momento posible. Si no existe ahora entonces
  // no tiene sentido seguir adelante :)
  this.extend (MarkerClusterer, google.maps.OverlayView);
  this.map_ = map;

  / **
   * @type {Array. <google.maps.Marker>}
   * @private
   * /
  this.markers_ = [];

  / **
   * @type {Array. <Cluster>}
   * /
  this.clusters_ = [];

  este tama�o = [53, 56, 66, 78, 90];

  / **
   * @private
   * /
  this.styles_ = [];

  / **
   * @type {boolean}
   * @private
   * /
  this.ready_ = false;

  opciones var = opt_options || {};

  / **
   * @teclea un n�mero}
   * @private
   * /
  this.gridSize_ = opciones ['gridSize'] || 60;

  / **
   * @private
   * /
  this.minClusterSize_ = options ['minimumClusterSize'] || 2;


  / **
   * @teclea un n�mero}
   * @private
   * /
  this.maxZoom_ = opciones ['maxZoom'] || nulo;

  this.styles_ = opciones ['estilos'] || [];

  / **
   * @type {cadena}
   * @private
   * /
  this.imagePath_ = opciones ['imagePath'] ||
      this.MARKER_CLUSTER_IMAGE_PATH_;

  / **
   * @type {cadena}
   * @private
   * /
  this.imageExtension_ = opciones ['imageExtension'] ||
      this.MARKER_CLUSTER_IMAGE_EXTENSION_;

  / **
   * @type {boolean}
   * @private
   * /
  this.zoomOnClick_ = true;

  if (options ['zoomOnClick']! = undefined) {
    this.zoomOnClick_ = options ['zoomOnClick'];
  }

  / **
   * @type {boolean}
   * @private
   * /
  this.averageCenter_ = false;

  if (options ['averageCenter']! = undefined) {
    this.averageCenter_ = options ['averageCenter'];
  }

  this.setupStyles_ ();

  this.setMap (mapa);

  / **
   * @teclea un n�mero}
   * @private
   * /
  this.prevZoom_ = this.map_.getZoom ();

  // A�adir los oyentes del evento map
  var que = esto;
  google.maps.event.addListener (this.map_, 'zoom_changed', function () {
    // Determina el tipo de mapa y evita niveles de zoom ilegales
    var zoom = that.map_.getZoom ();
    var minZoom = that.map_.minZoom || 0;
    var maxZoom = Math.min (that.map_.maxZoom || 100,
                         that.map_.mapTypes [that.map_.getMapTypeId ()]. ??maxZoom);
    zoom = Math.min (Math.max (zoom, minZoom), maxZoom);

    if (that.prevZoom_! = zoom) {
      that.prevZoom_ = zoom;
      that.resetViewport ();
    }
  });

  google.maps.event.addListener (this.map_, 'idle', function () {
    that.redraw ();
  });

  // Finalmente, agrega los marcadores
  if (opt_markers && (opt_markers.length || Object.keys (opt_markers) .length)) {
    this.addMarkers (opt_markers, false);
  }
}


/ **
 * La ruta de la imagen del cl�ster de marcadores.
 *
 * @type {cadena}
 * @private
 * /
MarkerClusterer.prototype.MARKER_CLUSTER_IMAGE_PATH_ = '../images/m';


/ **
 * La ruta de la imagen del cl�ster de marcadores.
 *
 * @type {cadena}
 * @private
 * /
MarkerClusterer.prototype.MARKER_CLUSTER_IMAGE_EXTENSION_ = 'png';


/ **
 * Extiende un prototipo de objetos por otros.
 *
 * @param {Objeto} obj1 El objeto a extender.
 * @param {Object} obj2 El objeto a extender con.
 * @return {Objeto} El nuevo objeto extendido.
 * @ignorar
 * /
MarkerClusterer.prototype.extend = function (obj1, obj2) {
  retorno (funci�n (objeto) {
    para (propiedad var en object.prototype) {
      this.prototype [propiedad] = object.prototype [propiedad];
    }
    devuelve esto
  }). apply (obj1, [obj2]);
};


/ **
 * Implementaci�n del m�todo de interfaz.
 * @ignorar
 * /
MarkerClusterer.prototype.onAdd = function () {
  this.setReady_ (true);
};

/ **
 * Implementaci�n del m�todo de interfaz.
 * @ignorar
 * /
MarkerClusterer.prototype.draw = function () {};

/ **
 * Configura el objeto de estilos.
 *
 * @private
 * /
MarkerClusterer.prototype.setupStyles_ = function () {
  if (this.styles_.length) {
    regreso;
  }

  para (var i = 0, tama�o; tama�o = este tama�o [i]; i ++) {
    este.estilos_.push ({
      url: this.imagePath_ + (i + 1) + '.' + this.imageExtension_,
      altura: tama�o
      ancho: tama�o
    });
  }
};

/ **
 * Ajustar el mapa a los l�mites de los marcadores en el agrupador.
 * /
MarkerClusterer.prototype.fitMapToMarkers = function () {
  marcadores var = this.getMarkers ();
  l�mites de var = new google.maps.LatLngBounds ();
  para (var i = 0, marcador; marcador = marcadores [i]; i ++) {
    lines.extend (marker.getPosition ());
  }

  this.map_.fitBounds (l�mites);
};


/ **
 * Establece los estilos.
 *
 * @param {Objeto} estilos El estilo a establecer.
 * /
MarkerClusterer.prototype.setStyles = function (styles) {
  this.styles_ = styles;
};


/ **
 * Obtiene los estilos.
 *
 * @return {Objeto} El objeto de estilos.
 * /
MarkerClusterer.prototype.getStyles = function () {
  devuelve this.styles_;
};


/ **
 * Si el zoom al hacer clic est� configurado.
 *
 * @return {boolean} Verdadero si se establece zoomOnClick_.
 * /
MarkerClusterer.prototype.isZoomOnClick = function () {
  devuelve this.zoomOnClick_;
};

/ **
 * Si el centro medio est� establecido.
 *
 * @return {boolean} Verdadero si se establece averageCenter_.
 * /
MarkerClusterer.prototype.isAverageCenter = function () {
  devuelve this.averageCenter_;
};


/ **
 * Devuelve la matriz de marcadores en el clusterer.
 *
 * @return {Array. <google.maps.Marker>} Los marcadores.
 * /
MarkerClusterer.prototype.getMarkers = function () {
  devuelve this.markers_;
};


/ **
 * Devuelve el n�mero de marcadores en el clusterer.
 *
 * @return {Number} El n�mero de marcadores.
 * /
MarkerClusterer.prototype.getTotalMarkers = function () {
  devuelve this.markers_.length;
};


/ **
 * Establece el zoom m�ximo para el clusterer.
 *
 * @param {number} maxZoom El nivel de zoom m�ximo.
 * /
MarkerClusterer.prototype.setMaxZoom = function (maxZoom) {
  this.maxZoom_ = maxZoom;
};


/ **
 * Obtiene el zoom m�ximo para el clusterer.
 *
 * @return {n�mero} El nivel de zoom m�ximo.
 * /
MarkerClusterer.prototype.getMaxZoom = function () {
  devuelve this.maxZoom_;
};


/ **
 * La funci�n para calcular la imagen del icono del cl�ster.
 *
 * @param {Array. <google.maps.Marker>} markers Los marcadores en el clusterer.
 * @param {number} numStyles El n�mero de estilos disponibles.
 * @return {Objeto} Propiedades de un objeto: 'texto' (cadena) e '�ndice' (n�mero).
 * @private
 * /
MarkerClusterer.prototype.calculator_ = function (markers, numStyles) {
  �ndice var = 0;
  var count = markers.length;
  var dv = cuenta;
  while (dv! == 0) {
    dv = parseInt (dv / 10, 10);
    �ndice ++;
  }

  index = Math.min (index, numStyles);
  regreso {
    texto: contar
    �ndice: �ndice
  };
};


/ **
 * Establecer la funci�n de calculadora.
 *
 * @param {function (Array, number)} calculator La funci�n para establecer como
 * calculadora. La funci�n debe devolver las propiedades de un objeto:
 * 'texto' (cadena) e '�ndice' (n�mero).
 *
 * /
MarkerClusterer.prototype.setCalculator = function (calculator) {
  this.calculator_ = calculator;
};


/ **
 * Obtener la funci�n de calculadora.
 *
 * @return {function (Array, number)} la funci�n de la calculadora.
 * /
MarkerClusterer.prototype.getCalculator = function () {
  devuelve this.calculator_;
};


/ **
 * Agregar una matriz de marcadores al clusterer.
 *
 * @param {Array. <google.maps.Marker>} marcadores Los marcadores para agregar.
 * @param {boolean =} opt_nodraw Si se deben volver a dibujar los grupos.
 * /
MarkerClusterer.prototype.addMarkers = function (markers, opt_nodraw) {
  if (markers.length) {
    para (var i = 0, marcador; marcador = marcadores [i]; i ++) {
      this.pushMarkerTo_ (marcador);
    }
  } else if (Object.keys (markers) .length) {
    para (marcador var en marcadores) {
      this.pushMarkerTo_ (marcadores [marcador]);
    }
  }
  si (! opt_nodraw) {
    this.redraw ();
  }
};


/ **
 * Empuja un marcador al agrupador.
 *
 * @param {google.maps.Marker} marcador El marcador a agregar.
 * @private
 * /
MarkerClusterer.prototype.pushMarkerTo_ = function (marker) {
  marker.isAdded = false;
  if (marcador ['draggable']) {
    // Si el marcador es arrastrable, agregue un escucha para que actualicemos los clusters en
    // el final del arrastre.
    var que = esto;
    google.maps.event.addListener (marcador, 'dragend', function () {
      marker.isAdded = false;
      that.repaint ();
    });
  }
  this.markers_.push (marcador);
};


/ **
 * Agrega un marcador al agrupador y vuelve a dibujar si es necesario.
 *
 * @param {google.maps.Marker} marcador El marcador a agregar.
 * @param {boolean =} opt_nodraw Si se deben volver a dibujar los grupos.
 * /
MarkerClusterer.prototype.addMarker = function (marker, opt_nodraw) {
  this.pushMarkerTo_ (marcador);
  si (! opt_nodraw) {
    this.redraw ();
  }
};


/ **
 * Elimina un marcador y devuelve verdadero si se elimina, falso si no
 *
 * @param {google.maps.Marker} marker El marcador a eliminar
 * @return {boolean} Si el marcador fue eliminado o no
 * @private
 * /
MarkerClusterer.prototype.removeMarker_ = function (marker) {
  �ndice var = -1;
  if (this.markers_.indexOf) {
    index = this.markers_.indexOf (marcador);
  } else {
    para (var i = 0, m; m = this.markers_ [i]; i ++) {
      si (m == marcador) {
        �ndice = i;
        descanso;
      }
    }
  }

  si (�ndice == -1) {
    // El marcador no est� en nuestra lista de marcadores.
    falso retorno;
  }

  marker.setMap (null);

  this.markers_.splice (�ndice, 1);

  devuelve verdadero
};


/ **
 * Eliminar un marcador del cluster.
 *
 * @param {google.maps.Marker} marker El marcador a eliminar.
 * @param {boolean =} opt_nodraw Opcional booleano para forzar que no se vuelva a dibujar.
 * @return {boolean} Verdadero si se quit� el marcador.
 * /
MarkerClusterer.prototype.removeMarker = function (marker, opt_nodraw) {
  var se elimin� = this.removeMarker_ (marcador);

  si (! opt_nodraw && eliminado) {
    this.resetViewport ();
    this.redraw ();
    devuelve verdadero
  } else {
   falso retorno;
  }
};


/ **
 * Elimina una matriz de marcadores del cl�ster.
 *
 * @param {Array. <google.maps.Marker>} marcadores Los marcadores que se eliminar�n.
 * @param {boolean =} opt_nodraw Opcional booleano para forzar que no se vuelva a dibujar.
 * /
MarkerClusterer.prototype.removeMarkers = function (markers, opt_nodraw) {
  // crear una copia local de marcadores si es necesario
  // (removeMarker_ modifica la matriz getMarkers () en su lugar)
  var markersCopy = markers === this.getMarkers ()? markers.slice (): marcadores;
  var eliminado = falso;

  para (var i = 0, marcador; marker = markersCopy [i]; i ++) {
    var r = this.removeMarker_ (marcador);
    eliminado = eliminado || r;
  }

  si (! opt_nodraw && eliminado) {
    this.resetViewport ();
    this.redraw ();
    devuelve verdadero
  }
};


/ **
 * Establece el estado listo del cluster.
 *
 * @param {boolean} ready El estado.
 * @private
 * /
MarkerClusterer.prototype.setReady_ = function (ready) {
  if (! this.ready_) {
    this.ready_ = ready;
    this.createClusters_ ();
  }
};


/ **
 * Devuelve el n�mero de clusters en el clusterer.
 *
 * @return {n�mero} El n�mero de agrupaciones.
 * /
MarkerClusterer.prototype.getTotalClusters = function () {
  devuelve this.clusters_.length;
};


/ **
 * Devuelve el mapa de Google con el que est� asociado el agrupador.
 *
 * @return {google.maps.Map} El mapa.
 * /
MarkerClusterer.prototype.getMap = function () {
  devuelve this.map_;
};


/ **
 * Establece el mapa de Google con el que est� asociado el clusterer.
 *
 * @param {google.maps.Map} map El mapa.
 * /
MarkerClusterer.prototype.setMap = function (map) {
  this.map_ = map;
};


/ **
 * Devuelve el tama�o de la cuadr�cula.
 *
 * @return {n�mero} El tama�o de la cuadr�cula.
 * /
MarkerClusterer.prototype.getGridSize = function () {
  devuelve this.gridSize_;
};


/ **
 * Establece el tama�o de la cuadr�cula.
 *
 * @param {number} size El tama�o de la cuadr�cula.
 * /
MarkerClusterer.prototype.setGridSize = function (size) {
  this.gridSize_ = tama�o;
};


/ **
 * Devuelve el tama�o m�nimo del cluster.
 *
 * @return {n�mero} El tama�o de la cuadr�cula.
 * /
MarkerClusterer.prototype.getMinClusterSize = function () {
  devuelve this.minClusterSize_;
};

/ **
 * Establece el tama�o m�nimo del cluster.
 *
 * @param {number} size El tama�o de la cuadr�cula.
 * /
MarkerClusterer.prototype.setMinClusterSize = function (size) {
  this.minClusterSize_ = size;
};


/ **
 * Extiende un objeto de l�mites por el tama�o de la cuadr�cula.
 *
 * @param {google.maps.LatLngBounds} l�mites Los l�mites a extender.
 * @return {google.maps.LatLngBounds} Los l�mites extendidos.
 * /
MarkerClusterer.prototype.getExtendedBounds = funci�n (l�mites) {
  var projection = this.getProjection ();

  // Convertir los l�mites en latlng.
  var tr = new google.maps.LatLng (lines.getNorthEast (). lat (),
      lines.getNorthEast (). lng ());
  var bl = new google.maps.LatLng (lines.getSouthWest (). lat (),
      fronteras.getSouthWest (). lng ());

  // Convertir los puntos en p�xeles y extenderlos por el tama�o de la cuadr�cula.
  var trPix = projection.fromLatLngToDivPixel (tr);
  trPix.x + = this.gridSize_;
  trPix.y - = this.gridSize_;

  var blPix = projection.fromLatLngToDivPixel (bl);
  blPix.x - = this.gridSize_;
  blPix.y + = this.gridSize_;

  // Convertir los puntos de p�xel de nuevo a LatLng
  var ne = projection.fromDivPixelToLatLng (trPix);
  var sw = projection.fromDivPixelToLatLng (blPix);

  // Extiende los l�mites para contener los nuevos l�mites.
  l�mites. extensi�n (ne);
  l�mites. extensi�n (sw);

  l�mites de retorno;
};


/ **
 * Determina si un marcador est� contenido en unos l�mites.
 *
 * @param {google.maps.Marker} marker El marcador a verificar.
 * @param {google.maps.LatLngBounds} l�mites Los l�mites con los que se debe verificar.
 * @return {boolean} Verdadero si el marcador est� dentro de los l�mites.
 * @private
 * /
MarkerClusterer.prototype.isMarkerInBounds_ = function (marcador, l�mites) {
  devuelve los l�mites.contiene (marker.getPosition ());
};


/ **
 * Borra todos los clusters y marcadores del clusterer.
 * /
MarkerClusterer.prototype.clearMarkers = function () {
  this.resetViewport (true);

  // Establecer los marcadores en una matriz vac�a.
  this.markers_ = [];
};


/ **
 * Borra todos los clusters existentes y los recrea.
 * @param {boolean} opt_hide Para ocultar tambi�n el marcador.
 * /
MarkerClusterer.prototype.resetViewport = function (opt_hide) {
  // Eliminar todos los grupos
  para (var i = 0, cluster; cluster = this.clusters_ [i]; i ++) {
    cluster.remove ();
  }

  // Restablecer los marcadores para que no se agreguen y sean invisibles.
  para (var i = 0, marcador; marcador = this.markers_ [i]; i ++) {
    marker.isAdded = false;
    if (opt_hide) {
      marker.setMap (null);
    }
  }

  this.clusters_ = [];
};

/ **
 *
 * /
MarkerClusterer.prototype.repaint = function () {
  var oldClusters = this.clusters_.slice ();
  this.clusters_.length = 0;
  this.resetViewport ();
  this.redraw ();

  // Eliminar los clusters antiguos.
  // H�galo en un tiempo muerto para que los otros grupos se hayan dibujado primero.
  window.setTimeout (function () {
    para (var i = 0, cluster; cluster = oldClusters [i]; i ++) {
      cluster.remove ();
    }
  }, 0);
};


/ **
 * Redibuja los grupos.
 * /
MarkerClusterer.prototype.redraw = function () {
  this.createClusters_ ();
};


/ **
 * Calcula la distancia entre dos ubicaciones de latencia en km.
 * @ver http://www.movable-type.co.uk/scripts/latlong.html
 *
 * @param {google.maps.LatLng} p1 El primer punto de latencia.
 * @param {google.maps.LatLng} p2 El segundo punto de latencia.
 * @return {n�mero} La distancia entre los dos puntos en km.
 * @private
* /
MarkerClusterer.prototype.distanceBetweenPoints_ = function (p1, p2) {
  si (! p1 ||! p2) {
    devuelve 0;
  }

  var R = 6371; // Radio de la Tierra en km
  var dLat = (p2.lat () - p1.lat ()) * Math.PI / 180;
  var dLon = (p2.lng () - p1.lng ()) * Math.PI / 180;
  var a = Math.sin (dLat / 2) * Math.sin (dLat / 2) +
    Math.cos (p1.lat () * Math.PI / 180) * Math.cos (p2.lat () * Math.PI / 180) *
    Math.sin (dLon / 2) * Math.sin (dLon / 2);
  var c = 2 * Math.atan2 (Math.sqrt (a), Math.sqrt (1 - a));
  var d = R * c;
  volver d;
};


/ **
 * Agregar un marcador a un cl�ster, o crea un nuevo cl�ster.
 *
 * @param {google.maps.Marker} marcador El marcador a agregar.
 * @private
 * /
MarkerClusterer.prototype.addToClosestCluster_ = function (marker) {
  distancia var = 40000; // Un n�mero grande
  var clusterToAddTo = null;
  var pos = marker.getPosition ();
  para (var i = 0, cluster; cluster = this.clusters_ [i]; i ++) {
    var center = cluster.getCenter ();
    si (centro) {
      var d = this.distanceBetweenPoints_ (center, marker.getPosition ());
      si (d <distancia) {
        distancia = d;
        clusterToAddTo = cluster;
      }
    }
  }

  if (clusterToAddTo && clusterToAddTo.isMarkerInClusterBounds (marcador)) {
    clusterToAddTo.addMarker (marcador);
  } else {
    var cluster = nuevo Cluster (este);
    cluster.addMarker (marcador);
    this.clusters_.push (cluster);
  }
};


/ **
 * Crea los clusters.
 *
 * @private
 * /
MarkerClusterer.prototype.createClusters_ = function () {
  if (! this.ready_) {
    regreso;
  }

  // Obtener nuestros l�mites de vista de mapa actuales.
  // Crea un nuevo objeto de l�mites para que no afectemos el mapa.
  var mapBounds = new google.maps.LatLngBounds (this.map_.getBounds (). getSouthWest (),
      this.map_.getBounds (). getNorthEast ());
  var lines = this.getExtendedBounds (mapBounds);

  para (var i = 0, marcador; marcador = this.markers_ [i]; i ++) {
    if (! marker.isAdded && this.isMarkerInBounds_ (marcador, l�mites)) {
      this.addToClosestCluster_ (marcador);
    }
  }
};


/ **
 * Un cluster que contiene marcadores.
 *
 * @param {MarkerClusterer} markerClusterer El markerclusterer que esto
 * cl�ster est� asociado con.
 * @constructor
 * @ignorar
 * /
funci�n Cluster (markerClusterer) {
  this.markerClusterer_ = markerClusterer;
  this.map_ = markerClusterer.getMap ();
  this.gridSize_ = markerClusterer.getGridSize ();
  this.minClusterSize_ = markerClusterer.getMinClusterSize ();
  this.averageCenter_ = markerClusterer.isAverageCenter ();
  this.center_ = null;
  this.markers_ = [];
  this.bounds_ = null;
  this.clusterIcon_ = new ClusterIcon (this, markerClusterer.getStyles (),
      markerClusterer.getGridSize ());
}

/ **
 * Determina si ya se ha agregado un marcador al cl�ster.
 *
 * @param {google.maps.Marker} marker El marcador a verificar.
 * @return {boolean} Verdadero si el marcador ya est� agregado.
 * /
Cluster.prototype.isMarkerAlreadyAdded = function (marker) {
  if (this.markers_.indexOf) {
    devuelve this.markers_.indexOf (marker)! = -1;
  } else {
    para (var i = 0, m; m = this.markers_ [i]; i ++) {
      si (m == marcador) {
        devuelve verdadero
      }
    }
  }
  falso retorno;
};


/ **
 * Agrega un marcador al cluster.
 *
 * @param {google.maps.Marker} marcador El marcador a agregar.
 * @return {boolean} Verdadero si se agreg� el marcador.
 * /
Cluster.prototype.addMarker = function (marker) {
  if (this.isMarkerAlreadyAdded (marcador)) {
    falso retorno;
  }

  if (! this.center_) {
    this.center_ = marker.getPosition ();
    this.calculateBounds_ ();
  } else {
    if (this.averageCenter_) {
      var l = this.markers_.length + 1;
      var lat = (this.center_.lat () * (l-1) + marker.getPosition (). lat ()) / l;
      var lng = (this.center_.lng () * (l-1) + marker.getPosition (). lng ()) / l;
      this.center_ = new google.maps.LatLng (lat, lng);
      this.calculateBounds_ ();
    }
  }

  marker.isAdded = true;
  this.markers_.push (marcador);

  var len = this.markers_.length;
  if (len <this.minClusterSize_ && marker.getMap ()! = this.map_) {
    // No se alcanz� el tama�o m�nimo del cluster, as� que muestre el marcador.
    marker.setMap (this.map_);
  }

  if (len == this.minClusterSize_) {
    // Ocultar los marcadores que se mostraban.
    para (var i = 0; i <len; i ++) {
      this.markers_ [i] .setMap (null);
    }
  }

  if (len> = this.minClusterSize_) {
    marker.setMap (null);
  }

  this.updateIcon ();
  devuelve verdadero
};


/ **
 * Devuelve el marcador de cl�ster al que est� asociado el cl�ster.
 *
 * @return {MarkerClusterer} El agrupador de marcadores asociado.
 * /
Cluster.prototype.getMarkerClusterer = function () {
  devuelve this.markerClusterer_;
};


/ **
 * Devuelve los l�mites del cluster.
 *
 * @return {google.maps.LatLngBounds} los l�mites del cl�ster.
 * /
Cluster.prototype.getBounds = function () {
  var lines = new google.maps.LatLngBounds (this.center_, this.center_);
  marcadores var = this.getMarkers ();
  para (var i = 0, marcador; marcador = marcadores [i]; i ++) {
    lines.extend (marker.getPosition ());
  }
  l�mites de retorno;
};


/ **
 * Elimina el cluster
 * /
Cluster.prototype.remove = function () {
  this.clusterIcon_.remove ();
  this.markers_.length = 0;
  eliminar this.markers_;
};


/ **
 * Devuelve el n�mero de marcadores en el cl�ster.
 *
 * @return {n�mero} El n�mero de marcadores en el grupo.
 * /
Cluster.prototype.getSize = function () {
  devuelve this.markers_.length;
};


/ **
 * Devuelve una lista de los marcadores en el cl�ster.
 *
 * @return {Array. <google.maps.Marker>} Los marcadores en el cl�ster.
 * /
Cluster.prototype.getMarkers = function () {
  devuelve this.markers_;
};


/ **
 * Devuelve el centro del cluster.
 *
 * @return {google.maps.LatLng} El centro del cl�ster.
 * /
Cluster.prototype.getCenter = function () {
  devuelve this.center_;
};


/ **
 * Calcul� los l�mites extendidos del cl�ster con la cuadr�cula.
 *
 * @private
 * /
Cluster.prototype.calculateBounds_ = function () {
  var lines = new google.maps.LatLngBounds (this.center_, this.center_);
  this.bounds_ = this.markerClusterer_.getExtendedBounds (l�mites);
};


/ **
 * Determina si un marcador se encuentra en los l�mites de los grupos.
 *
 * @param {google.maps.Marker} marker El marcador a verificar.
 * @return {boolean} Verdadero si el marcador se encuentra dentro de los l�mites.
 * /
Cluster.prototype.isMarkerInClusterBounds = function (marker) {
  devuelve this.bounds_.contains (marker.getPosition ());
};


/ **
 * Devuelve el mapa al que est� asociado el cl�ster.
 *
 * @return {google.maps.Map} El mapa.
 * /
Cluster.prototype.getMap = function () {
  devuelve this.map_;
};


/ **
 * Actualiza el icono del cl�ster.
 * /
Cluster.prototype.updateIcon = function () {
  var zoom = this.map_.getZoom ();
  var mz = this.markerClusterer_.getMaxZoom ();

  if (mz && zoom> mz) {
    // El zoom es mayor que nuestro zoom m�ximo, as� que muestre todos los marcadores en el cl�ster.
    para (var i = 0, marcador; marcador = this.markers_ [i]; i ++) {
      marker.setMap (this.map_);
    }
    regreso;
  }

  if (this.markers_.length <this.minClusterSize_) {
    // Min tama�o de grupo a�n no alcanzado.
    this.clusterIcon_.hide ();
    regreso;
  }

  var numStyles = this.markerClusterer_.getStyles (). length;
  var sums = this.markerClusterer_.getCalculator () (this.markers_, numStyles);
  this.clusterIcon_.setCenter (this.center_);
  this.clusterIcon_.setSums (sumas);
  this.clusterIcon_.show ();
};


/ **
 * Un icono de cl�ster
 *
 * @param {Cluster} cluster El cluster a asociar.
 * @param {Objeto} estilos Un objeto que tiene propiedades de estilo:
 * 'url': (cadena) La url de la imagen.
 * 'altura': (n�mero) La altura de la imagen.
 * 'ancho': (n�mero) El ancho de la imagen.
 * 'ancla': (Array) La posici�n de ancla del texto de la etiqueta.
 * 'textColor': (cadena) El color del texto.
 * 'textSize': (n�mero) El tama�o del texto.
 * 'backgroundPosition: (string) La posici�n del fondo x, y.
 * @param {n�mero =} opt_padding Relleno opcional para aplicar al icono del cl�ster.
 * @constructor
 * @extends google.maps.OverlayView
 * @ignorar
 * /
funci�n ClusterIcon (cluster, styles, opt_padding) {
  cluster.getMarkerClusterer (). extend (ClusterIcon, google.maps.OverlayView);

  this.styles_ = styles;
  this.padding_ = opt_padding || 0;
  this.cluster_ = cluster;
  this.center_ = null;
  this.map_ = cluster.getMap ();
  this.div_ = null;
  this.sums_ = null;
  this.visible_ = false;

  this.setMap (this.map_);
}


/ **
 * Desencadena el evento clusterclick y el zoom si la opci�n est� configurada.
 * /
ClusterIcon.prototype.triggerClusterClick = function () {
  var markerClusterer = this.cluster_.getMarkerClusterer ();

  // Activar el evento clusterclick.
  google.maps.event.trigger (markerClusterer.map_, 'clusterclick', this.cluster_);

  if (markerClusterer.isZoomOnClick ()) {
    // Zoom en el cluster.
    this.map_.fitBounds (this.cluster_.getBounds ());
  }
};


/ **
 * A�adiendo el icono del cl�ster al dom.
 * @ignorar
 * /
ClusterIcon.prototype.onAdd = function () {
  this.div_ = document.createElement ('DIV');
  si (esto.visible_) {
    var pos = this.getPosFromLatLng_ (this.center_);
    this.div_.style.cssText = this.createCss (pos);
    this.div_.innerHTML = this.sums_.text;
  }

  var panes = this.getPanes ();
  panes.overlayMouseTarget.appendChild (this.div_);

  var que = esto;
  google.maps.event.addDomListener (this.div_, 'click', function () {
    that.triggerClusterClick ();
  });
};


/ **
 * Devuelve la posici�n para colocar la divisi�n div en el latlng.
 *
 * @param {google.maps.LatLng} latlng La posici�n en latlng.
 * @return {google.maps.Point} La posici�n en p�xeles.
 * @private
 * /
ClusterIcon.prototype.getPosFromLatLng_ = function (latlng) {
  var pos = this.getProjection (). fromLatLngToDivPixel (latlng);
  pos.x - = parseInt (this.width_ / 2, 10);
  pos.y - = parseInt (this.height_ / 2, 10);
  retorno pos;
};


/ **
 * Dibuja el icono.
 * @ignorar
 * /
ClusterIcon.prototype.draw = function () {
  si (esto.visible_) {
    var pos = this.getPosFromLatLng_ (this.center_);
    this.div_.style.top = pos.y + 'px';
    this.div_.style.left = pos.x + 'px';
    this.div_.style.zIndex = google.maps.Marker.MAX_ZINDEX + 1;
  }
};


/ **
 * Ocultar el icono.
 * /
ClusterIcon.prototype.hide = function () {
  if (this.div_) {
    this.div_.style.display = 'none';
  }
  this.visible_ = false;
};


/ **
 * Posiciona y muestra el icono.
 * /
ClusterIcon.prototype.show = function () {
  if (this.div_) {
    var pos = this.getPosFromLatLng_ (this.center_);
    this.div_.style.cssText = this.createCss (pos);
    this.div_.style.display = '';
  }
  this.visible_ = true;
};


/ **
 * Eliminar el icono del mapa.
 * /
ClusterIcon.prototype.remove = function () {
  this.setMap (null);
};


/ **
 * Implementaci�n de la interfaz onRemove.
 * @ignorar
 * /
ClusterIcon.prototype.onRemove = function () {
  if (this.div_ && this.div_.parentNode) {
    this.hide ();
    this.div_.parentNode.removeChild (this.div_);
    this.div_ = null;
  }
};


/ **
 * Establecer las sumas del icono.
 *
 * @param {Object} sums Las sumas que contienen:
 * 'texto': (cadena) El texto a mostrar en el icono.
 * '�ndice': (n�mero) El �ndice de estilo del icono.
 * /
ClusterIcon.prototype.setSums = function (sums) {
  this.sums_ = sums;
  this.text_ = sums.text;
  this.index_ = sums.index;
  if (this.div_) {
    this.div_.innerHTML = sums.text;
  }

  this.useStyle ();
};


/ **
 * Establece el icono a los estilos.
 * /
ClusterIcon.prototype.useStyle = function () {
  var index = Math.max (0, this.sums_.index - 1);
  index = Math.min (this.styles_.length - 1, index);
  var style = this.styles_ [index];
  this.url_ = style ['url'];
  this.height_ = style ['height'];
  this.width_ = style ['width'];
  this.textColor_ = style ['textColor'];
  this.anchor_ = style ['anchor'];
  this.textSize_ = style ['textSize'];
  this.backgroundPosition_ = style ['backgroundPosition'];
};


/ **
 * Establece el centro del icono.
 *
 * @param {google.maps.LatLng} center La latlng para establecer como centro.
 * /
ClusterIcon.prototype.setCenter = function (center) {
  this.center_ = center;
};


/ **
 * Crea el texto css basado en la posici�n del icono.
 *
 * @param {google.maps.Point} pos La posici�n.
 * @return {string} El texto de estilo css.
 * /
ClusterIcon.prototype.createCss = function (pos) {
  var style = [];
  style.push ('background-image: url (' + this.url_ + ');');
  var backgroundPosition = this.backgroundPosition_? this.backgroundPosition_: '0 0';
  style.push ('background-position:' + backgroundPosition + ';');

  if (typeof this.anchor_ === 'object') {
    if (typeof this.anchor_ [0] === 'number' && this.anchor_ [0]> 0 &&
        this.anchor_ [0] <this.height_) {
      style.push ('height:' + (this.height_ - this.anchor_ [0]) +
          'px; padding-top: '+ this.anchor_ [0] +' px; ');
    } else {
      style.push ('height:' + this.height_ + 'px; line-height:' + this.height_ +
          'px;');
    }
    if (typeof this.anchor_ [1] === 'number' && this.anchor_ [1]> 0 &&
        this.anchor_ [1] <this.width_) {
      style.push ('width:' + (this.width_ - this.anchor_ [1]) +
          'px; relleno-izquierda: '+ this.anchor_ [1] +' px; ');
    } else {
      style.push ('width:' + this.width_ + 'px; text-align: center;');
    }
  } else {
    style.push ('height:' + this.height_ + 'px; line-height:' +
        this.height_ + 'px; ancho: '+ this.width_ +' px; text-align: center; ');
  }

  var txtColor = this.textColor_? this.textColor_: 'black';
  var txtSize = this.textSize_? this.textSize_: 11;

  style.push ('cursor: puntero; arriba:' + pos.y + 'px; izquierda:' +
      pos.x + 'px; color: '+ txtColor +'; posici�n: absoluta; tama�o de letra: '+
      txtSize + 'px; Familia tipogr�fica: Arial, sans-serif; fuente-peso: negrita ');
  return style.join ('');
};


// Exportar s�mbolos para el cierre
// Si no va a compilar con cierre, puede eliminar el
// c�digo abajo.
var ventana = ventana || {};
ventana ['MarkerClusterer'] = MarkerClusterer;
MarkerClusterer.prototype ['addMarker'] = MarkerClusterer.prototype.addMarker;
MarkerClusterer.prototype ['addMarkers'] = MarkerClusterer.prototype.addMarkers;
MarkerClusterer.prototype ['clearMarkers'] =
    MarkerClusterer.prototype.clearMarkers;
MarkerClusterer.prototype ['fitMapToMarkers'] =
    MarkerClusterer.prototype.fitMapToMarkers;
MarkerClusterer.prototype ['getCalculator'] =
    MarkerClusterer.prototype.getCalculator;
MarkerClusterer.prototype ['getGridSize'] =
    MarkerClusterer.prototype.getGridSize;
MarkerClusterer.prototype ['getExtendedBounds'] =
    MarkerClusterer.prototype.getExtendedBounds;
MarkerClusterer.prototype ['getMap'] = MarkerClusterer.prototype.getMap;
MarkerClusterer.prototype ['getMarkers'] = MarkerClusterer.prototype.getMarkers;
MarkerClusterer.prototype ['getMaxZoom'] = MarkerClusterer.prototype.getMaxZoom;
MarkerClusterer.prototype ['getStyles'] = MarkerClusterer.prototype.getStyles;
MarkerClusterer.prototype ['getTotalClusters'] =
    MarkerClusterer.prototype.getTotalClusters;
MarkerClusterer.prototype ['getTotalMarkers'] =
    MarkerClusterer.prototype.getTotalMarkers;
MarkerClusterer.prototype ['redraw'] = MarkerClusterer.prototype.redraw;
MarkerClusterer.prototype ['removeMarker'] =
    MarkerClusterer.prototype.removeMarker;
MarkerClusterer.prototype ['removeMarkers'] =
    MarkerClusterer.prototype.removeMarkers;
MarkerClusterer.prototype ['resetViewport'] =
    MarkerClusterer.prototype.resetViewport;
MarkerClusterer.prototype ['repaint'] =
    MarkerClusterer.prototype.repaint;
MarkerClusterer.prototype ['setCalculator'] =
    MarkerClusterer.prototype.setCalculator;
MarkerClusterer.prototype ['setGridSize'] =
    MarkerClusterer.prototype.setGridSize;
MarkerClusterer.prototype ['setMaxZoom'] =
    MarkerClusterer.prototype.setMaxZoom;
MarkerClusterer.prototype ['onAdd'] = MarkerClusterer.prototype.onAdd;
MarkerClusterer.prototype ['draw'] = MarkerClusterer.prototype.draw;

Cluster.prototype ['getCenter'] = Cluster.prototype.getCenter;
Cluster.prototype ['getSize'] = Cluster.prototype.getSize;
Cluster.prototype ['getMarkers'] = Cluster.prototype.getMarkers;

ClusterIcon.prototype ['onAdd'] = ClusterIcon.prototype.onAdd;
ClusterIcon.prototype ['draw'] = ClusterIcon.prototype.draw;
ClusterIcon.prototype ['onRemove'] = ClusterIcon.prototype.onRemove;

Object.keys = Object.keys || funci�n (o) {
    var resultado = [];
    para (nombre var en o) {
        if (o.hasOwnProperty (nombre))
          result.push (nombre);
    }
    resultado de retorno
};

if (tipo de m�dulo == 'objeto') {
  module.exports = MarkerClusterer;
}