package com.example.fixed_guess_who;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private TextView tvStatusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        SignInButton btnGoogle = findViewById(R.id.btnGoogleSignIn);
        LoginButton btnFacebook = findViewById(R.id.btnFacebookSignIn);

        // Google Sign-In Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            tvStatusMessage.setVisibility(View.GONE);
            mAuth.signOut(); // Sign out from Firebase
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        // Facebook Setup
        mCallbackManager = CallbackManager.Factory.create();
        btnFacebook.setPermissions("email", "public_profile");
        btnFacebook.setOnClickListener(v -> {
            MusicManager.sound_login(this);
            tvStatusMessage.setVisibility(View.GONE);
            mAuth.signOut();
            com.facebook.login.LoginManager.getInstance().logOut();
        });
        btnFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle specific error messages from intent (e.g. from StartActivity)
        String error = getIntent().getStringExtra("LOGIN_ERROR");
        if (error != null) {
            tvStatusMessage.setText(error);
            tvStatusMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        signInWithCredential(credential);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        signInWithCredential(credential);
    }

    private void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                checkSessionAndUserRecord(user);
            } else {
                Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSessionAndUserRecord(FirebaseUser user) {
        if (user == null) return;

        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean isOnline = snapshot.child("isOnline").getValue(Boolean.class);
                    if (isOnline != null && isOnline) {
                        mAuth.signOut();
                        mGoogleSignInClient.signOut();
                        tvStatusMessage.setText("משתמש זה כבר מחובר במכשיר אחר");
                        tvStatusMessage.setVisibility(View.VISIBLE);
                    } else {
                        String username = snapshot.child("username").getValue(String.class);
                        if (username == null || username.isEmpty()) {
                            showUsernameSelectionDialog(user);
                        } else {
                            proceedToMenu(user.getUid());
                        }
                    }
                } else {
                    // New user from social login
                    showUsernameSelectionDialog(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUsernameSelectionDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר שם משתמש");
        builder.setCancelable(false);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_username_selection, null);
        builder.setView(dialogView);

        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        TextView tvError = dialogView.findViewById(R.id.tvUsernameError);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btnSaveUsername);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.board_background);
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                tvError.setText("הכנס שם משתמש");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            if (username.matches(".*[.#$\\[\\]].*")) {
                tvError.setText("שם משתמש מכיל תווים אסורים");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            mDatabase.child("usernames").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String existingUid = snapshot.getValue(String.class);
                        if (user.getUid().equals(existingUid)) {
                            // It's their own username, just proceed
                            saveNewUser(user, username, dialog);
                        } else {
                            tvError.setText("שם המשתמש כבר תפוס");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        saveNewUser(user, username, dialog);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    private void saveNewUser(FirebaseUser fbUser, String username, AlertDialog dialog) {
        String uid = fbUser.getUid();
        User newUser = new User(username, 100, "רגיל");
        newUser.email = fbUser.getEmail();
        newUser.isOnline = true;

        mDatabase.child("users").child(uid).setValue(newUser).addOnSuccessListener(unused -> {
            mDatabase.child("usernames").child(username).setValue(uid);
            dialog.dismiss();
            proceedToMenu(uid);
        });
    }

    private void proceedToMenu(String uid) {
        mDatabase.child("users").child(uid).child("isOnline").setValue(true);
        mDatabase.child("users").child(uid).child("isOnline").onDisconnect().setValue(false);
        startActivity(new Intent(LoginActivity.this, MenuActivity.class));
        finish();
    }
}