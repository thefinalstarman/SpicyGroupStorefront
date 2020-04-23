function formatParams(params) {
    return "?" + Object
        .keys(params)
        .map(function(key) {
            return key+"="+encodeURIComponent(params[key])
        })
        .join("&")
}

function makeJsonRequest(uri, params, callback) {
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
    let uri = "/controller/sample_rest/sum";
    params = {
        a: document.getElementById("a").value,
        b: document.getElementById("b").value
    }
    makeJsonRequest(uri, params, (json,error) => {
        if(error == null) {
            document.getElementById('result').innerHTML = "Result = " + json['result']
            document.getElementById('error').innerHTML = ""
        } else {
            document.getElementById('result').innerHTML = ""
            document.getElementById('error').innerHTML = "Error " + error['status'] + ": " + error['msg']
        }
    })
}

function isElement(element) {
    return element instanceof Element || element instanceof HTMLDocument;
}

function getParams(url) {
	  var params = {};
	  var parser = document.createElement('a');
	  parser.href = url;
	  var query = parser.search.substring(1);
	  var vars = query.split('&');
	  for (var i = 0; i < vars.length; i++) {
		    var pair = vars[i].split('=');
		    params[pair[0]] = decodeURIComponent(pair[1]);
	  }
	  return params;
};

function addRow(table, arr) {
    row = table.insertRow(-1)
    for(var i = 0; i < arr.length; i++) {
        cell = row.insertCell(i)

        console.log(typeof arr[i])
        if(!isElement(arr[i])) {
            cell.innerHTML = arr[i]
        } else {
            cell.appendChild(arr[i])
        }
    }
}

function makeLink(text, uri) {
    ret = document.createElement("A")
    ret.href = uri
    ret.innerHTML = text
    return ret
}

function makePara(text, id = null) {
    ret = document.createElement("P")
    ret.innerHTML = text
    ret.id = id
    return ret
}

function makeCheckoutForm() {
    ret = document.createElement("FORM")


}

var sumbitOrder = function() {}

window.onload = function() {
    our_params = getParams(window.location.href)

    listing = document.getElementById('listing')
    checkout = document.getElementById('checkout')
    confirmation = document.getElementById('confirmation')

    makeJsonRequest('/controller/common/listProducts', {}, (json, error) => {
        if(error == null) {
            table = document.getElementById('listing_table')

            for(var i = 0; i < json.length; i++) {
                addRow(table, [makeLink(json[i].name, "?checkout=" + json[i].itemId),
                                 json[i].price])
            }
        }
    })

    if(our_params["checkout"] != null) {
        checkout = document.getElementById('checkout')
        listing.style.display = "none"
        checkout.style.display = "block"
        confirmation.style.display = "none"

        product = {}
        makeJsonRequest('/controller/common/listProducts', {'id': our_params['checkout']}, (json, error) => {
            if(error == null) {
                product = json[0]
                document.getElementById('ch_product').innerHTML += product.name
                document.getElementById('ch_price').innerHTML += product.price
            }
        })

        submitOrder = function() {
            params = {}

            fetch = ["name","address","cardName","cardNumber","monthExp","yearExp","cvv","zip"]

            for(var i = 0; i < fetch.length; i++) {
                params[fetch[i]] = document.getElementById(fetch[i]).value
            }

            if(document.getElementById("discountId").value != "")
                params["discountId"] = document.getElementById("discountId").value

            params["itemId"] = our_params["checkout"]

            makeJsonRequest('/controller/storefront/submitOrder',
                            params, (json, error) => {
                if(error == null) {
                    listing.style.display = "none"
                    checkout.style.display = "none"
                    confirmation.style.display = "block"

                    document.getElementById("c_order").innerHTML += json.order.orderId
                    document.getElementById("c_name").innerHTML += json.person.name
                    document.getElementById("c_address").innerHTML += json.person.billingAddress
                    document.getElementById("c_discount").innerHTML += json.order.discountId
                    document.getElementById('c_product').innerHTML += product.name
                    document.getElementById('c_price').innerHTML += product.price
                }
            })
        }
    }
}
