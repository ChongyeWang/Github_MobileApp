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


public class Repositories extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> repositoryList;
    private ListView listView;
    private ProgressDialog pd;
    private ProgressDialog pd2;
    private Firebase firebase;
    private EditText editText;
    private EditText editText2;
    private Button star;
    private Button unstar;
    private String repoTodo;
    private String repoTodo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        repositoryList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.list);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://githubprofile-71758.firebaseio.com/");

        //Create new JsonTask
        new JsonTask().execute("https://api.github.com/users/ChongyeWang/repos");

        //Get the input from the user to star
        star = (Button)findViewById(R.id.star);
        star.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editText = (EditText)findViewById(R.id.textView1);
                repoTodo = editText.getText().toString();
                String url = "https://api.github.com/user/starred/ChongyeWang/" + repoTodo;
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


        //Get the input from the user to unstar
        unstar = (Button)findViewById(R.id.unstar);
        unstar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editText2 = (EditText)findViewById(R.id.textView2);
                repoTodo2 = editText2.getText().toString();
                String url = "https://api.github.com/user/starred/ChongyeWang/" + repoTodo2;
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
    }

    private class JsonTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Repositories.this);
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
                    Firebase rep = firebase.child("Repositories");
                    JSONArray repoList = new JSONArray(result);
                    for(int i = 0; i < repoList.length(); i++) {
                        JSONObject repo = repoList.getJSONObject(i);
                        String repoName = repo.getString("name");
                        String repoLink = repo.getString("html_url");
                        HashMap<String, String> repository = new HashMap<>();
                        repository.put("name", repoName);
                        repository.put("link", repoLink);
                        Log.d("Link", repository.get("link"));
                        repositoryList.add(repository);

                        //Save data to fireBase
                        Firebase subChild = rep.child(repoName);
                        subChild.setValue(repository);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Set simpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(Repositories.this, repositoryList,
              R.layout.list_item, new String[] {"name", "link"}, new int[] {R.id.name, R.id.link});
            listView.setAdapter(simpleAdapter);

        }//end of onPostExecute
    }//end of JsonTask class
}//end of class