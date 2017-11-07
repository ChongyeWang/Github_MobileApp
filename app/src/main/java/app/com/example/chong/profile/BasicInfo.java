package app.com.example.chong.profile;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.firebase.client.Firebase;
import com.squareup.picasso.Picasso;

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

public class BasicInfo extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> infoList;
    private ListView listView;
    private ProgressDialog pd;
    private ImageView imageView;
    private String imageURl;
    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basicinfo);

        infoList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.list);

        //Load image
        imageView = (ImageView) findViewById(R.id.gitimage);
        Picasso.with(this).load("https://avatars0.githubusercontent.com/u/29451652?v=4").into(imageView);

        //Create fireBase
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://githubprofile-71758.firebaseio.com/");

        //Execute new AsyncTask
        new BasicInfo.JsonTask().execute("https://api.github.com/users/ChongyeWang");
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(BasicInfo.this);
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
                    Firebase rep = firebase.child("Profile_Information");
                    JSONObject git = new JSONObject(result);
                    String gitName = git.getString("name");
                    String gitUrl = git.getString("html_url");
                    String gitLocation = git.getString("location");
                    String gitPublic_repos = git.getString("public_repos");
                    String gitFollowers = git.getString("followers");
                    String gitFollowing = git.getString("following");
                    String gitCreated_at = git.getString("created_at");

                    HashMap<String, String> gitObject = new HashMap<>();
                    gitObject.put("gitName", "Name : " + gitName);
                    gitObject.put("gitUrl", "Link : " + gitUrl);
                    gitObject.put("gitLocation", "Location : " + gitLocation);
                    gitObject.put("gitPublic_repos", "Public_Repos : " + gitPublic_repos);
                    gitObject.put("gitFollowers", "Followers : " + gitFollowers);
                    gitObject.put("gitFollowing", "Following : " + gitFollowing);
                    gitObject.put("gitCreated_at", "Created_at : " + gitCreated_at);

                    //add the hashmap to the list
                    infoList.add(gitObject);

                    //Get the url of the image
                    imageURl = git.getString("avatar_url");

                    //Save data to fireBase
                    Firebase subChild = rep.child(gitName);
                    subChild.setValue(gitObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Set simpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(BasicInfo.this, infoList,
                    R.layout.list_gititem, new String[] {"gitName", "gitUrl", "gitLocation", "gitPublic_repos", "gitFollowers",
                    "gitFollowing", "gitCreated_at"}, new int[] {R.id.gitName, R.id.gitLink, R.id.gitLocation, R.id.gitPublicRepos,
                    R.id.gitFollowers, R.id.gitFollowing, R.id.gitCreated_at});
            listView.setAdapter(simpleAdapter);

        }//end of onPostExecute
    }//end of JsonTask class
}