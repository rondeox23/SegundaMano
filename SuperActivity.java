package weakling.segunda.mano;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import weakling.segunda.mano.utils.mFx;

public class SuperActivity extends AppCompatActivity {
    //This is intended for initialization and checks if there's an internet connection

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super);
        mFx.scale_up(this,findViewById(R.id.ml));
        findViewById(R.id.ml).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SuperActivity.this,MainActivity.class));
            }
        });
    }
}
