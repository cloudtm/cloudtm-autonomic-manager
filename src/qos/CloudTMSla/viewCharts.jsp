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
    pageEncoding="ISO-8859-1"
    import="authentication.LoginBean"
    %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="header.jsp" %>    

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

 
    <!-- Promo -->
    <div id="col-top"></div>
    <div class="box" id="col">
    
        <div id="ribbon"></div> <!-- /ribbon (design/ribbon.gif) -->
        
        <!-- Screenshot in browser (replace tmp/browser.gif) -->
        <div id="col-browser"></div> 
        
        <div id="col-text">

            <h2 id="slogan"><span>Prediction Charts</span></h2>
        <p>Here are listed all your submitted template. If you haven't uploaded your application, you can do it now.</p>    
    
    <c:forEach var="chart" items="${charts}" varStatus="counter">
     <script type="text/javascript">
$(document).ready(function()
		{
		

		var Mychart${counter.count}, options = {
		chart: {
		renderTo: 'container${counter.count}',
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
			title: {text: '${chart.attribute.name}'},
			stackLabels: {
                enabled: true,
               
            }
		},
		tooltip: {
			formatter: function() {
				 return '<b>Load: '+ this.x +' tx/s</b><br/>'+
				 (this.series.name == 'thre' ? 'Sla threshold surpassed by '+this.y : '');
			}
			},
		exporting: {enabled: false},
		series: []
		};

		var id_chart = ${chart.id_chart};  
		$.getJSON('JsonGetChartServlet', { id_chart: id_chart } ,  function(JSONResponse)
		{
			
		options.series = JSONResponse.data;
		Mychart${counter.count}= new Highcharts.Chart(options);
		Mychart${counter.count}.setTitle({text: '${chart.attribute.name}'});
		
		
		});

		});
			
		
		</script>
     
      </c:forEach>
    
    
    
    
    
    
    
    
    
    
           
    
     <c:forEach var="chart" items="${charts}" varStatus="counter">
     <table id="box-table-charts">
     <tr>
     <td>
     <div id="container${counter.count}" ></div>
     </td>
     </tr>
     </table>
     </c:forEach>
  
               
          
    

        </div> <!-- /col-text -->
    
    </div> <!-- /col -->
    <div id="col-bottom"></div>
    
    <hr class="noscreen">
    




<%@ include file="footer.jsp" %>
 
 

 
 
