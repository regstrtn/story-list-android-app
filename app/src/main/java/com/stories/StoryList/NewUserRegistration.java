package com.stories.StoryList;

import static com.stories.StoryList.util.Constants.MIN_PASSWORD_LENGTH;

import android.content.Context;
import android.content.Intent;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.Stories.StoryList.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class NewUserRegistration extends AppCompatActivity {

  private FirebaseAuth firebaseAuth;
  private EditText editTextEmail, editTextDisplayName, editTextPassword, editTextConfirmPassword;
  private TextView backToLoginPage;
  private ProgressBar progressBar;
  private Button registerUserButton;
  private Context ctx;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_registration_activity);
    firebaseAuth = FirebaseAuth.getInstance();
    ctx = this;

    registerUserButton = (Button) findViewById(R.id.RegisterUserButton);
    setRegisterButtonClickListener(registerUserButton);

    editTextEmail = (EditText) findViewById(R.id.NewUserEmailAddress);
    editTextDisplayName = (EditText) findViewById(R.id.NewUserDisplayName);
    editTextPassword = (EditText) findViewById(R.id.NewUserPassword);
    editTextConfirmPassword = (EditText) findViewById(R.id.NewUserConfirmPassword);
    progressBar = (ProgressBar) findViewById(R.id.progressBar);

    setBackToLoginPageClickListener();
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

  private void setRegisterButtonClickListener(Button registerUserButton) {
    registerUserButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString().trim();
        String displayName = editTextDisplayName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        if(email.isEmpty() || displayName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
          editTextEmail.setError("All fields are required!");
          editTextEmail.requestFocus();
          return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          editTextEmail.setError("Email address is not valid!");
          editTextEmail.requestFocus();
          return;
        }

        // Display name must have no whitespace characters.
        if (!displayName.matches("\\S+")) {
          editTextDisplayName.setError("Display name should not contain any spaces!");
          editTextDisplayName.requestFocus();
          return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
          editTextPassword.setError("Password should have atleast 6 characters!");
          editTextPassword.requestFocus();
          return;
        }

        if(!password.equals(confirmPassword)) {
          editTextPassword.setError("Passwords do not match!");
          editTextPassword.requestFocus();
          return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                  UserProfileChangeRequest profileUpdates =
                      new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
                  firebaseAuth.getCurrentUser().updateProfile(profileUpdates)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                          Toast.makeText(NewUserRegistration.this, "Registration successful!",
                              Toast.LENGTH_LONG).show();
                          startActivity(new Intent(ctx, MainActivity.class));
                        }
                      });
                }
                else {
                  Toast.makeText(NewUserRegistration.this, "Could not register. Try again!",
                      Toast.LENGTH_LONG).show();
                }
              }
            });
      }
    });
  }

  private void goToFeedsActivity() {
    startActivity(new Intent(this, MainActivity.class));
  }

  private void setBackToLoginPageClickListener() {
    backToLoginPage = (TextView) findViewById(R.id.BackToLoginPageMessage);
    backToLoginPage.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(NewUserRegistration.this, UserLogin.class));
      }
    });
  }
}