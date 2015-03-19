package com.activeintime.parseofflinesyncproblem;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.activeintime.parseofflinesyncproblem.models.Thing;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    // this is the column we're going to try and update/ break
    private static final String COL_NAME = "col1";
    private static final boolean  ENABLE_LOCAL_DATA_STORE  = false;
    private Thing mThing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ENABLE_LOCAL_DATA_STORE)
            Parse.enableLocalDatastore(getApplicationContext());
        ParseObject.registerSubclass(Thing.class);
        Parse.initialize(
                getApplicationContext(),
                "",
                ""
        );
        ParseUser.enableAutomaticUser();
        try {
            ParseUser.getCurrentUser().increment("RunCount");
            ParseUser.getCurrentUser().save();
            ParseConfig.getInBackground();
            Log.d(TAG, "Current user is" + ParseUser.getCurrentUser().getObjectId());
            ParseUser.getCurrentUser().fetchInBackground();
        } catch (ParseException e) {
            Log.e(TAG, "Error saving user", e);
        }
        setContentView(R.layout.activity_main);

        Button add = (Button) findViewById(R.id.button_new_thing);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thing t = new Thing();
                JSONArray ja = new JSONArray();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("Hello", "World");
                    ja.put(jo);
                    t.put(COL_NAME, ja);
                    if (ENABLE_LOCAL_DATA_STORE)
                        t.pin();
                    t.save();
                    Log.d(TAG, "Generated a thing, saved it, all the goodness" +
                            " id is: " + t.getObjectId());
                } catch (JSONException e) {
                    Log.e(TAG, "Error generating thing", e);
                } catch (ParseException e) {
                    Log.e(TAG, "Error pinning or saving, but not both", e);
                }
            }
        });

        final EditText et = (EditText) findViewById(R.id.edit_text_object_id);
        final TextView tv = (TextView) findViewById(R.id.text_view_result);

        Button loadFromLocal = (Button) findViewById(R.id.button_load_thing_from_local);
        loadFromLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thing thing = getFromLocal(et.getText().toString());
                tv.setText("Items in array: " + thing.getJSONArray(COL_NAME).length());
                Log.d(TAG, "Items in array: " + thing.getJSONArray(COL_NAME).length());
            }
        });

        Button fetch = (Button) findViewById(R.id.button_fetch_and_display);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thing thing = getFromLocal(et.getText().toString());
                try {
                    thing.fetch(); // would never do this in main thread in real life
                    Log.d(TAG, "Fetch completed");
                    tv.setText("Items in array: " + thing.getJSONArray(COL_NAME).length());
                    Log.d(TAG, "Items in array: " + thing.getJSONArray(COL_NAME).length());
                } catch (ParseException e) {
                    Log.e(TAG, "Error fetching thing", e);
                }
            }
        });

        Button query = (Button) findViewById(R.id.button_query_remote_and_display);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thing thing = getFromRemote(et.getText().toString());
                tv.setText("Items in array: " + thing.getJSONArray(COL_NAME).length());
                Log.d(TAG, "Items in array: " + thing.getJSONArray(COL_NAME).length());
            }
        });

        Button queryToInstance = (Button) findViewById(R.id.button_load_remote_to_instance);
        queryToInstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mThing = getFromRemote(et.getText().toString());
                tv.setText("Items in array: " + mThing.getJSONArray(COL_NAME).length());
                Log.d(TAG, "Items in array: " + mThing.getJSONArray(COL_NAME).length());
            }
        });


        Button fetchInstance = (Button) findViewById(R.id.button_fetch_instance);
        fetchInstance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    mThing = mThing.fetch();
//                    tv.setText("Items in array: " + mThing.getJSONArray(COL_NAME).length());
//                    Log.d(TAG, "Items in array: " + mThing.getJSONArray(COL_NAME).length());
                    mThing.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            tv.setText("Items in array: " + parseObject.getJSONArray(COL_NAME).length());
                            Log.d(TAG, "Items in array (returned obj): " + parseObject.getJSONArray(COL_NAME).length());
                            Log.d(TAG, "Items in array (mThing): " + mThing.getJSONArray(COL_NAME).length());
                        }
                    });
//                } catch (ParseException e) {
//                    Log.e(TAG, "Error getching instance variable",e);
//                }

            }
        });
    }

    private Thing getFromRemote(String objectId) {
        ParseQuery<Thing> query = new ParseQuery<Thing>("Thing");
        try {
            return query.get(objectId);
        } catch (ParseException e) {
            Log.e(TAG, "Error querying",e);
        }
        return null;
    }

    private Thing getFromLocal(String objectId) {
        ParseQuery<Thing> query = new ParseQuery<Thing>("Thing");
        try {
            query.fromLocalDatastore();
            return query.get(objectId); // would never do this in main thread in real life
        } catch (ParseException e) {
            Log.e(TAG, "Error getting from local datastore", e);
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
