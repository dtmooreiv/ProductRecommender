function getRecommendations() {
    //Get user input for desired recommendations
	var siteId = document.getElementById("siteIdInput").value;
	var contactId = document.getElementById("contactIdInput").value;
	var count = document.getElementById("countInput").value;
	//We need to validate that these are all numbers

    //Get the recommendations based on user input
	var response = httpGet("/api/recommend/" + siteId + "/" + contactId + "?count=" + count);
	var recommendations = JSON.parse(response);

	//Get and Remove all previous results
	var orderedList = document.getElementById("results");
	var length = orderedList.children.length;
	for(i = 0; i < length; i++) {
		orderedList.children[0].parentNode.removeChild(orderedList.children[0]);
	}

    //Add a list item containing the productId and score to the results ordered list for display
    for(var i = 0; i < recommendations.length; i++) {
        var resultElement = document.createElement("li");
        var resultText = document.createTextNode(recommendations[i].productId + " " + recommendations[i].score);
        resultElement.appendChild(resultText);
        orderedList.appendChild(resultElement);
    }
}

//This function sends a GET request to theUrl and returns the response text of the GET request.
function httpGet(theUrl) {
    var xmlHttp = null;
    xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", theUrl, false );
    xmlHttp.send( null );
    return xmlHttp.responseText;
}
