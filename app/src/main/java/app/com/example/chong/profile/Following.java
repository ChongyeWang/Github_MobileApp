package app.com.example.chong.profile;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Following extends AppCompatActivity {


    private ArrayList<HashMap<String, String>> followingList;
    private ListView listView;
    private ProgressDialog pd;

    private Button postUser;
    private EditText editText;
    private String user;

    private Button unfollow;
    private EditText editText2;
    private String user2;

    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        followingList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.list);

        //Initialize firebase
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://githubprofile-71758.firebaseio.com/");

        //List the following
        new Following.JsonTask().execute("https://api.github.com/users/ChongyeWang/following");

        //Get the input from the user to follow
        postUser = (Button)findViewById(R.id.follow);
        postUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editText = (EditText)findViewById(R.id.textView1);
                user = editText.getText().toString();
                Log.d("User", user);
                String url = "https://api.github.com/user/following/" + user;
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest req = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Response", error.toString());
                        }
                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> params = new HashMap<String, String>();
                            String creds = String.format("%s:%s","ChongyeWang","4362be4bb0dddabb96229fd949cff7ce11a95856");
                            String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                            params.put("Authorization", auth);
                            return params;
                        }
                    };
                requestQueue.add(req);
            }
        });


        //Get the input from the user to unfollow
        unfollow = (Button) findViewById(R.id.unfollow);
        unfollow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editText2 = (EditText)findViewById(R.id.textView2);
                user2 = editText2.getText().toString();
                Log.d("EditText", user2);
                String url = "https://api.github.com/user/following/" + user2;
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest req = new StringRequest(Request.Method.DELETE, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Response", error.toString());
                        }
                    })
                    {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<String, String>();
                        String creds = String.format("%s:%s","ChongyeWang","4362be4bb0dddabb96229fd949cff7ce11a95856");
                        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                        params.put("Authorization", auth);
                        return params;
                    }
                };
                requestQueue.add(req);
            }
        });

    }//end of onCreate


    private class JsonTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Following.this);
            pd.setMessage("Wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", line);
                }
                return buffer.toString();//used in onPostExecute() method

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (pd.isShowing()){
                pd.dismiss();
            }//close ProgressDialog
            if(result != null){
                try {
                    Firebase followChlid = firebase.child("Following");
                    JSONArray following_List = new JSONArray(result);
                    for(int i = 0; i < following_List.length(); i++) {
                        JSONObject following = following_List.getJSONObject(i);
                        String name = following.getString("login");
                        String imageUrl = following.getString("avatar_url");
                        String link = following.getString("html_url");
                        HashMap<String, String> followingItem = new HashMap<>();
                        followingItem.put("name", name);
                        followingItem.put("link", link);
                        followingList.add(followingItem);
                        Log.d("link" , link);

                        //Save data to fireBase
                        Firebase subChild = followChlid.child(name);
                        subChild.setValue(followingItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Set simpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(Following.this, followingList,
                    R.layout.list_following, new String[] {"name", "link"}, new int[] {R.id.name, R.id.link});
            listView.setAdapter(simpleAdapter);

        }//end of onPostExecute
    }//end of JsonTask class

}