function formatParams(params) {
    return "?" + Object
        .keys(params)
        .map(function(key) {
            return key+"="+encodeURIComponent(params[key])
        })
        .join("&")
}

function makeJSONRequest(uri, params, callback) {
    let request = new XMLHttpRequest();
    let url = uri + formatParams(params)

    request.open("GET", url)
    request.send();
    request.onload = () => {
        if(request.status == 200) {
            callback(JSON.parse(request.response),null)
        } else {
            console.log(request)
            callback(null, {
                status: request.status, msg: request.statusText
            })
        }
    }
}

function makeRequest() {
    uri = "common/listProducts"
    params = {

    }
    makeJSONRequest(uri, params, (json,error) => {
        if(error == null) {
            makeTable(json);
        } else {
            console.log(error);
        }
    })
}

function makeTable(json) {
    console.log(json);

    var table = document.getElementById('item-table').lastChild;
    json.forEach(element => {

        var tr = document.createElement('tr');
        var name = tr.insertCell(0);
        name.innerHTML = element['name'];
    
        var price = tr.insertCell();
        price.innerHTML = element['price'];
    
        var button = tr.insertCell();
        button.innerHTML = "<a class='btn' href='manage.html'>Place Order</a>"
        table.appendChild(tr);
    });
}