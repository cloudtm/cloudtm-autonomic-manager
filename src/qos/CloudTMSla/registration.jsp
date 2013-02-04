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

    <!-- Promo -->
    <div id="col-top"></div>
    <div class="box" id="col">
    
        <div id="ribbon"></div> <!-- /ribbon (design/ribbon.gif) -->
        
        <!-- Screenshot in browser (replace tmp/browser.gif) -->
        <div id="col-browser"></div> 
        
        <div id="col-text">

            <h2 id="slogan"><span>Registration</span></h2>
            
           <form  method="post" name="registration" action="UserServlet">
    <table width="474" border="0" cellpadding="5" cellspacing="5">
      <tr>
        <td width="131">Company Name</td>
        <td width="308">*&nbsp;
        <input name="company_name" type="text" size="30" maxlength="30" /></td>
      </tr>
      <tr>
        <td>User Name:</td>
        <td>*&nbsp;
        <input name="user_name" type="text" size="30" maxlength="30" /></td>
      </tr>
      <tr>
        <td>Password:</td>
        <td>*&nbsp;
        <input type="password" name="password"/></td>
      </tr>
      <tr>
        <td>First name:</td>
        <td>*&nbsp;
        <input name="first_name" type="text" size="30" maxlength="30" /></td>
      </tr>
      <tr>
        <td>Last Name:</td>
        <td>*&nbsp;
        <input name="last_name" type="text" size="30" maxlength="30" /></td>
      </tr>
      <tr>
        <td>Telephone:</td>
        <td>*&nbsp;
        <input name="telephone" type="text" size="30" maxlength="30" /></td>
      </tr>
      <tr>
        <td>Email:</td>
        <td>*&nbsp;
        <input name="email" type="text" size="30" maxlength="30" /></td>
      </tr>
      
    </table>
    <p id="button">
<input name="submit" type="submit" value="Confirm" />&nbsp;&nbsp;
<input name="reset" type="reset" value="Rest From" /></p>
  </form>
            
        
        </div> <!-- /col-text -->
    
    </div> <!-- /col -->
    <div id="col-bottom"></div>
    
    <hr class="noscreen">
   
    <hr class="noscreen">




<%@ include file="footer.jsp" %>
