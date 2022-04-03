# Music Service

###What are used
* Spring Boot - Web server to run the application
* Kotlin - Language to build application
* JDK 11 is used
* Kotlin-Logger - For logging
* Junit Jupiter - For Junit tests
* Json Path resolver - To extract specific data dynamically from json

###How to Build & Run
* Run with
   ```
      mvn spring-boot:run
   ```
  
Alternatively IDE UI also can be used to build and run the application.

Intellij IDE has been used, so it is recommended to use the same. 
However, any other IDEs also are free to use.
  
##Rest endpoint to test
   ```
http://localhost:8080/musify/music-artist/details/{mbid}
   ```
###The following improvements would make the application better performant 
* Spring Reactive framework
* Cache implementation
* Async non blocking way
