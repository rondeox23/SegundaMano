package weakling.segunda.mano.utils;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

import weakling.segunda.mano.authentication.GoogleAuthentication;

public class UtilityPipeline {
    public FirebaseFirestore database;
    public FirebaseStorage storage;
    public FirebaseAuth authentication;
    public GoogleAuthentication gAuth;

    public UtilityPipeline(Context context){
        FirebaseApp.initializeApp(context);
        database = FirebaseFirestore.getInstance();
        database.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build());
        storage = FirebaseStorage.getInstance();
        authentication = FirebaseAuth.getInstance();
        gAuth = new GoogleAuthentication();
    }
}
