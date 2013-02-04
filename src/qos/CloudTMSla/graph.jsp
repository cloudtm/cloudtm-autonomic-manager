<!--
CINI, Consorzio Interuniversitario Nazionale per l'Informatica
Copyright 2013 CINI and/or its affiliates and other
contributors as indicated by the @author tags. All rights reserved.
See the copyright.txt in the distribution for a full listing of
individual contributors.

This is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 3.0 of
the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this software; if not, write to the Free
Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
02110-1301 USA, or see the FSF site: http://www.fsf.org.
-->

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<!-- 1. Add these JavaScript inclusions in the head of your page -->
		<script type="text/javascript" src="/CloudTMSla/script/jquery.min.js"></script>
    <script type="text/javascript" src="/CloudTMSla/script/highcharts.src.js"></script>
<script type="text/javascript">
$(document).ready(function()
		{
		Highcharts.setOptions({
		global: {useUTC: true}
		});

		var Mychart1, options = {
		chart: {
		renderTo: 'container1',
		defaultSeriesType: 'column',
		backgroundColor: null,
		         events: {
		            load: function() {
		            
		              
		            }
		         }
		},
		credits: {enabled: false},
		legend: {enabled: false},
		plotOptions: {
			series: {
                stacking: 'normal'
            }
		},
		xAxis: {
			title: {text: 'Load (tx/s)'}	
		},
		yAxis: {
			stackLabels: {
                enabled: true,
               
            }
		},
		tooltip: {
		formatter: function() {
			 return '<b>'+ this.x +'</b><br/>'+
             this.series.name +': '+ this.y +'<br/>'+
             'Total: '+ this.point.stackTotal;
		}
		},
		exporting: {enabled: false},
		series: []
		};

		var id_chart = 30;  
		$.getJSON('JsonGetChartServlet', { id_chart: id_chart } ,  function(JSONResponse)
		{
			
		options.series = JSONResponse.data;
		Mychart1= new Highcharts.Chart(options);
		
		
		});

		});
			
		
		</script>
		
	
<body>
		
		<!-- 3. Add the container -->
		<div id="container1" style="width: 800px; height: 400px; margin: 0 auto"></div>

</body>
</html>
