package app.com.example.chong.profile;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

public class Search extends AppCompatActivity {

    private Button searchUser;
    private EditText editText;
    private String user;
    private ProgressDialog pd;
    private ListView listView;
    private ArrayList<HashMap<String, String>> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = (ListView) findViewById(R.id.list);

        searchUser = (Button)findViewById(R.id.search);
        searchUser.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                editText = (EditText)findViewById(R.id.textView1);
                user = editText.getText().toString();

                String url = "https://api.github.com/search/users?q=" + user;
                new JsonTask().execute(url);
            }
        });
    }

    private class JsonTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Search.this);
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
            userList= new ArrayList<HashMap<String, String>>();
            if (pd.isShowing()){
                pd.dismiss();
            }//close ProgressDialog
            if(result != null){
                try {
                    JSONObject user = new JSONObject(result);
                    JSONArray user_List = new JSONArray(user.getString("items"));
                    for(int i = 0; i < user_List.length(); i++) {
                        JSONObject userObject = user_List.getJSONObject(i);
                        String name = userObject.getString("login");
                        String link = userObject.getString("html_url");
                        HashMap<String, String> userItem = new HashMap<>();
                        userItem.put("name", name);
                        userItem.put("link", link);
                        userList.add(userItem);
                        Log.d("link" , link);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Set simpleAdapter
            SimpleAdapter simpleAdapter = new SimpleAdapter(Search.this, userList,
                    R.layout.list_item, new String[] {"name", "link"}, new int[] {R.id.name, R.id.link});
            listView.setAdapter(simpleAdapter);
        }//end of onPostExecute
    }//end of JsonTask class
}
