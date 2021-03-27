package com.android.StoryList;

import android.content.Context;
import android.content.Intent;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.StoryList.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class UserLogin extends AppCompatActivity {

  private EditText editTextEmail, editTextPassword;
  private Button loginButton;
  private TextView registerUserLink;
  private Context ctx;

  private FirebaseAuth firebaseAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_login_activity);
    ctx = this;

    loginButton = (Button) findViewById(R.id.LoginUserButton);
    editTextEmail = (EditText) findViewById(R.id.LoginPageEmailAddress);
    editTextPassword = (EditText) findViewById(R.id.LoginPagePassword);

    setLoginButtonClickListener();

    setGoToRegistrationPageClickListener();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.discard_current_action_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case(R.id.AbandonAction):
        goToFeedsActivity();
        break;
      default:
        super.onOptionsItemSelected(item);
    }
    return true;
  }

  public void setLoginButtonClickListener() {

    loginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
          editTextEmail.setError("Both email and password are required.");
          editTextEmail.requestFocus();
          return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          editTextEmail.setError("Please provide a valid email.");
          editTextEmail.requestFocus();
          return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // Redirect to feeds page.
                  Toast.makeText(UserLogin.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                  startActivity(new Intent(UserLogin.this, MainActivity.class));
                }
                else {
                  // Show error.
                  Toast.makeText(UserLogin.this, "Failed to login. Please check your credentials.", Toast.LENGTH_LONG).show();
                }
              }
            }
        );
      }
    });
  }

  public void setGoToRegistrationPageClickListener() {
    registerUserLink = (TextView) findViewById(R.id.LoginPageRegistrationRedirectionMessage);
    registerUserLink.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(ctx, NewUserRegistration.class));
      }
    });
  }

  private void goToFeedsActivity() {
    startActivity(new Intent(this, MainActivity.class));
  }
}