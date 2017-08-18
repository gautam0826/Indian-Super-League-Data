# Indian Super League Data

Indian Super League Data has been created to store data scraped both from the fantasy game and the match centre. 

The scraper is written in Java and makes use of two external libraries: [JSOUP](https://github.com/stleary/JSON-java) and [org.json](https://github.com/stleary/JSON-java). The data is stored in JSON file format. 

While the detailed match data is already indented, the fantasy match data is compact so a tool like [JSON Editor Online](http://jsoneditoronline.org/) is recommended. While the detailed data encapsulates almost all the data in the fantasy data, it does not have fantasy points so some data wrangling could be done to create more features using the detailed data if you wanted to build a fantasy model. Another interesting project would be looking through the shot events in the detailed match file to build an [expected goals model](http://11tegen11.net/2014/02/10/what-is-expg/).