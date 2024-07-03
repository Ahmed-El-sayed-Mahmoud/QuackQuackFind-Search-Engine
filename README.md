# Introduction
This project is a simple Crawler- based search engine that demonstrates the main
features of a search engine (web crawling, indexing and ranking) and the interaction between them.
# Languages and Frameworks 
1- Java

2- MongoDB

3- React

4- Spring

# Search Engine Modules
## Web Crawler 
The web crawler is a software agent that collects documents from the web. The crawler starts with a list of URL
addresses (seed set). It downloads the documents identified by these URLs and extracts hyper-links from them.
The extracted URLs are added to the list of URLs to be downloaded. Thus, web crawling is a recursive process.
## Indexer 
The output of web crawling process is a set of downloaded HTML documents. To respond to user queries fast
enough, the contents of these documents have to be indexed in a data structure that stores the words contained
in each document and their importance.
We tried to keep the time to store the huge amount of data to a minimum and also the time to get information from the database to make the search operation as efficient as possible.
## Query Processor 
This module receives search queries, performs necessary preprocessing and searches the index for relevant
documents. Retrieve documents containing words that share the same stem with those in the search query. For
example, the search query “travel” should match (with lower degree) the words “traveler”, “traveling” ... etc.
Ranker 
The ranker module sorts documents based on their popularity and relevance to the search query.
1. Relevance
Relevance is a relation between the query words and the result page and could be calculated in several
ways such as tf-idf of the query word in the result page or simply whether the query word appeared in
the title, heading, or body. And then you aggregate the scores from all query words to produce the final
page relevance score.
