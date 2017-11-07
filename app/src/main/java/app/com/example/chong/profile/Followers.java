package app.com.example.chong.profile;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

public class Followers extends AppCompatActivity {


    private ArrayList<HashMap<String, String>> followersList;
    private ListView listView;
    private ProgressDialog pd;
    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        followersList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.list);
        //Initialize firebase
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://githubprofile-71758.firebaseio.com/");

        new Followers.JsonTask().execute("https://api.github.com/users/ChongyeWang/followers");
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Followers.this);
            pd.setMessage("Wait");
            pd.setCancelable(false);
            pd.show();
        }

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
                    Firebase followersChlid = firebase.child("Followers");
                    JSONArray followers_List = new JSONArray(result);
                    for(int i = 0; i < followers_List.length(); i++) {
                        JSONObject followers = followers_List.getJSONObject(i);
                        String name = followers.getString("login");
                        String imageUrl = followers.getString("avatar_url");
                        String link = followers.getString("html_url");
                        HashMap<String, String> followersItem = new HashMap<>();
                        followersItem.put("name", name);
                        //followersItem.put("imageUrl", imageUrl);
                        followersItem.put("link", link);
                        followersList.add(followersItem);
                        Log.d("link" , link);

                        //Save data to fireBase
                        Firebase subChild = followersChlid.child(name);
                        subChild.setValue(followersItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Set simpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(Followers.this, followersList,
                    R.layout.list_followers, new String[] {"name", "link"}, new int[] {R.id.name, R.id.link});
            listView.setAdapter(simpleAdapter);

        }//end of onPostExecute
    }//end of JsonTask class

}
