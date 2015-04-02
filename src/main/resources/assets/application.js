function setContact() {

    document.getElementById("typeLabel").innerHTML = "Contact ID";

}

function setProd(){

    document.getElementById("typeLabel").innerHTML = "Product ID";

}

function getRecommendations() {
    //Get user input for desired recommendations
	var siteId = document.getElementById("siteIdInput").value;
	var contactId = document.getElementById("contactIdInput").value;
	var count = document.getElementById("countInput").value;
	//We need to validate that these are all numbers

    //Get the recommendations based on user input
    var response;
    if (document.getElementById("Radio1").checked == "checked"){

	    response = httpGet("/api/recommend/" + siteId + "/" + contactId + "?count=" + count);

    }
    else {

        response = httpGet("/api/product-recommend/" + siteId + "/" + contactId + "?count=" + count);

    }
	//Get and Remove all previous results
	var resultsTable = document.getElementById("results");
    clearRecommendationTable(resultsTable);

    //Add a list item containing the productId and score to the results ordered list for display
    createRecommendationTable(response, resultsTable);
}

//This function builds a table for display
function createRecommendationTable(getRequestResponse, tableElement) {
    var recommendations = JSON.parse(getRequestResponse);
    for (key in recommendations) {

        var titleContainer = document.createElement("div");
        titleContainer.className = "container";
        if (document.getElementById("Radio1").checked == "checked"){
            titleContainer.innerHTML = "<br><h1> Recommendations for Contact ID: " + key + "</h1><br><br>";
        }
        else {
            titleContainer.innerHTML = "<br><h1> Recommendations for Product ID: " + key + "</h1><br><br>";
        }
        titleContainer.align = "center";
        tableElement.appendChild(titleContainer);

        var titleRow = document.createElement("div");
        titleRow.className = "row";

        var prodImageCell = document.createElement("div");
        prodImageCell.className = "col-md-2";
        prodImageCell.appendChild(document.createTextNode("Image"));

        var prodTitleCell = document.createElement("div");
        prodTitleCell.className = "col-md-2";
        prodTitleCell.appendChild(document.createTextNode("Title"));

        var prodIDCell = document.createElement("div");
        prodIDCell.className = "col-md-2";
        prodIDCell.appendChild(document.createTextNode("Product ID"));

        var prodScoreCell = document.createElement("div");
        prodScoreCell.className = "col-md-2";
        prodScoreCell.appendChild(document.createTextNode("Score"));

        var prodCatCell = document.createElement("div");
        prodCatCell.className = "col-md-2";
        prodCatCell.appendChild(document.createTextNode("Category"));

        var prodDescCell = document.createElement("div");
        prodDescCell.className = "col-md-2";
        prodDescCell.appendChild(document.createTextNode("Description"));

        titleRow.appendChild(prodImageCell);
        titleRow.appendChild(prodTitleCell);
        titleRow.appendChild(prodIDCell);
        titleRow.appendChild(prodScoreCell);
        titleRow.appendChild(prodCatCell);
        titleRow.appendChild(prodDescCell);

        tableElement.appendChild(titleRow);
        tableElement.appendChild(document.createElement("br"));
        tableElement.appendChild(document.createElement("br"));
        tableElement.appendChild(document.createElement("br"));



        for (var i = 0; i < recommendations[key].length; i++){

            var resultRow = document.createElement("div");
            resultRow.className = "row";

            var imgCol = document.createElement("div");
            imgCol.className = "col-md-2";
            var prodLink = document.createElement("a");
            prodLink.href = recommendations[key][i].productUrl;
            var prodImg = document.createElement("img");
            prodImg.className = "img-responsive";
            prodImg.src = recommendations[key][i].imageUrl;
            prodLink.appendChild(prodImg);
            imgCol.appendChild(prodLink);
            //imgCol.innerHTML = "<a href=\"" + recommendations[key][i].productUrl + "><img src=\"" + recommendations[key][i].imageUrl + "></a>";

            var titleCol = document.createElement("div");
            titleCol.className = "col-md-2";
            titleCol.appendChild(document.createTextNode(recommendations[key][i].title));

            var prodIDCol = document.createElement("div");
            prodIDCol.className = "col-md-2";
            prodIDCol.appendChild(document.createTextNode(recommendations[key][i].productId));

            var prodScoreCol = document.createElement("div");
            prodScoreCol.className = "col-md-2";
            prodScoreCol.appendChild(document.createTextNode(recommendations[key][i].score));

            var catCol = document.createElement("div");
            catCol.className = "col-md-2";
            catCol.appendChild(document.createTextNode(recommendations[key][i].category));

            var descCol = document.createElement("div");
            descCol.className = "col-md-2";
            descCol.appendChild(document.createTextNode(recommendations[key][i].description));

            resultRow.appendChild(imgCol);
            resultRow.appendChild(titleCol);
            resultRow.appendChild(prodIDCol);
            resultRow.appendChild(prodScoreCol);
            resultRow.appendChild(catCol);
            resultRow.appendChild(descCol);

            tableElement.appendChild(resultRow);

            tableElement.appendChild(document.createElement("br"));
            tableElement.appendChild(document.createElement("br"));



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
