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

function makeButton(text, action) {
    ret = document.createElement("INPUT")
    ret.type = "button"
    ret.setAttribute('class', 'btn btn-cancel')
    ret.value = text
    ret.onclick = action
    return ret
}

function makePara(text, id = null) {
    ret = document.createElement("P")
    ret.innerHTML = text
    ret.id = id
    return ret
}

function deleteProduct(id) {
    makeJsonRequest('/controller/sms/deleteProduct', {'id': id},
                    (json, error) => {
                        if(error != null) {
                            console.log(error)
                        } else {
                            location.reload()
                        }
                    })
}

function addProduct() {
    params = {
        "name": document.getElementById("name").value,
        "price": document.getElementById("price").value,
    }

    makeJsonRequest('/controller/sms/addProduct', params,
                    (json, error) => {
                        if(error != null) {
                            console.log(error)
                        } else {
                            location.reload()
                        }
                    })
}

var customerCount = 0

function filterCustomers() {
    params = {}

    if(document.getElementById("f_name").value != "")
        params["name"] = document.getElementById("f_name").value

    if(document.getElementById("f_product").value != "")
        params["product"] = document.getElementById("f_product").value

    makeJsonRequest('/controller/sms/customerSearch', params, (json, error) => {
        if(error == null) {
            table = document.getElementById('customers_table')

            while(customerCount > 0) {
                table.deleteRow(-1)
                customerCount--;
            }

            for(var i = 0; i < json.length; i++) {
                addRow(table, [json[i].personId, json[i].name, json[i].billingAddress])
                customerCount++;
            }
        }
    })
}

var sumbitOrder = function() {}

window.onload = function() {
    our_params = getParams(window.location.href)

    filterCustomers()

    orders = document.getElementById("orders")

    makeJsonRequest('/controller/common/listProducts', {}, (json, error) => {
        if(error == null) {
            table = document.getElementById('listing_table')

            for(var i = 0; i < json.length; i++) {
                var id = json[i].itemId
                addRow(table, [
                    json[i].itemId, json[i].name, json[i].price.toFixed(2),
                    makeButton("Delete",
                               function() { deleteProduct(id); })])
            }
        }
    })

    makeJsonRequest('/controller/common/listDiscounts', {}, (json, error) => {
        if(error == null) {
            table = document.getElementById('discounts_table')

            for(var i = 0; i < json.length; i++) {
                addRow(table, [makeLink(json[i].discountId, "?discount=" + json[i].discountId),
                               json[i].dateCreated, json[i].exp])
            }
        }
    })

    if(our_params["discount"] != null) {
        orders.style.display = "block"
        document.getElementById("orders_discount_code").innerHTML = our_params["discount"]

        makeJsonRequest('/controller/common/listOrders',
                        {'discountId': our_params['discount'],
                         'withDiscount': ""}, (json, error) => {
            if(error == null) {
                table = document.getElementById('orders_table')

                for(var i = 0; i < json.length; i++) {
                    addRow(table, [
                        json[i].orderId,
                        json[i].itemId,
                        json[i].name,
                        json[i].price.toFixed(2),
                        json[i].priceAdj.toFixed(2),
                        json[i].personId,
                        json[i].customer,
                        json[i].address,
                        json[i].creditId,
                        json[i].cardName,
                        json[i].cardNumber,
                        json[i].cardExpMonth,
                        json[i].cardExpYear,
                        json[i].cardCVV,
                        json[i].cardZIP
                    ])
                }
            }
        })
    }
}
