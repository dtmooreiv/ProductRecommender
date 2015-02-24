function getRecommendations() {
	var siteId = document.getElementById("siteIdInput").value;
	var contactId = document.getElementById("contactIdInput").value;
	var count = document.getElementById("countInput").value;
	//We need to validate that these are all numbers

	var response = httpGet("/api/recommend/" + siteId + "/" + contactId + "?count=" + count);
	var results = JSON.parse(response).productIds;
	var orderedList = document.getElementById("results");
	
	//Remove all previous results.
	var index;
	var length = orderedList.children.length;
	for(index = 0; index < length; index++) {
		orderedList.children[0].parentNode.removeChild(orderedList.children[0]);
	}

	//Add a list item containing the productId and score to the results ordered list
	results.forEach(function(_result) {
		var resultElement = document.createElement("li");
		var result = JSON.parse(_result);
		var resultText = document.createTextNode(result.productId + " " + result.score);
		resultElement.appendChild(resultText);
		orderedList.appendChild(resultElement);	
	});
}

//This function sends a GET request to theUrl and returns the response text of the GET request.
function httpGet(theUrl) {
    var xmlHttp = null;
    xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false );
    xmlHttp.send( null );
    return xmlHttp.responseText;
}
