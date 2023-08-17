package com.gaurikasharma27.googlauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button button;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient googleSignInClient;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Creating account");
        progressDialog.setMessage("We are creating your account");

        GoogleSignInOptions gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("438431947620-ecpi41uk3dhhf4mv8g8q993k3vs49ltm.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gsio);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    int RC_SIGN_IN = 40;

    private void signIn() {

        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());

            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void firebaseAuth(String idToken) {

        AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user= auth.getCurrentUser();

                            Users users=new Users();
                            users.setUserId(user.getUid());
                            users.setName(user.getDisplayName());
                            users.setProfile(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            Intent intent=new Intent(MainActivity.this,SecondActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(MainActivity.this,"error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}