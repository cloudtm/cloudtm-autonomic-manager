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

            <h2 id="slogan"><span>Submitted SLA Request</span></h2>
        <p>Here are listed all your submitted template. If you haven't uploaded your application, you can do it now.</p>    
           
    <table id="box-table-a">
     <thead>
     <tr>
     	
        <th scope="col">ID Request</th>
        <th scope="col">Submission Date</th>
        <th scope="col">Template</th>
        <th scope="col" width=300 >Application (if uploaded)</th>
        <th scope="col">Status</th>
        <th scope="col"></th>
      </tr> 
     </thead>
     <tbody>
       <c:forEach var="template" items="${templateList}" varStatus="counter">
           
      <tr>

      <td>      <a href="ViewTemplateServlet?action=delete&id_template=${template.template.id}"><img src="/CloudTMSla/design/delete.png" ></a> ${template.template.id}</td>
      <td>${template.template.creationDate}</td>
      <td><a href="${pageContext.request.contextPath}/DownloadServlet?file=${template.template.id}.xml">Download</a>
       - 
        <a href="TemplateReaderServlet?id_template=${template.template.id}"  class="example2demo" name="windowX">View</a>
      <script type="text/javascript"> 
$('.example2demo').popupWindow({ 
centerBrowser:1,
height:600, 
width:800
}); 
</script>
      </td>
       
        <c:choose>
        <c:when test="${template.application.appname == null}">
               
       <form  method="post" name="template" action="ApplicationServlet" enctype="multipart/form-data">
        <td>
        
        <input type="hidden" name="id_template" value="${template.template.id}" />
        <input type="file" size="0" name="file" />
               
<input name="submit" type="submit" value="Confirm" />&nbsp;&nbsp;
</td>
         </form>
        </c:when>
  
        <c:otherwise>
        <td>${template.application.appname}</td>
        </c:otherwise>
        </c:choose>
        <td>${template.application.app_status}</td>
        <td><c:if test="${(template.application.app_status=='Predictions Available') || (currentSessionUser.level==0) }"><a href="ViewPendingSlaPrediction?id_template=${template.template.id}" >Predictions
         </a></c:if>
        <c:if test="${template.pendingSla==null}">Not Available</c:if>
        </td>
      </tr>
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
          
          Boxy.DEFAULTS.title = 'Template Data';
          
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
 
 

 
 
