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
        
        <div id="col-text">

            <h2 id="slogan"><span>Submit SLA Request</span></h2>
           
            <table id="box-table-c">
     <thead>
     <tr>
        <th scope="col"><a href="javascript:addElement();" > Add Transactional Class</a></th>
        <th scope="col"><a href="javascript:removeElement();" >Remove Transactional Class</a></th>
        
      </tr> 
     </thead>
     </table>
    </p>
       
           <form  method="post" class="template" id="template" name="template" action="TemplateServlet" >
    <table id="box-table-a">
     <div id="content">
      <tr>
        <td width="131">Transactional Class Name *&nbsp;</td>
        <td width="308">=
        <input class="required" name="class_name0" type="text" size="30" maxlength="30" /></td>
      </tr>
      
      <tr>
        <td><input type="checkbox" id="cbthroughput0" checked="checked"  onClick="toggle('cbthroughput0', 'throughput0')" /> Throughput *&nbsp;</td>
        <td>=
        <input id="throughput0" class="required" name="throughput0" number="true" type="text" size="10" maxlength="30" /><strong> tx/sec</strong></td>
      </tr>
      <tr>
        <td><input type="checkbox" id="cbpercentile_resptime0" checked="checked"  onClick="toggle('cbpercentile_resptime0', 'percentile_resptime0')"/> Response time *&nbsp;</td>
        <td>=
        <input id="percentile_resptime0" class="required" number="true" name="percentile_resptime0" type="text" size="10" maxlength="30" /><strong> ms</strong>
        <select name="percentile_value0"><option selected="selected">80</option><c:forEach var="i" begin="81" end="99">
            <option value="${i}">${i}</option>
          </c:forEach></select> Percentile</td>
        </td>
      </tr>
      <tr>
        <td><input type="checkbox" id="cbabort_rate0" checked="checked" onClick="toggle('cbabort_rate0', 'abort_rate0')" /> Maximum Admissible Abort Rate *&nbsp;</td>
        <td>=
        <input id="abort_rate0" class="required" name="abort_rate0" number="true" type="text" size="10" maxlength="30" /> %</td>
      </tr>
       <tr>
        <td>Observation Period *&nbsp;</td>
        <td>=
        <input class="required" number="true" name="observation_period0" type="text" size="10" maxlength="30" /><strong> ms </strong></td>
      </tr>
     
    </div> 
      
    </table>
    <script type="text/javascript">
    $(document).ready(function(){
        $('.input_control').attr('checked', true);
        $('.input_control').click(function(){
            if($('input[name='+ $(this).attr('value')+']').attr('disabled') == false){
                $('input[name='+ $(this).attr('value')+']').attr('disabled', true);
                $('input[name='+ $(this).attr('value')+']').css('background-color', '#C2C2C2');
            }else{
                $('input[name='+ $(this).attr('value')+']').attr('disabled', false); 
                $('input[name='+ $(this).attr('value')+']').css('background-color', '#FFFFFF');

            }
        });
    });
</script>
    <p id="button">
<input name="submit" type="submit" value="Confirm" />&nbsp;&nbsp;
<input name="reset" type="reset" value="Reset From" /></p>
 
            
          

        </div> <!-- /col-text -->
     </form>
    </div> <!-- /col -->
    <div id="col-bottom"></div>
    
    <hr class="noscreen">
    
    

    <hr class="noscreen">

 <script type="text/javascript">
$(document).ready(function()
{
	$('.template').submit(function ()
	{
		var i,
		    validate_fields = ['.required'], // fields to validate
		    invalid_fields = []; // store of empty fields
		for (i in validate_fields)
		{
			// check if field is empty
			$(validate_fields[i]).each(function(){
				if ($.trim($(this).val()).length == 0)
			{
				invalid_fields.push(validate_fields[i]);
			}
		});
		}
		if (invalid_fields.length)
		{
			if(invalid_fields.length == 1)
			alert('There is ' + invalid_fields.length + ' empty field.');
			else
			alert('There are ' + invalid_fields.length + ' empty fields.');
			return false; // cancel the form submit
		}
		return true;
	});
});
</script>


<%@ include file="footer.jsp" %>
