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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"><html>
<head>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

    <meta content="text/html; charset=utf-8" http-equiv="content-type">
    <meta content="en" http-equiv="content-language">
    <meta content="all,follow" name="robots">

    <meta lang="en" content="All: Your name [www.url.com]; e-mail: info@url.com" name="author">
    <meta lang="en" content="Webdesign: Nuvio [www.nuvio.cz]; e-mail: ahoj@nuvio.cz" name="copyright">

    <meta content="..." name="description">
    <meta content="..." name="keywords">

    <link href="/CloudTMSla/css/reset.css" type="text/css" media="screen,projection" rel="stylesheet">
    <link href="/CloudTMSla/css/boxy.css" type="text/css" media="screen,projection" rel="stylesheet">
    <link href="/CloudTMSla/css/main.css" type="text/css" media="screen,projection" rel="stylesheet">
    <!--[if lte IE 6]><link rel="stylesheet" type="text/css" href="/CloudTMSla/css/main-msie.css" /><![endif]-->
    <link href="/CloudTMSla/css/style.css" type="text/css" media="screen,projection" rel="stylesheet">
    <link href="/CloudTMSla/css/print.css" type="text/css" media="print" rel="stylesheet">

    <title>CloudTm Sla Manager</title>
    
	<script type="text/javascript" src="/CloudTMSla/script/jquery.min.js"></script>
	<script type="text/javascript" src="/CloudTMSla/script/jquery.validate.min.js"></script>
    <script type="text/javascript" src="/CloudTMSla/script/highcharts.src.js"></script>
    <script type="text/javascript" src="/CloudTMSla/script/jquery.boxy.js"></script>
    <script type="text/javascript" src="/CloudTMSla/script/jquery.popupWindow.js"></script>
  <script type="text/javascript">
	function toggle(checkboxID, toggleID) {
     var checkbox = document.getElementById(checkboxID);
     var toggle = document.getElementById(toggleID);
     updateToggle = checkbox.checked ? toggle.disabled=false : toggle.disabled=true;
     updateToggle = checkbox.checked ? toggle.className = "required" : toggle.className = "notrequired";
     

   }
   </script>
   <script type="text/javascript">
  jQuery(function(){
     jQuery("#template").validate();
  });
</script>
    <script type="text/javascript">
    var intTextBox=0;

  //FUNCTION TO ADD TEXT BOX ELEMENT
  function addElement()
  {
  intTextBox = intTextBox + 1;
  var contentID = document.getElementById('content');
  var newTBDiv = document.createElement('div');
  newTBDiv.setAttribute('id','strText'+intTextBox);
  newTBDiv.innerHTML = "<table id=\"box-table-a\">"+
	     "<div id=\"content\">"+
	      "<tr>"+
	        "<td width=\"131\">Transactional Class Name *&nbsp;</td>"+
	        "<td width=\"308\">= "+
	        "<input class=\"required\" name=\"class_name"+intTextBox+"\" type=\"text\" size=\"30\" maxlength=\"30\" /></td>"+
	     "</tr>"+
	     "<tr>"+
	        "<td><input type=\"checkbox\" id=\"cbthroughput"+intTextBox+"\" checked=\"checked\" onClick=\"toggle('cbthroughput"+intTextBox+"', 'throughput"+intTextBox+"')\"  /> Throughput *&nbsp;</td>"+
	       "<td>= "+
	       "<input class=\"required\" number=\"true\" id=\"throughput"+intTextBox+"\" name=\"throughput"+intTextBox+"\" type=\"text\" size=\"10\" maxlength=\"30\" /><strong> tx/sec</strong></td>"+
	    "</tr><tr>"+
	       "<td><input type=\"checkbox\"  id=\"cbpercentile_resptime"+intTextBox+"\" checked=\"checked\" onClick=\"toggle('cbpercentile_resptime"+intTextBox+"', 'percentile_resptime"+intTextBox+"')\" /> Response time *&nbsp;</td>"+
	        "<td>= "+
	       "<input class=\"required\" number=\"true\" id=\"percentile_resptime"+intTextBox+"\" name=\"percentile_resptime"+intTextBox+"\" type=\"text\" size=\"10\" maxlength=\"30\" /><strong> ms </strong>"+
	       "<select name=\"percentile_value"+intTextBox+"\"><option selected=\"selected\">80</option>"+
	       "<c:forEach var="i" begin="81" end="99">"+
	          "<option value=\"${i}\">${i}</option>"+
	        "</c:forEach></select> Percentile</td>"+
	       "</td>"+
	     "</tr>"+
	     "<tr>"+
	       "<td><input type=\"checkbox\" id=\"cbabort_rate"+intTextBox+"\" checked=\"checked\" onClick=\"toggle('cbabort_rate"+intTextBox+"', 'abort_rate"+intTextBox+"')\" /> Maximum Admissible Abort Rate *&nbsp;</td>"+
	       "<td>= "+
	        "<input class=\"required\" number=\"true\" id=\"abort_rate"+intTextBox+"\" name=\"abort_rate"+intTextBox+"\" type=\"text\" size=\"10\" maxlength=\"30\" /> %</td>"+
	      "</tr>"+
	       "<tr>"+
	        "<td>Observation Period *&nbsp;</td>"+
	        "<td>= "+
	        "<input class=\"required\" number=\"true\" name=\"observation_period"+intTextBox+"\" type=\"text\" size=\"10\" maxlength=\"30\" /><strong> ms </strong></td>"+
	      "</tr>"+"</div>"+"</table>";
  contentID.appendChild(newTBDiv);
  }

  //FUNCTION TO REMOVE TEXT BOX ELEMENT
  function removeElement()
  {
  if(intTextBox != 0)
  {
  var contentID = document.getElementById('content');
  contentID.removeChild(document.getElementById('strText'+intTextBox));
  intTextBox = intTextBox-1;
  }
  }
    </script>
    
</head><body onload='boot();'>

<div id="main">

    <!-- Header -->
    <div id="header">

        <h1 id="logo"><a title="[Go to homepage]" href="./"><img width="160" src="http://www.cloudtm.eu/_/rsrc/1288609844892/home/spotlight_logo.png" alt=""></a></h1>
        <hr class="noscreen">

        <!-- Navigation -->
        <div id="nav">
            <a href="#">Homepage</a> <span>|</span>
            <a href="#">About us</a> <span>|</span>
            <a href="#">Support</a> <span>|</span>
            <a href="#">Contact</a>
        </div> <!-- /nav -->

    </div> <!-- /header -->
    
    <!-- Tray -->
    <div id="tray">

        <ul>
            <li><a href="index.jsp">Homepage</a></li> <!-- Active page -->
            <li><a href="sla_template.jsp">Submit SLA Request</a></li>
            <li><a href="ViewTemplateServlet">View SLA Request</a></li>
            <c:if test="${currentSessionUser.level==0}">
            <li><a href="admin">Administrator Panel</a></li>
            </c:if>
            <li><a href="#"></a></li>
            <li><a href="#"></a></li>
        </ul>
        
        <!-- Search -->
        <div class="box" id="search">
            <form method="get" action="#">
                <div class="box">
                    
                    
                </div>
            </form>
        </div> <!-- /search -->

    <hr class="noscreen">
    </div> <!-- /tray -->
