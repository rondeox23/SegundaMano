package weakling.segunda.mano.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import weakling.segunda.mano.R;
import weakling.segunda.mano.Rb;

public class Authentication extends AppCompatActivity {
    AutoCompleteTextView login_email;
    EditText login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Rb.activity = this;
        Rb.instatiateProgressDialog();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        login_email = (AutoCompleteTextView) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
    }

    public void authenticate(View v){
        String email = login_email.getText().toString();
        String password = login_password.getText().toString();
        Rb.utilityPipeline.authentication.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if(task.isSuccessful()){
                           //Logged in
                           startActivity(Rb.redirect);
                       }else{
                           //Wrong Credentials
                           Snackbar.make(findViewById(R.id.login_id), "Invalid Credential!", Snackbar.LENGTH_INDEFINITE).show();
                       }
                    }
                });
    }

    public void signup(View v){
        Intent intent = new Intent(Authentication.this,Newerth.class);
        startActivity(intent);
    }

    public void google(View v){
        Rb.progressDialog.show();
        Rb.utilityPipeline.gAuth.set(this,getString(R.string.default_web_client_id));
        Rb.utilityPipeline.gAuth.checkAndUpdate();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Rb.utilityPipeline.gAuth.setOnActivityForResult(requestCode,resultCode,data);
    }
}
