var selectAndCopy = function() {
  // Select text
  var cutTextarea = document.querySelector('#copy_container');
  cutTextarea.select();
  // Execute copy
  var successful = document.execCommand('copy');
  var msg = successful ? 'Your key was copied to the clipboard' : 'Your key could not be copied to the clipboard';
  cutTextarea.value="";
  alert(msg);
};

$(document).on("click", '.clipboard', function() {
  var url = $(this).data("url");
  var xhr = new XMLHttpRequest();
  xhr.open('get', url, false);
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        // Set text
        var textarea = document.querySelector('#copy_container');
        textarea.value = xhr.responseText;
        selectAndCopy();
      }
    }
  };
  xhr.send();
});
