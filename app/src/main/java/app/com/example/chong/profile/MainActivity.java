package app.com.example.chong.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private Button repositories;
    private Button following;
    private Button followers;
    private Button getGithubInfo;
    private Button search;
    private ImageView imageView;

    //public static Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load image
        imageView = (ImageView) findViewById(R.id.gitimage);
        Picasso.with(this).load("https://avatars0.githubusercontent.com/u/29451652?v=4").into(imageView);

        //Button Repositories
        repositories = (Button) findViewById(R.id.repositories);
        repositories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent repositoriesIntent = new Intent(MainActivity.this, Repositories.class);
                startActivity(repositoriesIntent);
            }
        });

        //Button Following
        following = (Button) findViewById(R.id.following);
        following.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent followingIntent = new Intent(MainActivity.this, Following.class);
                startActivity(followingIntent);
            }
        });

        //Button Followers
        followers = (Button) findViewById(R.id.followers);
        followers.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent followersIntent = new Intent(MainActivity.this, Followers.class);
                startActivity(followersIntent);
            }
        });

        //Button Get GitHub Information
        getGithubInfo = (Button) findViewById(R.id.gitInfo);
        getGithubInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent basicInfoIntent = new Intent(MainActivity.this, BasicInfo.class);
                startActivity(basicInfoIntent);
            }
        });

        //Button Search
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(MainActivity.this, Search.class);
                startActivity(searchIntent);
            }
        });

    }//end of onCreate

}//end of class