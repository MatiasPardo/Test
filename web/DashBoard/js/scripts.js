Apex.grid = {
  padding: {
    right: 0,
    left: 0
  }
}

Apex.dataLabels = {
  enabled: false
}
var randomizeArray = function (arg) {
  var array = arg.slice();
  var currentIndex = array.length, temporaryValue, randomIndex;
  
  while (0 !== currentIndex) {

    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex -= 1;

    temporaryValue = array[currentIndex];
    array[currentIndex] = array[randomIndex];
    array[randomIndex] = temporaryValue;
  }

  return array;
}


// the default colorPalette for this dashboard
//var colorPalette = ['#01BFD6', '#5564BE', '#F7A600', '#EDCD24', '#F74F58'];
//var colorPalette = ['#00D8B6','#008FFB',  '#FEB019', '#FF4560', '#775DD0']
var colorPalette = ['#008FFB','#00D8B6',  '#FEB019', '#FF4560', '#775DD0']


for(var i=0;i<sparks.length;i++){
	var spark = {
			  chart: {
			    id: 'sparkline1',
			    group: 'sparklines',
			    type: 'area',
			    height: 160,
			    sparkline: {
			      enabled: true
			    },
			  },
			  stroke: {
			    curve: 'straight'
			  },
			  fill: {
			    opacity: 1,
			  },
			  series: [{
			    name: sparks[i].name,		//******name
			    data: sparks[i].serie[0].datas	//*****datas
				}],
			  labels: sparks[i].serie[i].labels, 		//******labels
			  yaxis: {min: 0},
			  
			  colors: ['#00D8B6'], 
			  title: {
			    text: sparks[i].value, //*****value
			    offsetX: 30,
			    style: {
			      fontSize: '24px',
			      cssClass: 'apexcharts-yaxis-title'
			    }
			  },
			  subtitle: {
			    text: sparks[i].name,  //********name
			    offsetX: 30,
			    style: {
			      fontSize: '14px',
			      cssClass: 'apexcharts-yaxis-title'
			    }
			  }
			}
	new ApexCharts(document.querySelector("#spark"+(i+1).toString()), spark).render();

}	


for(var i=0;i<bars.length;i++){
	
		var optionsBar = {
		  chart: {
		    type: 'bar',
		    height: 350,
		    width: '100%',
		    stacked: true,
		  },
		  plotOptions: {
		    bar: {
		      columnWidth: '45%',
		    }
		  },
		  colors: colorPalette,
		  series: bars[i].serie,//[{name:'Costos',data:[9.3,9.9,9.1,0,1,2,3,4,5,6,7]},{name:'Ventas',data:[4.3,5.9,5.1,0,1,2,3,4,5,6,7]}],
		  labels: bars[i].labels,
		  xaxis: {
		    labels: {
		      show: true
		    },
		    axisBorder: {
		      show: false
		    },
		    axisTicks: {
		      show: false
		    },
		  },
		  yaxis: {
		    axisBorder: {
		      show: false
		    },
		    axisTicks: {
		      show: false
		    },
		    labels: {
		      style: {
		        color: '#78909c'
		      }
		    }
		  },
		  title: {
		    text: bars[i].name,
		    align: 'left',
		    style: {
		      fontSize: '18px'
		    }
		  }

		}
		new ApexCharts(document.querySelector("#bar"+(i+1).toString()), optionsBar).render();
	//	new ApexCharts(document.qyerySelector("#bar1"),optionsBar).render();
	//	new ApexCharts(document.qyerySelector("#bar2"),optionsBar).render();
}

for(var i=0;i<donas.length;i++){
	
	var optionDonut = {
	  chart: {
	      type: 'donut',
	      width: '100%'
	  },
	  dataLabels: {
	    enabled: false,
	  },
	  plotOptions: {
	    pie: {
	      donut: {
	        size: '70%',
	      },
	      offsetY: 20,
	    },
	    stroke: {
	      colors: undefined
	    }
	  },
	  colors: colorPalette,
	  title: {
	    text: donas[i].serie[i].name,
	    style: {
	      fontSize: '18px'
	    }
	  },
	  series: donas[i].serie[i].datas,
	  labels: donas[i].labels,
	  legend: {
	    position: 'left',
	    offsetY: 80
	  }
	}

	new ApexCharts(document.querySelector("#donut"+(i+1).toString()), optionDonut).render();

}
for(var i=0;i<rads.length;i++){

var options = {
        series: rads[i].serie,
        chart: {
          height: 350,
          type: 'radar',
          dropShadow: {
            enabled: true,
            blur: 1,
            left: 1,
            top: 1
          }
        },
        title: {
          text: rads[i].name
        },
        stroke: {
          width: 0
        },
        fill: {
          opacity: 0.4
        },
        markers: {
          size: 1
        }
      };

	new ApexCharts(document.querySelector("#rad"+(i+1).toString()), options).render();
}
    
for(var i=0;i<mixed.length;i++){

      var options = {
    		  colors: colorPalette,
    	      series: mixed[i].serie,
    		  chart: {
    	          height: 350,
    	          type: 'line',
    	          stacked: false,
    	        },
    	        stroke: {
    	          width: [4,4,4,4,4,4,4,4,4,4,4,4],
    	          curve: 'straight'
    	        },
    	        plotOptions: {
    	          bar: {
    	            columnWidth: '50%'
    	          }
    	        },
    	        title: {
    	            text: mixed[i].name,
    	            align: 'left'
    	          },
    	        fill: {
    	        	gradient: {
    	            inverseColors: false,
    	            shade: 'light',
    	            type: "vertical",
    	            opacityFrom: 0.85,
    	            opacityTo: 0.55,
    	          }
    	        },
    	        labels: mixed[i].labels,
    	        markers: {
    	          size: 0
    	        },
    	        xaxis: {
    	          type: 'datetime'
    	        },
    	        yaxis: {
    	          title: {
    	            text: 'Points',
    	          },
    	          min: 0
    	        },
    	        tooltip: {
    	          shared: true,
    	          intersect: false,
    	          y: {
    	            formatter: function (y) {
    	              if (typeof y !== "undefined") {
    	                return y.toFixed(0) + " points";
    	              }
    	              return y;
    	        
    	            }
    	          }
    	        }
    	      };

  	new ApexCharts(document.querySelector("#mixed"+(i+1).toString()), options).render();

      }	    
    



chartLine.render().then(function () {
  var ifr = document.querySelector("#wrapper");
  if (ifr.contentDocument) {
    ifr.style.height = ifr.contentDocument.body.scrollHeight + 20 + 'px';
  }
});


// on smaller screen, change the legends position for donut

