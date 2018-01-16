/**
 * Search page javascript
 */
$(document).ready(function () {
	
	$('.agentFacet:gt(4)').hide().last().after(
		$('<a />').attr('href','#').attr('class','showMore').text('+ show more').click(function(){
			var a = this;
			$('.agentFacet:not(:visible):lt(4)').fadeIn(function(){
			 if ($('.agentFacet:not(:visible)').length == 0) $(a).remove();   
			}); return false;
		})
	);		

});

$( function() {
	
    $(".dateInput").datepicker( {
    	maxDate: new Date(), 
		dateFormat: "yy-mm-dd",
		onSelect: function(date) {
			document.searchForm[this.id].value=date;
			document.searchForm["page"].value=0;	
			$(':input[value=""]').attr('disabled', true);
			document.searchForm.submit();
			return false;		
        }
	});
	
	$(".filter").click(function(){
		applyFilter($(this), false);
		return false; // avoid parents divs if you have nested divs
	});
	
	$(".clearFilter").click(function(){
		applyFilter($(this), true);
		return false; // avoid parents divs if you have nested divs
	});
	
	$('[name="searchForm"]').submit(function(){
		document.searchForm["page"].value=0;	
		$(':input[value=""]').attr('disabled', true);
	});
		
});

function applyFilter(divObj, blClearFields) {
	var flds = divObj.data("fields").split(",");
	for (var i=0;i<flds.length; i++) {
		if (blClearFields) {
			document.searchForm[flds[i]].value="";		
		} else {
			document.searchForm[flds[i]].value=divObj.data(flds[i]);
		}
	}
	if (flds!="page") {
		document.searchForm["page"].value=0;	
	}
	$(':input[value=""]').attr('disabled', true);
	document.searchForm.submit();
}
