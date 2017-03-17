<script>
var LIMIT = 8;
var offset = 0;


function showNext() {
	hideAll();
	offset = offset+LIMIT;
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(offset, offset+LIMIT);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'shown';
	  }
	switchButtons();
}

function showPrevious() {
	hideAll();
	offset = offset-LIMIT;
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(offset, offset+LIMIT);
	for (var i=0; i < listData.length; i++)	{
		listData[i].className  = 'shown';
	}
	switchButtons();
}

function hideAll() {
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.hiddenproperty)')).slice(-LIMIT);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'hiddenproperty';
	}
}


function switchButtons() {
	var listLength = Array.prototype.slice.call(document.querySelectorAll('#properties li')).length;
	var currLastRow = offset+LIMIT;
		
	if(currLastRow>=listLength) {
		document.getElementsByClassName('next')[0].style.display = 'none';
	}
	else	{
		document.getElementsByClassName('next')[0].style.display = 'block';
	}
		
	if(currLastRow==LIMIT){
		document.getElementsByClassName('previous')[0].style.display = 'none';
	}
	else {
		document.getElementsByClassName('previous')[0].style.display = 'block';
	}
}

function loadFirstProps() {
	var listData = Array.prototype.slice.call(document.querySelectorAll('#properties li:not(.shown)')).slice(offset, offset+LIMIT);
	for (var i=0; i < listData.length; i++)  {
		listData[i].className  = 'shown';
	  }
	switchButtons();
}
</script>