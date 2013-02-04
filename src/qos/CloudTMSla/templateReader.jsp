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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"><html>
<head>

    <meta content="text/html; charset=utf-8" http-equiv="content-type">
    <meta content="en" http-equiv="content-language">
    <meta content="all,follow" name="robots">

    

    
    <link href="/CloudTMSla/css/main.css" type="text/css" media="screen,projection" rel="stylesheet">
   

    <title>Template Data</title>
</head>
<div style='background-color: white; height: 400px; width: 800px;  color: black; font-size: 12px'>



<br>
 <h2 id="slogan"><span>Template Data</span></h2>
<table id="box-table-a">

<c:forEach var="listTransactional" items="${listTransactional}" varStatus="counter">
 <tr>
        <td width="131">Transactional Class Name *&nbsp;</td>
        <td width="308">= ${listTransactional.name}
        </td>
      </tr>
      
      <tr>
        <td>Throughput *&nbsp;</td>
        <td>= ${listTransactional.throughput}
        <strong> tx/sec</strong></td>
      </tr>
      <tr>
        <td>Response time *&nbsp;</td>
        <td>= ${listTransactional.response_time}
        <strong> ms </strong>
         ${listTransactional.response_time_percentile} Percentile</td>
        </td>
      </tr>
      <tr>
        <td>Maximum Admissible Abort Rate *&nbsp;</td>
        <td>= ${listTransactional.abort_rate}
         %</td>
      </tr>
       <tr>
        <td>Observation Period *&nbsp;</td>
        <td>= ${listTransactional.period}
       <strong> ms </strong></td>
      </tr>
     


  </c:forEach>










</table>


</div>




