function getRecommendations() {
    //Get user input for desired recommendations
	var siteId = document.getElementById("siteIdInput").value;
	var contactId = document.getElementById("contactIdInput").value;
	var count = document.getElementById("countInput").value;
	//We need to validate that these are all numbers

    //Get the recommendations based on user input
	var response = httpGet("/api/recommend/" + siteId + "/" + contactId + "?count=" + count);

	//Get and Remove all previous results
	var orderedList = document.getElementById("results");
	clearRecommendationTable(orderedList);

    //Add a list item containing the productId and score to the results ordered list for display
    createRecommendationTable(response, orderedList);
}

//This function builds a table for display
function createRecommendationTable(getRequestResponse, tableElement) {
    var recommendations = JSON.parse(getRequestResponse);
    for (key in recommendations) {
        var nameElement = document.createElement("li");
        var nameText = document.createTextNode("ContactId: " + key);
        nameElement.appendChild(nameText);
        nameElement.setAttribute("type","circle");
        nameElement.setAttribute("value","0");
        tableElement.appendChild(nameElement);
        for(var i = 0; i < recommendations[key].length; i++) {
            var resultElement = document.createElement("li");
            var resultText = document.createTextNode(recommendations[key][i].productId + " " + recommendations[key][i].score);
            resultElement.appendChild(resultText);
            tableElement.appendChild(resultElement);
        }
    }
}

//This function removes all elements from the display table
function clearRecommendationTable(tableToClear) {
    var length = tableToClear.children.length;
    for(i = 0; i < length; i++) {
    	tableToClear.children[0].parentNode.removeChild(tableToClear.children[0]);
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
