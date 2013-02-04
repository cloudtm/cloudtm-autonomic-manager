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
    %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="header.jsp" %>    
<jsp:useBean id="templateList" class="java.util.ArrayList"
  scope="request" />
 
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

    <!-- Promo -->
    <div id="col-top"></div>
    <div class="box" id="col">
    
        <div id="ribbon"></div> <!-- /ribbon (design/ribbon.gif) -->
        
        <!-- Screenshot in browser (replace tmp/browser.gif) -->
        <div id="col-browser"></div> 
        
        <div id="col-text">

            <h2 id="slogan"><span>View SLA Charts</span></h2>
            
            <p></p>
             <c:set var="id_template" value="${id_template}"  />
			 <c:set var="id_sla_prediction" value="${id_sla_prediction}"  />
      
      <form action="ViewPredictionCharts" method="GET"><select name="id_sla_chart_group">
      <option value="0">Select Optimization</option>
      
      <c:forEach var="groupList" items="${groupList}" varStatus="counter">
      <option value="${groupList.id_sla_chart_group}"  <c:if test="${groupList.id_sla_chart_group == param.id_sla_chart_group}">
        selected="selected"     
        </c:if>>${groupList.name}</option>
      </c:forEach>
      </select>
      <input type="hidden" name="id_sla_prediction" value="${id_sla_prediction}" />
      <select name="id_transactional_class">
      <option value="0">Select Transactional Class</option>
      <c:forEach var="listTransactional" items="${listTransactional}" varStatus="counter">
      <option value="${listTransactional.id_transactional_class}" <c:if test="${listTransactional.id_transactional_class == param.id_transactional_class}">
        selected="selected"     
        </c:if>
        >${listTransactional.name}</option>
      </c:forEach>
      </select>
      
      
      
      <input type="submit" value="View Charts">
      </form>
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
			title: {text: '<c:if test="${chart.attribute.name=='Nodes'}"># nodes</c:if><c:if test="${chart.attribute.name=='Clients'}"># Clients</c:if><c:if test="${chart.attribute.name=='Abort Rate'}">%</c:if><c:if test="${chart.attribute.name=='Response Time'}">milliseconds</c:if>'},
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
    
    
 <script type='text/javascript'>
      
        var diagnose = function(boxy) {
            alert("Position: " + boxy.getPosition() +
                  "\nSize: " + boxy.getSize() +
                  "\nContent size: " + boxy.getContentSize() +
                  "\nCenter: " + boxy.getCenter());
        };
      
        $(function() {
          
          Boxy.DEFAULTS.title = 'Upload Data';
          
          //
          // Diagnostics
          
          $('#diagnostics').click(function() {
              new Boxy("<div><a href='#' onclick='diagnose(Boxy.get(this));'>Diagnose</a></div>");
              return false;
          });
        
          //
          // Set content
          
          var setContent = null;
          $('#set-content-open').click(function() {
              setContent = new Boxy(
                "<div style='background-color:red'>This is content</div>", {
                  behaviours: function(c) {
                    c.hover(function() {
                      $(this).css('backgroundColor', 'green');
                    }, function() {
                      $(this).css('backgroundColor', 'pink');
                    });
                  }
                }
              );
              return false;
          });
          $('#set-content').click(function() {
              setContent.setContent("<div style='background-color:blue'>This is new content</div>");
              return false;
          });
          
          //
          // Callbacks
          
          $('#after-hide').click(function() {
              new Boxy("<div>Test content</div>", {
                afterHide: function() {
                  alert('after hide called');
                }
              });
              return false;
          });
          
          $('#before-unload').click(function() {
              new Boxy("<div>Test content</div>", {
                beforeUnload: function() {
                  alert('before unload called');
                },
                unloadOnHide: true
              });
              return false;
          });
          
          $('#before-unload-no-auto-unload').click(function() {
              new Boxy("<div>Test content</div>", {
                beforeUnload: function() {
                  alert('should not see this');
                },
                unloadOnHide: false
              });
              return false;
          });
          
          $('#after-drop').click(function() {
              new Boxy("<div>Test content</div>", {
                afterDrop: function() {
                  alert('after drop: ' + this.getPosition());
                },
                draggable: true
              });
              return false;
          });
          
          $('#after-show').click(function() {
              new Boxy("<div>Test content</div>", {
                afterShow: function() {
                  alert('after show: ' + this.getPosition());
                }
              });
              return false;
          });
          
          //
          // Z-index
          
          var zIndex = null;
          $('#z-index').click(function() {
              zIndex = new Boxy(
                "<div>Test content</div>", { clickToFront: true }
              );
              return false;
          });
          
          $('#z-index-latest').click(function() {
              zIndex.toTop();
              return false;
          });
          
          //
          // Modals
          
          function newModal() {
              new Boxy("<div><a href='#'>Open a stacked modal</a> | <a href='#' onclick='alert(Boxy.isModalVisible()); return false;'>test for modal dialog</a></div>", {
                modal: true, behaviours: function(c) {
                  c.find('a:first').click(function() {
                    newModal();
                  });
                }
              });
          };
          
          $('#modal').click(newModal);
          
          //
          // No-show
          
          var noShow;
          $('#no-show').click(function() {
              noShow = new Boxy("<div>content</div>", {show: false});
              return false;
          });
          
          $('#no-show-now').click(function() {
              noShow.show();
              return false;
          });
          
          // Actuator
          
          $('#actuator').click(function() {
              var ele = $('#actuator-toggle')[0];
              new Boxy("<div>test content</div>", {actuator: ele, show: false});
              return false;
          });
          $('#actuator-toggle').click(function() {
              Boxy.linkedTo(this).toggle();
              return false;
          });
          
        });
      </script>



<%@ include file="footer.jsp" %>
 
 

 
 
