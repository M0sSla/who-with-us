package edu.mirea.onebeattrue.znakomstva.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.mirea.onebeattrue.znakomstva.MainActivity;
import edu.mirea.onebeattrue.znakomstva.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // потеря фокуса ввода email при нажатии кнопки назад
        binding.email.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // Пользователь нажал кнопку "назад"
                    binding.email.clearFocus();
                    return true;
                }
                return false;
            }
        });

        // потеря фокуса ввода password при нажатии кнопки назад
        binding.password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // Пользователь нажал кнопку "назад"
                    binding.password.clearFocus();
                    return true;
                }
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();

        binding.RegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(binding.email.getText());
                password = String.valueOf(binding.password.getText());

                if (TextUtils.isEmpty(email)) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверяем правильность формата адреса электронной почты с помощью регулярного выражения
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Проверяем длину пароля
                if (password.length() < 8) {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Password should be at least 8 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                binding.progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Authentication failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}