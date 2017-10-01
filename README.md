# Search-Engine
This project is part of the course work for CECS 529. The project pieces together various search engine features. 

## Getting Started
When you run the program, the application UI is displayed. User is provided a search box, a button called 'Folder' and another button called 'Search'.
Clicking on the 'Folder' button enables user to select a directory that contains a list of text files that will be indexed.
User may then query terms by entering the query text in the text box and clicking on the 'Search' button.
A list of documents containing the queried term is displayed for the user.
User may additionallly, click on a document name from the displayed document list to view the original text of the document.
### Query Language
User queries of the form Q<sub>1</sub> + Q<sub>2</sub> + ... + Q<sub>k</sub> are supported.
Where the + represents OR, and we call each Q<sub>i</sub> a subquery, defned as a sequence of query literals
separated by white spaces. 

A query literal is one of the following:

1. a single token.
2. a sequence of tokens that are within double quotes, representing a phrase literal.

### Additional features

* NEAR operator
* Graphical user interface
* Unit testing framework
* Wildcard queries

## Authors

* **Darryl D'mello**
* **Luqman Khan**
* **Sina Astani**

## Acknowledgments

* Thank you Mr.T for being an awesome professor.
