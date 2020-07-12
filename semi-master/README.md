[![Build Status](https://travis-ci.com/AntonisGkortzis/semi.svg?token=G9zDqfcXKyqNsWnKiyZr&branch=master)](https://travis-ci.com/AntonisGkortzis/semi)

# Single Responsibility Principle based Extract Method Identification
The Single responsibility principle based Extract Method Identification (__semi__) tool 
was developed as part of the Charalampidou et al., 2017 study [1].

You can use the following to reference the tool: <br>
[1] _S. Charalampidou, A. Ampatzoglou, A. Chatzigeorgiou, A. Gkortzis, and P. Avgeriou. “Identifying Extract Method Refactoring Opportunities Based on Functional Relevance.” IEEE Transactions on Software Engineering 43, no. 10 (October 1, 2017): 954–74. https://doi.org/10.1109/TSE.2016.2645572._

A copy of the paper is available [here](http://users.uom.gr/~a.ampatzoglou/papers/charalampidou2017tse.pdf).

## Prerequisites 
1. Java JDK > 1.8
2. Apache Maven > 3

## Using the semi tool
Build the tool executing the following:
```bash
cd semi
mvn install
```
And run:
```bash
cd target
java -jar semi-0.0.1-jar-with-dependencies.jar
```

__semi__ is a Maven project and thus, is not bound to any IDE specific configurations.
Just load it as a Maven project (root directory is ```semi/semi```) and Run the ```gui.LongMethodDetector``` class.

The ```semi/demo_cases``` directory contains classes 
that can be used for demonstrating the __semi__ tool. 

## Contributing to the project
Contributions are welcome! Please 
1. ```fork``` the repository, 
2. create a new ```branch``` and finally,
3. create a ```pull request``` when you want to merge your changes. 
