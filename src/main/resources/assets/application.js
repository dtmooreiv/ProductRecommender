function changeText() {
	var siteId = document.getElementById("siteIdInput").value;
	var contactId = document.getElementById("contactIdInput").value;
	var count = document.getElementById("countInput").value;
	console.log("Site Id: " + siteId + " Contact Id: " + contactId + " count: " + count);
	console.log(httpGet("http://localhost:8080/recommend/9621/69750106/"));
}

function httpGet(theUrl)
{
    var xmlHttp = null;
    xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false );
    xmlHttp.send( null );
    return xmlHttp.responseText;
}
