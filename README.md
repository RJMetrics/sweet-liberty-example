# Sweet Liberty Example

This is a simple example project meant to demonstrate how to use Sweet Liberty [(github repo)](https://github.com/RJMetrics/sweet-liberty). Here's a [blog post](http://rjmetrics.com/##############) about the library.

This application exposes a RESTful interface that can be used to perform CRUD operations on *dog* records contained in an in-memory database. This example can be used as a template for more sophicated projects built using Sweet Liberty.

##### Try it out

Drop some knowledge on your terminal:

```
$ git clone https://github.com/RJMetrics/sweet-liberty-example.git
$ cd Sweet-Liberty-example
$ lein ring server-headless
```

Once the server has started successfully, the terminal should show:

`Started server on port 3000`

From another terminal, type:

`$ curl http://localhost:3000/dogs`

You should see a response like:
```json
[{"breed":"poodle","name":"Fido","id":1},{"breed":"corgi","name":"Lacy","id":2},{"breed":"chihuahua","name":"Rex","id":3},{"breed":"dalmation","name":"Spot","id":4},{"breed":"chihuahua","name":"Taco","id":5},{"breed":"corgi","name":"Brody","id":6}]
```

You just let the dogs out.
