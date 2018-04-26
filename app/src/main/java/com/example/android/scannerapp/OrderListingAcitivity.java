package com.example.android.scannerapp;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import helper.CheckNetworkStatus;
import helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderListingActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    //private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_ORDER_ID = "1";

    private static final String BASE_URL = "http://worten.2digital.ie/";
    private ArrayList<HashMap<String, String>> orderList;
    private ListView orderListView;
    private ProgressDialog pDialog;
    private String order_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_listing);
        orderListView = (ListView) findViewById(R.id.movieList);
        new FetchordersAsyncTask().execute();
        Intent intent = getIntent();
        order_id = intent.getStringExtra(KEY_ORDER_ID);
    }

    /**
     * Fetches the list of orders from the server
     */
    private class FetchordersAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(OrderListingActivity.this);
            pDialog.setMessage("Loading orders. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_ORDER_ID, order_id);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_orders.php", "GET", null);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray orders;
                if (success == 1) {
                    orderList = new ArrayList<>();
                    orders = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate orders list
                    for (int i = 0; i < orders.length(); i++) {
                        JSONObject movie = orders.getJSONObject(i);
                        Integer movieId = movie.getInt(KEY__ID);
                        String movieName = movie.getString(KEY_PRODUCT_NAME);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY__ID, movieId.toString());
                        map.put(KEY_PRODUCT_NAME, movieName);
                        movieList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    populateMovieList();
                }
            });
        }

    }

    /**
     * Updating parsed JSON data into ListView
     * */
    private void populateMovieList() {
        ListAdapter adapter = new SimpleAdapter(
                MovieListingActivity.this, movieList,
                R.layout.list_item, new String[]{KEY_MOVIE_ID,
                KEY_MOVIE_NAME},
                new int[]{R.id.movieId, R.id.movieName});
        // updating listview
        movieListView.setAdapter(adapter);
        //Call MovieUpdateDeleteActivity when a movie is clicked
        movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    String movieId = ((TextView) view.findViewById(R.id.movieId))
                            .getText().toString();
                    Intent intent = new Intent(getApplicationContext(),
                            MovieUpdateDeleteActivity.class);
                    intent.putExtra(KEY_MOVIE_ID, movieId);
                    startActivityForResult(intent, 20);

                } else {
                    Toast.makeText(MovieListingActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            // If the result code is 20 that means that
            // the user has deleted/updated the movie.
            // So refresh the movie listing
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}