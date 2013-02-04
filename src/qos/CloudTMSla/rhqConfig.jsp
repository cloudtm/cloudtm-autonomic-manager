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

<c:if test="${rhqConfig!=null}">
<div style='background-color: white; height: 400px; width: 350px; padding: 15px; color: black; font-size: 12px'>
 <c:set var="update" value="${update}"  />
 <c:if test="${update!=null}">
 <strong>${update}</strong>
 <script type="text/javascript"> 
 opener.location.reload();
</script>

 
	 </c:if>
	 		<table>
     <c:set var="rhqConfig" value="${rhqConfig}"  />
     <c:set var="id_sla_prediction" value="${id_sla_prediction}"  />
     
      <tr>
        
      </tr>
      <c:forEach var="Rhq_ConfigResource" items="${Rhq_ConfigResource}" varStatus="counter">
      <tr>
        <td width="131">${Rhq_ConfigResource.group.name}</td>
        <td width="308">
        File Uploaded : ${Rhq_ConfigResource.resource_name}
        
        </td>
        
      </tr>
      </c:forEach>
      
      
    </table>
    <input type="hidden" name="id_rhq_config" value="${rhqConfig.id_rhq_config}" />
	<input type="hidden" name="updateRhq" value="1" />
    
  	<form  method="post" action="PullRhqChartServlet"><input type="hidden" name="id_sla_prediction" value="${id_sla_prediction}" /><input type="hidden" name="id_rhqConfig" value="${rhqConfig.id_rhq_config}" /><input type="submit" value="Retrieve Data"></form>
  	
  	<table>
     
      <tr>
        
      </tr>
      <c:forEach var="group" items="${groupList}" varStatus="counter">
      <tr>
        <td width="131">${group.name}:</td>
        <td width="308">*&nbsp;
        <td>
        <form  method="post" action="RhqUploadServlet" enctype="multipart/form-data">
        <input type="hidden" name="id_rhq_config" value="${rhqConfig.id_rhq_config}" />
        <input type="hidden" name="id_template" value="${template.template.id}" />
        <input type="hidden" name="id_sla_prediction" value="${id_sla_prediction}" />
        <input type="hidden" name="id_group" value="${group.id_sla_chart_group}" />
        <input type="file" size="0" name="file"  />
                
        <input name="submit" type="submit" value="Upload" />
        </form>
       </td>
       
      </tr>
      </c:forEach>
       
      
    </table>
  	
  	
</div>
 </c:if>
 <c:if test="${rhqConfig==null}">
<div style='background-color: white; height: 400px; width: 350px; padding: 15px; color: black; font-size: 12px'>
<c:set var="id_sla_prediction" value="${id_sla_prediction}"  />

			<table>
     
      <tr>
        
      </tr>
      <c:forEach var="group" items="${groupList}" varStatus="counter">
      <tr>
        <td width="131">${group.name}:</td>
        <td width="308">*&nbsp;
        <td>
        <form  method="post" action="RhqUploadServlet" enctype="multipart/form-data">
        <input type="hidden" name="id_template" value="${template.template.id}" />
        <input type="hidden" name="id_sla_prediction" value="${id_sla_prediction}" />
        <input type="hidden" name="id_group" value="${group.id_sla_chart_group}" />
        <input type="file" size="0" name="file"  />
                
        <input name="submit" type="submit" value="Upload" />
        </form>
       </td>
       
      </tr>
      </c:forEach>
       
      
    </table>
            <input type="hidden" name="id_sla_prediction" value="${id_sla_prediction}" />
			<input type="hidden" name="newRhq" value="1" />
			
			
</div>
 </c:if>
