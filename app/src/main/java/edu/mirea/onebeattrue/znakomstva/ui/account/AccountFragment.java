package edu.mirea.onebeattrue.znakomstva.ui.account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.mirea.onebeattrue.znakomstva.R;
import edu.mirea.onebeattrue.znakomstva.databinding.FragmentAccountBinding;
import edu.mirea.onebeattrue.znakomstva.ui.auth.Login;
import edu.mirea.onebeattrue.znakomstva.ui.chat.ChatMessage;

public class AccountFragment extends Fragment {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isConnected;



    Map<String, Boolean> interestsMap = new HashMap<>();

    // Получение ссылки на хранилище Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FragmentAccountBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("users");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel homeViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getContext(), Login.class);
            startActivity(intent);
            requireActivity().finish(); // возможно не будет работать
        }
        else {
            // отображение email пользователя
            binding.userDetails.setText(user.getEmail());
            usersRef.child(user.getUid()).child("email").setValue(user.getEmail());

            // отображение аватарки
            // Создание слушателя для получения значения аватарки
            ValueEventListener avatarListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String avatarUrl = dataSnapshot.getValue(String.class);

                        // Использование библиотеки Picasso для загрузки и отображения аватарки
                        Picasso.get().load(avatarUrl).into(binding.avatar);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обработка ошибок получения значения аватарки
                    binding.avatar.setImageResource(R.drawable.default_profile_picture);
                }
            };

            // Добавление слушателя к узлу аватарки текущего пользователя
            usersRef.child(user.getUid()).child("avatarUrl").addListenerForSingleValueEvent(avatarListener);

            // отображение имени пользователя
            // Создание слушателя для получения значения имени пользователя
            ValueEventListener usernameListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.getValue(String.class);
                        binding.userNameTextView.setText(username);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обработка ошибок получения значения имени пользователя
                    binding.userNameTextView.setText("noname");
                }
            };

            // Добавление слушателя к узлу аватарки текущего пользователя
            usersRef.child(user.getUid()).child("userName").addListenerForSingleValueEvent(usernameListener);


            // отображение состояния чекбоксов
            usersRef.child(user.getUid()).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Получение данных из базы данных
                    if (dataSnapshot.exists()) {
                        // Обновение состояния чекбоксов на основе данных из базы данных
                        boolean music = Boolean.TRUE.equals(dataSnapshot.child("music").getValue(Boolean.class));
                        boolean sport = Boolean.TRUE.equals(dataSnapshot.child("sport").getValue(Boolean.class));
                        boolean art = Boolean.TRUE.equals(dataSnapshot.child("art").getValue(Boolean.class));
                        boolean movies = Boolean.TRUE.equals(dataSnapshot.child("movies").getValue(Boolean.class));
                        boolean education = Boolean.TRUE.equals(dataSnapshot.child("education").getValue(Boolean.class));
                        boolean social = Boolean.TRUE.equals(dataSnapshot.child("social").getValue(Boolean.class));
                        boolean culinary = Boolean.TRUE.equals(dataSnapshot.child("culinary").getValue(Boolean.class));
                        boolean technology = Boolean.TRUE.equals(dataSnapshot.child("technology").getValue(Boolean.class));

                        // Обновите состояние чекбоксов на основе полученных значений
                        binding.checkBox1.setChecked(music);
                        binding.checkBox2.setChecked(sport);
                        binding.checkBox3.setChecked(art);
                        binding.checkBox4.setChecked(movies);
                        binding.checkBox5.setChecked(education);
                        binding.checkBox6.setChecked(social);
                        binding.checkBox7.setChecked(culinary);
                        binding.checkBox8.setChecked(technology);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Обработка ошибки чтения данных из базы данных
                    binding.checkBox1.setChecked(false);
                    binding.checkBox2.setChecked(false);
                    binding.checkBox3.setChecked(false);
                    binding.checkBox4.setChecked(false);
                    binding.checkBox5.setChecked(false);
                    binding.checkBox6.setChecked(false);
                    binding.checkBox7.setChecked(false);
                    binding.checkBox8.setChecked(false);
                }
            });
        }

        binding.editUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.userNameEditTextView.setText("");

                binding.userNameTextView.setVisibility(View.GONE);
                binding.userNameTextViewLayout.setVisibility(View.GONE);

                binding.editUserNameBtn.setVisibility(View.GONE);

                binding.userNameEditTextView.setVisibility(View.VISIBLE);
                binding.userNameEditTextViewLayout.setVisibility(View.VISIBLE);

                binding.saveUserNameBtn.setVisibility(View.VISIBLE);
            }
        });

        binding.saveUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username;
                username = String.valueOf(binding.userNameEditTextView.getText());

                if (!username.trim().isEmpty()) {
                    username = username.trim();

                    usersRef.child(user.getUid()).child("userName").setValue(username);

                    binding.userNameTextView.setText(username);

                    binding.userNameEditTextView.setVisibility(View.GONE);
                    binding.userNameEditTextViewLayout.setVisibility(View.GONE);

                    binding.saveUserNameBtn.setVisibility(View.GONE);

                    binding.userNameTextView.setVisibility(View.VISIBLE);
                    binding.userNameTextViewLayout.setVisibility(View.VISIBLE);

                    binding.editUserNameBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Username changed successfully", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getContext(), "Enter username", Toast.LENGTH_SHORT).show();
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        binding.changeAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Получение пути к файлу в Firebase Storage
        String avatarPath = "avatars/" + user.getUid() + ".jpg";
        StorageReference avatarRef = storageRef.child(avatarPath);

        // Инициализация ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();

                            // Загрузка файла в Firebase Storage
                            UploadTask uploadTask = avatarRef.putFile(imageUri);

                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Получение URL-адреса загруженной аватарки
                                    avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri downloadUri) {
                                            // Сохранение URL-адреса в базе данных
                                            usersRef.child(user.getUid()).child("avatarUrl").setValue(downloadUri.toString());

                                            // установка аватарки
                                            binding.avatar.setImageURI(imageUri);
                                            Toast.makeText(requireContext(), "Avatar changed successfully",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Обработка ошибки загрузки аватарки
                                    // ну ошибка и ошибка, чо бухтеть то
                                }
                            });
                        }
                    }
                });

        // Слушатель для чекбокса 1
        binding.checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("music", isChecked);
            }
        });

        // Слушатель для чекбокса 2
        binding.checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("sport", isChecked);
            }
        });

        // Слушатель для чекбокса 3
        binding.checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("art", isChecked);
            }
        });

        // Слушатель для чекбокса 4
        binding.checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("movies", isChecked);
            }
        });

        // Слушатель для чекбокса 5
        binding.checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("education", isChecked);
            }
        });

        // Слушатель для чекбокса 6
        binding.checkBox6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("social", isChecked);
            }
        });

        // Слушатель для чекбокса 7
        binding.checkBox7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("culinary", isChecked);
            }
        });

        // Слушатель для чекбокса 8
        binding.checkBox8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateInterestInDatabase("technology", isChecked);
            }
        });

        // Инициализация ConnectivityManager
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Перенос прогресс бара на передний план
        binding.progressBarAccount.bringToFront();

        // Инициализация NetworkCallback
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isConnected = true;
                // Обработка доступности интернет-соединения
                // Удаление загрузочного кольца (если отображается)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressBarAccount.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                isConnected = false;
                // Обработка потери интернет-соединения
                // Отображение загрузочного кольца
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressBarAccount.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        return root;
    }

    private void updateInterestInDatabase(String interestKey, boolean isChecked) {
        usersRef.child(user.getUid()).child("interests").child(interestKey).setValue(isChecked)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Интерес успешно обновлен в базе данных
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Обработка ошибки обновления интереса в базе данных
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Регистрация NetworkCallback
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Отмена регистрации NetworkCallback
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}