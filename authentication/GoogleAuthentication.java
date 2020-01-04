package weakling.segunda.mano.authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import weakling.segunda.mano.Rb;

public class GoogleAuthentication {
    GoogleSignInClient googleSigninClient;
    FirebaseUser currentUser;
    Activity activity;

    public GoogleAuthentication set(Activity activity, String token){
        this.activity = activity;
        GoogleSignInOptions googleSigninOpts = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token)
                .requestEmail()
                .build();
        googleSigninClient = GoogleSignIn.getClient(this.activity,googleSigninOpts);
        currentUser = Rb.utilityPipeline.authentication.getCurrentUser();
        Rb.utilityPipeline.authentication = FirebaseAuth.getInstance();
        return this;
    }

    public void checkAndUpdate(){
        if(!isAuthenticated()){
            signIn();
        }
    }

    public void setOnActivityForResult(int requestCode, int resultCode, Intent data){
        if(requestCode==9001){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                // TODO: Update UI to null
                activity.finish();
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        Rb.utilityPipeline.authentication.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            currentUser = Rb.utilityPipeline.authentication.getCurrentUser();
                            Toast.makeText(activity,currentUser.getEmail(),Toast.LENGTH_LONG).show();
                            // TODO: Update UI to current user
                            activity.startActivity(Rb.redirect);
                        }else{
                            // TODO: Update UI to null
                            activity.finish();
                        }
                        Rb.progressDialog.dismiss();
                    }
                });
    }

    public static int requestCode = 9001;

    public void signIn(){
        Intent intent = googleSigninClient.getSignInIntent();
        activity.startActivityForResult(intent,requestCode);
    }

    public boolean isAuthenticated(){
        return currentUser!=null;
    }

}
