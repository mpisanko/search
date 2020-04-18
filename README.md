# search

Coding assignment from Zendesk

## Design
In order to make sure search time does not linearly increase with number of documents - a naaive version of inverse index will be used (not taking into account document frequency, just occurences).
There will also be separate indices for relating the entities between each other and searching by ids (internal / external) and boolean/enum type fields.
Indices will be built ahead of time and stored in files in order to avoid long startups - as being a command line app - each invocation only allows a single query.
Given above assumptions I will create following high level modules:
 - ingestion and index building
 - runtime search (given indices and query - find results)
 - command line application which can:
    - read query, invoke search and present results
    - create indices for runtime search

## Installation

Download from https://github.com/mpisanko/search.

## Usage

Run the project's tests:

    $ clojure -A:test:runner

Build an uberjar:

    $ clojure -A:uberjar

Run that uberjar:

    $ java -jar search.jar [Options]

## Options

FIXME: listing of options this app accepts.

# OVERVIEW
Using the provided data (tickets.json and users.json and organization.json) write a simple command line application to search the data and return the results in a human readable format.
* Feel free to use libraries or roll your own code as you see fit. However, please do not use a database or full text search product as we are interested to see how you write the solution.
* Where the data exists, values from any related entities should be included in the results, i.e. searching organization by id should return its tickets and users.
* The user should be able to search on any field, full value matching is fine (e.g. "mar" won't return "mary").
* The user should also be able to search for empty values, e.g. where description is empty.
Search can get pretty complicated pretty easily, we just want to see that you can code a basic but efficient search application. Ideally, search response times should not increase linearly as the number of documents grows. You can assume all data can fit into memory on a single machine.

## EVALUATION CRITERIA
We will look at your project and assess it for:
1. Extensibility - separation of concerns.
2. Simplicity - aim for the simplest solution that gets the job done whilst remaining
readable, extensible and testable.
3. Test Coverage - breaking changes should break your tests.
4. Performance - should gracefully handle a significant increase in amount of data
provided.
5. Robustness - should handle and report errors.
6. Usability - Should provide installation instructions and how easy it is to use the application
7. General technical skills - Demonstrate proficiency in the chosen language and strong attention to details
If you have any questions about these criteria please ask.
## SPECIFICATIONS
* Use the language in which you are strongest.
* Include a README with (accurate) usage instructions.
* Document the assumptions and tradeoffs you’ve made.
## SUBMISSION
Github is the preferred option (a public repo is fine) but we will also accept a .zip file if necessary.
