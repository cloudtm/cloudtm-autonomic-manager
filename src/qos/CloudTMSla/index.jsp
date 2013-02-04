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
<%@ include file="header.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
    <!-- Promo -->
    <div id="col-top"></div>
    <div class="box" id="col">
    
        <div id="ribbon"></div> <!-- /ribbon (design/ribbon.gif) -->
        
        <!-- Screenshot in browser (replace tmp/browser.gif) -->
        <div id="col-browser"></div> 
        <c:if test="${currentSessionUser==null}">
        <div id="col-text">

            <h2 id="slogan"><span>Login to create a SLA</span></h2>

		<form name="actionForm" action="LoginServlet" method="GET">
			<table>
			<tr>
					<td>Administrator</td>
					<td> - username admin, password admin</td>
				</tr>
				<tr>
					<td>Username:</td>
					<td><input type="text" name="uname" class="input"/></td>
				</tr>
				<tr>
					<td>Password:</td>
					<td><input type="password" name="password" class="input"/></td>
				</tr>
				<tr>
					<td colspan="2" align="center"><input type="submit"
						value="submit"></td>
				</tr>
			</table>
		</form>
         <a href="${pageContext.request.contextPath}/registration.jsp">Register</a>
		

        </div> <!-- /col-text -->
    </c:if>
            <c:if test="${currentSessionUser!=null}">
        <div id="col-text">

            <h2 id="slogan"><span>Hello, ${currentSessionUser.firstName}</span></h2>

		<ul class="ul-01">
                    <li> <a href="${pageContext.request.contextPath}/sla_template.jsp">Submit SLA Request</a></li>
                    <li> <a href="${pageContext.request.contextPath}/ViewTemplateServlet">View SLA Request</a></li>
                    <c:if test="${currentSessionUser.level==0}">
                    <li> <a href="${pageContext.request.contextPath}/admin">Administrator Panel</a></li> </c:if>
                    <li> <a href="${pageContext.request.contextPath}/logout">Logout</a></li>
                </ul>

		

        </div> <!-- /col-text -->
    </c:if>
    </div> <!-- /col -->
    <div id="col-bottom"></div>
    
    <hr class="noscreen">
    


    <hr class="noscreen">




<%@ include file="footer.jsp" %>
