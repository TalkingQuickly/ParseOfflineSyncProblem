# Problem Use Case

* A Parse Object of type "Thing" has been queried and stored in an instance variable
* The data is changed on the server (either by cloud code or in the data browser)
* Fetch is used on the instance variable to get the latest data form the server
* The updated data is not available

There are actually two versions of this problem, one with offlineDataStore enabled, which appears
to make it impossible to update the relevant model (without uninstalling). And one with
offlineDataStore disabled which allows the new data to be retrieved with a new query but not
with fetch.

## Replicating

### Without Offline Data

#### Setup

* Ensure `ENABLE_LOCAL_DATA_STORE` is set to false in MainActivity.java
* Add credentials for an empty Parse Application to MainActivity onCreate
* Run the sample app
* Click "GENERATE NEW THING"
* Login to the Parse web interface and find the ID for this object (it's also logged to debug)
* Enter that ID in the text box on the apps UI (it's easier to change this in `activity_main.xml` so
it's always there)

#### Test Case 1 - Querying (Works as expected)

* Click the "Query Remote and Display" (it will show that the array on the parse object contains 1 item)
* In the parse web interface, modify the "col1" array to contain 2 items
* Click on "Query Remote and Display," this constructs a new Parse query for the object with the given
id, runs it and then displays the number of items in the array. This will display that there are now
2 items
* This works as expected

#### Test Case 2 - Fetching (Does not work as expected)

* Click on the "Load From Remote to Instance Variable" button, this will show again that there are still
two items in the array, this button is the same as "Query remote and display" but it stores the resulting
Thing in an instance variable
* In the web interface, modify the "col1" array to contain 3 items
* Click on the "Fetch On Instance Variable" button, this calls fetchInBackground on the instance variable
containing the previously queried object
* Expected Behaviour: the updated object (with an array in `col1` containing 3 items is fetched)
* Actual Behaviour: the previous object, containing 2 items, is still returned
* Performing another query (e.g. "Load from Remote to instance variable" button, will load the updated
record

This is been tried with fetch() and fetchInBackground() with a callback, behaviour is identical.

### With Offline Data

This is our actual use case, behaviour is similar to above but:

* After the data has been loaded once, e.g. the field has gone from null to containing some data,
it is never updated again.
* E.g. calling fetch or querying (with a query not set to use the local data store) will not update
the object
* Once fetch has been called, if data has been changed on the server, all future attempts to save the
model will silently fail to update the server
* If such a "stuck" object is unpinned, when the application is next started, there will be a pinned
object (with the same id) which DOES contain the correct data

The above behaviour is not consistent. E.g. in the original app we discovered this behaviour in, we
have several columns containing arrays of objects. Some of these work absolutely fine and are updated
as expected, others will always fail. But the behaviour of a given column is consistent, e.g. will
always work as expected or always fail.

I can provide test cases for the offline data store problems as well if needed.