# auction
REST API for auction bid tracker

## Requirements

Build part of a simple online auction system which will allow users to bid on items for sale.

Provide a bid tracker interface and concrete implementation with the following functionality:

- Record a user's bid on an item
- Get the current winning bid for an item
- Get all the bids for an item
- Get all the items on which a user has bid

## Build
```sh
$ git clone https://github.com/janesg/auction.git
$ cd auction/auction-service
$ mvn clean install
```

## Run
```sh
$ cd target
$ java -jar auction-1.0.0-SNAPSHOT.jar
```

## Manual Test

Open the Swagger UI by entering the following address in your browser:

- http://localhost:8080/swagger-ui.html

To call a particular REST endpoint:

- click the relevant button on the left to expand the options for the endpoint
- click the 'Try it out' button on the right
- enter the required/optional input for your test scenario
- click the large blue 'Execute' button
- view the response in the area below the 'Execute' button    