<script>
function openView(viewname) {
		// Declare all variables
		var i, tabcontent, tablinks;
		
		// Get all elements with class="tabcontent" and hide them
		tabcontent = document.getElementsByClassName("tabcontent");
		for (i = 0; i < tabcontent.length; i++) {
			tabcontent[i].style.display = "none";
		}

		// Get all elements with class="tablinks" and remove the class "active"
		tablinks = document.getElementsByClassName("tablinks");
		for (i = 0; i < tablinks.length; i++) {
			tablinks[i].className = tablinks[i].className.replace(" active", "");
		}
	
		// Show the current tab, and add an "active" class to the button that opened the tab
		document.getElementById(viewname).style.display = "block";
		document.getElementById(viewname+"link").className += " active";
		
	}	

	$( document ).ready(function() {
		document.getElementById("graphviewlink").className += " active";
		//load table data in the background;
		var dataTablePath = "<c:url value='/${PAGEPATH}/'/>" + encodeURIComponent('${RESOURCEURI.toString()}');
		dataTablePath = dataTablePath + "/tabledata";
		
		var url = window.location.href;
		if (url.indexOf("/resources/")>0){ //if we are on resource page, make sure is loads resource view!
			dataTablePath = dataTablePath + "?resview=1";
		}
		$.get(dataTablePath, function( data ) {
			document.getElementById("tableview").innerHTML = data;
			});
	});

</script>