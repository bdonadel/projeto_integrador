# Projeto_Integrador
API REST desenvolvida pelo grupo Beta Campers para o Projeto Integrador feito durante o IT Bootcamp Backend Java (wave 6). 

## Autores
<a href="https://github.com/vfreitasmeli">
  <img src="https://avatars.githubusercontent.com/u/107959338?s=50&v=4" style="width: 50px">
</a>
<a href="https://github.com/brunavottri">
  <img src="https://avatars.githubusercontent.com/u/108009877?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/pealmeida-meli">
  <img src="https://avatars.githubusercontent.com/u/108008922?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/thiagosordiMELI">
  <img src="https://avatars.githubusercontent.com/u/108008559?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/bdonadel">
  <img src="https://avatars.githubusercontent.com/u/108012641?s=120&v=4" style="width: 50px">
</a>
<a href="https://github.com/felipeticiani-meli">
  <img src="https://avatars.githubusercontent.com/u/108010964?s=120&v=4" style="width: 50px">
</a>

# Sumário

- <a href="https://app.diagrams.net/#G1X_05jbEF7Yt2yFOZ2y3OfKW_KCPjm5MC">Diagrama UML </a>
- [DER](SQL%20Model.png)
- [Postman collection](Projeto%20integrador.postman_collection.json)
- [Funcionalidades](#funcionalidades)
  - [Inbound](#inboundOrder)
  - [Purchase](#purchase)

# Funcionalidades

## Inbound <br name="inboundOrder">

`POST /api/v1/fresh-products/inboundorder`<br>
Cria uma nova entrada do pedido.
<pre><code><b>Payload Example:</b>
{
  "sectionCode": 1,
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31T00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16T22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 }
 
 <b>Response:</b>
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31 00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16 22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 
 </code></pre>
 
`PUT /api/v1/fresh-products/inboundorder?orderNumber={orderNumber}`<br>
Atualiza entrada do pedido.
<pre><code><b>Payload Example:</b>
{
  "sectionCode": 1,
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31T00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16T22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 }
 
 <b>Response:</b>
  "batchStock": [
          {
            "productId": 1,
            "currentTemperature":20,
            "minimumTemperature": 15,
            "initialQuantity": 10,
            "currentQuantity": 7,
            "manufacturingDate": "2021-12-31",
            "manufacturingTime": "2021-12-31 00:00:00",
            "dueDate": "2022-12-31"
            "productPrice": 22.50
         },
          {
            "productId": 2,
            "currentTemperature":19,
            "minimumTemperature": 16,
            "initialQuantity": 20,
            "currentQuantity": 13,
            "manufacturingDate": "2022-06-16",
            "manufacturingTime": "2022-06-16 22:16:23",
            "dueDate": "2022-07-01",
            "productPrice": 7.90
         },
   ]
 
 </code></pre>
 - Será validado se:<br>
  - Todos os campos não estão vazios
  - O código do setor, id do produto, e preço do produto são positivos
  - Se a lista "batchStock" não está vazia
  - Se a data de fabricação e a data de vencimento estão no formato dd-MM-yyyy
  - Se a hora de fabricação está no formato dd-MM-yyyy HH:mm:ss
  - Se a data e hora de fabricação e a data de vencimento são posteriores a data de criação

 
`GET /api/v1/fresh-products/warehouse?productId={productId}`<br>
Retorna a quantidade total dos lotes de um produto por armazém.
<pre><code><b>Response Example:</b>
{
    "productId": 2,
    "warehouses": [
        {
            "warehouseCode": 1,
            "totalQuantity": 1037
        }
    ]
}
</code></pre>
 
`GET /api/v1/fresh-products/list?productId={productId}&orderBy={order}`<br>
Retorna todos os lotes de um determinado produto.
O parâmetro 'orderBy' é opcional e pode ser: L: batchNumber, Q: currentQuantity, V: dueDate
<pre><code><b>Response Example:</b>
{
    "productId": 2,
    "batchStock": [
        {
            "batchNumber": 2,
            "currentQuantity": 980,
            "dueDate": "2024-08-20",
            "section": {
                "sectionCode": 3,
                "warehouseCode": 1
            }
        },
        {
            "batchNumber": 3,
            "currentQuantity": 57,
            "dueDate": "2023-06-20",
            "section": {
                "sectionCode": 3,
                "warehouseCode": 1
            }
        }
    ]
}
</code></pre>

`GET /api/v1/fresh-products/due-date?sectionCode={sectionCode}&numberOfDays={days_to_expire}`<br>
Retorna os lotes de uma seção cuja validade expira de acordo com o número de dias informado. 
<pre><code><b>Response Example:</b>
[
    {
        "batchNumber": 6,
        "productId": 1,
        "productName": "Iogurte",
        "productCategory": "CHILLED",
        "dueDate": "2022-08-25",
        "currentQuantity": 150
    },
    {
        "batchNumber": 12,
        "productId": 1,
        "productName": "Iogurte",
        "productCategory": "CHILLED",
        "dueDate": "2022-08-25",
        "currentQuantity": 150
    },
    {
        "batchNumber": 34,
        "productId": 5,
        "productName": "Requeijão",
        "productCategory": "CHILLED",
        "dueDate": "2022-09-06",
        "currentQuantity": 79
    }
]
</code></pre>

`GET /api/v1/fresh-products/due-date?category={category}&numberOfDays={days_to_expire}&orderDir={order}`<br>
Retorna os lotes de uma categoria (RF: CHILLED, FS: FRESH, FF: FROZEN) cuja validade expira de acordo com o número de dias informado. Ordenando de forma crescente (ASC) ou decrescente (DESC). 
<pre><code><b>Response Example:</b>
[
    {
        "batchNumber": 6,
        "productId": 1,
        "productName": "Iogurte",
        "productCategory": "CHILLED",
        "dueDate": "2022-08-25",
        "currentQuantity": 150
    },
    {
        "batchNumber": 12,
        "productId": 1,
        "productName": "Iogurte",
        "productCategory": "CHILLED",
        "dueDate": "2022-08-25",
        "currentQuantity": 150
    },
    {
        "batchNumber": 34,
        "productId": 5,
        "productName": "Requeijão",
        "productCategory": "CHILLED",
        "dueDate": "2022-09-06",
        "currentQuantity": 79
    }
]
</code></pre>

## Purchase Order <br name="purchase">

`GET /api/v1/fresh-products`<br>
Retorna todos os lotes disponíveis para compra.
<pre><code><b>Response Example:</b>
[
    {
        "batchNumber": 1,
        "productName": "Iogurte",
        "brand": "Danone",
        "category": "CHILLED",
        "quantity": 1,
        "dueDate": "2024-08-20",
        "productPrice": 5.99
    },
    {
        "batchNumber": 2,
        "productName": "Queijo",
        "brand": "Sadia",
        "category": "CHILLED",
        "quantity": 980,
        "dueDate": "2024-08-20",
        "productPrice": 7.50
    }
]
</code></pre>

`GET /api/v1/fresh-products&category={category}`<br>
Retorna todos os lotes disponíveis para compra em uma determinada categoria (RF: CHILLED, FS: FRESH, FF: FROZEN).
<pre><code><b>Response Example:</b>
[
    {
        "batchNumber": 1,
        "productName": "Iogurte",
        "brand": "Danone",
        "category": "CHILLED",
        "quantity": 1,
        "dueDate": "2024-08-20",
        "productPrice": 5.99
    },
    {
        "batchNumber": 2,
        "productName": "Queijo",
        "brand": "Sadia",
        "category": "CHILLED",
        "quantity": 980,
        "dueDate": "2024-08-20",
        "productPrice": 7.50
    }
]
</code></pre>

`POST /api/v1/fresh-products/orders`<br>
Adiciona um lote ao carrinho do cliente. Retorna o ID do carrinho e o valor total acumulado dele.
<pre><code><b>Payload Example:</b>
{
    "orderStatus": "Opened",
    "batch": {
        "batchNumber": 1,
        "quantity": 2
    }
}

<b>Response:</b>
 
 {
    "purchaseOrderId": 2,
    "totalPrice": 131.88
}
</code></pre>

`GET /api/v1/fresh-products/orders?purchaseOrderId={purchaseOrderId}`<br>
Retorna os produtos adicionados no carrinho do cliente.
<pre><code><b>Response Example:</b>
[
    {
        "batchNumber": 2,
        "productName": "Queijo",
        "brand": "Sadia",
        "category": "CHILLED",
        "quantity": 15,
        "dueDate": "2024-08-20",
        "productPrice": 7.50
    }
]
</code></pre>

`DELETE /api/v1/fresh-products/orders?purchaseOrderId={purchaseOrderId}`<br>
Remove um lote do carrinho do cliente.
<pre><code><b>Payload Example:</b>
{
    "batchNumber": 1
}
</code></pre>

`PUT /api/v1/fresh-products/orders?purchaseOrderId={purchaseOrderId}`<br>
Fecha o carrinho do cliente.
