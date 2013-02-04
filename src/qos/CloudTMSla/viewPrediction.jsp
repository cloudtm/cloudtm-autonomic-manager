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

            <h2 id="slogan"><span>Predictions</span></h2>
            
            <p>Here are listed all Sla prediction with their WL Characterization</p>
             <c:set var="id_template" value="${id_template}"  />
      <form name="" method="GET" action="SlaPredictionServlet">
        <input type="hidden" name="action" value="addSlaPrediction" />
     <input type="hidden" name="id_template" value="${id_template}" />
<div class="buttons">
       
    
    <button type="submit" class="positive" name="save">
  
        <img src="/CloudTMSla/design/apply2.png" alt=""/>
        New Prediction
    </button>

</div>

</form> 

         <table id="box-table-a">
     <thead>
     <tr>
        <th scope="col">ID Prediction</th>
        <th scope="col">Issue Date</th>
        
        <th scope="col">Template</th>
        
        <th scope="col"></th>
        <th scope="col">Set Status</th>
        <c:if test="${currentSessionUser.level==0}">
        <th scope="col">Upload data</th>
        </c:if>
      </tr> 
     </thead>
     <tbody>
       <c:forEach var="prediction" items="${predictionList}" varStatus="counter">
           
      <tr>
      <td>${prediction.slaPrediction.id_sla_prediction}</td>
      <td><fmt:formatDate value="${prediction.slaPrediction.creationDate}" pattern="dd MMM yy HH:mm" /></td>
      
      <td><a href="TemplateReaderServlet?id_template=${id_template}"  class="example2demo" name="windowX">View</a>
      <script type="text/javascript"> 
$('.example2demo').popupWindow({ 
centerBrowser:1,
height:600, 
width:800
}); 
</script></td>
      <td>
      <c:if test="${!empty prediction.charts}">
      <form action="ViewPredictionCharts" method="GET">
      <input type="hidden" name="id_sla_prediction" value="${prediction.slaPrediction.id_sla_prediction}" />
      <input type="submit" value="View Charts">
      </form>
      </c:if>
      </td>
      
      <td>
      <form action="SlaPredictionServlet" method="GET"><select name="id_template_status">
      <option>Select State</option>
      
      <c:forEach var="templateStatus" items="${templateStatusList}" varStatus="counter">
      <option value="${templateStatus.id_template_status}"<c:if test="${prediction.slaPrediction.status.id_template_status == templateStatus.id_template_status}">
        selected="selected"     
        </c:if>>${templateStatus.name}</option>
      </c:forEach>
      </select>
      <input type="hidden" name="id_sla_prediction" value="${prediction.slaPrediction.id_sla_prediction}" />
      <input type="hidden" name="action" value="updateSlaPredictionStatus" />
      <input type="submit" value="update">
      </form>
      </td>
      <c:if test="${currentSessionUser.level==0}">
      <td>
      <a href="RhqConfigServlet?id_sla_prediction=${prediction.slaPrediction.id_sla_prediction}" title="Upload Data" class="example2demo" name="windowX">Settings</a>
      <script type="text/javascript"> 
$('.example2demo').popupWindow({ 
centerBrowser:1,
height:600, 
width:400
}); 
</script>
      </td>
      </c:if>
      </tr>
      </c:forEach>
      
     </tbody> 
    </table>
   
         
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
 
 

 
 
