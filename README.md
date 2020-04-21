# search

Coding assignment from Zendesk

## Design
In order to make sure search time does not linearly increase with number of documents - a naaive version of inverse index will be used (not taking into account document frequency, just occurences, without stemming or lemmatisation).
There will be separate indices per entity, which also relate the entities between each other.
Indices will be built ahead of time and stored in files in order to avoid long startups - as being a command line app - each invocation only allows a single query.
In the interest of search performance the indices will be denormalised (eg. each organisation will have each of its users and tickets on the document).
Searching will only match on full word(s), case insensitive. 
The application takes a rather simplified approach of only allowing searching a single entity at a time (organisation/use/ticket).
Given above assumptions I will create following high level modules:
 - index building
 - runtime search (given indices and query - find results)
 - command line application which can:
    - read query, invoke search and present results
    - create indices for runtime search
 - presenters for showing results in human readable form 
 
### Load testing
There is a single load test (`test/load/search_load_test.clj`) which will run a query for each of the entities with specified arguments and empty flag.
Tests will use files in the root directory (organizations.json, users.json, tickets.json) which can be substituted for some containing more entities.
To run it use:

    $ clojure -A:test:runner:load -i :load

optionally passing environment variables `ARGS` and `EMPTY` to set those values, eg:

    $ ARGS='artisan geekfarm' clojure -A:test:runner:load -i :load

will run load tests for each of the entities searching for those matching 'artisan' or 'geekfarm'

    $ ARGS=external_id EMPTY=true clojure -A:test:runner:load -i :load

will run load tests for each entity looking for those with empty external_id field.

### Outstanding things (not done due to timeboxing this task)
 - search using flags for boolean attributes (eg user active, verified, etc)
 - compilation with GraalVM to achieve shorter startup times (JVM takes significant time starting up due to loading classes etc. GraalVM produces native executable with much better startup times)

## Installation

Install JDK 11 or higher (even though 8+ should also work) and optionally [Clojure](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools).
Download from https://github.com/mpisanko/search.

## Usage

Run the project's tests:

    $ clojure -A:test:runner

Build an uberjar:

    $ clojure -A:uberjar

Run that uberjar:

    $ java -jar search.jar [Options]

## Options
Following options are available to pass to application invocation:

  -h, --help          Print help information
  
  -i, --index         Create indices
  
  -o, --organisation  Query by organisation
  
  -u, --user          Query by user
  
  -t, --ticket        Query by ticket
  
  -e, --empty         Query for empty field specifying entity (one of the above flags) and field as argument, eg: '-u alias'  

It's a good idea to first create indices (run the application with -i flag) before querying.
The application requires input files (organizations.json, users.json, tickets.json in the working directory - root of project, where the jar is).
It will create indices in the root directory of the project (or whereever you run the jar from) - so that directory needs to be user-writeable and have sufficient disk space. 
When querying the arguments will be search query or field which should be empty (when querying for empty field values - specify which entity you're querying using flag, eg `-e -u alias` to query for users who do not have alias)

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
