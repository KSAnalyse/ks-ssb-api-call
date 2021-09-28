# ks-ssb-api-call
Solution to use the different SSB.no API's
1. Import SsbApiCall.
2. Create new SsbApiCall object with table number and optional classification numbers
3. Run metadataApiCall and klassApiCall
4. Then run tableApiCall which then returns a List<String> of query results.
5. If you need to query several tables, run metadataApiCall with the table number, then tableApiCall again. You don't need supply classification codes more than once.

The current solution needs classification numbers from ssb.no klass API. We have added this so we can filter out regionCodes that aren't valid in the years we are querying for.
It also only queries the last five years.

Future plans include:
1. Custom filters for the metadata
2. Query for custom amount of years
3. Query without classification codes (not filtering for invalid regioncodes for the years you're querying for).
4. Error handling when response code is not 200, including retries of the query that failed.
5. More to come...
