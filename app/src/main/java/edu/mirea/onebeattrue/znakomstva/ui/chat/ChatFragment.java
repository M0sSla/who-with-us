package edu.mirea.onebeattrue.znakomstva.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import edu.mirea.onebeattrue.znakomstva.R;
import edu.mirea.onebeattrue.znakomstva.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isConnected;



    private String username;
    private String avatarUrl = "";
    private String userId;

    private FragmentChatBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference messagesRef = database.getReference("messages");
    DatabaseReference usersRef = database.getReference("users");

    ArrayList<ChatMessage> messages = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChatViewModel chatViewModel =
                new ViewModelProvider(this).get(ChatViewModel.class);

        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        // потеря фокуса ввода сообщения при нажатии кнопки назад
        binding.messageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // Пользователь нажал кнопку "назад"
                    binding.messageEditText.clearFocus();
                    return true;
                }
                return false;
            }
        });

        // установка имени пользователя и аватарки
        //------------------------------------------------------------------------------------------
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        DatabaseReference userIdRef = usersRef.child(user.getUid());

        // получение имени пользователя
        userIdRef.child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Получение имени пользователя
                if (dataSnapshot.exists())
                    username = dataSnapshot.getValue(String.class);
                else
                    username = user.getEmail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок чтения из базы данных
            }
        });

        if (Objects.equals(username, ""))
            username = user.getEmail();

        // получение аватарки
        userIdRef.child("avatarUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Получение имени пользователя
                if (dataSnapshot.exists())
                    avatarUrl = dataSnapshot.getValue(String.class);
                else
                    avatarUrl = "";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок чтения из базы данных
            }
        });

        //------------------------------------------------------------------------------------------

        binding.messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DataAdapter dataAdapter = new DataAdapter(getContext(), messages);
        binding.messageRecyclerView.setAdapter(dataAdapter);

        // Добавляем слушатель событий
        messagesRef.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Получаем новое сообщение
                ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                messages.add(message);
                dataAdapter.notifyDataSetChanged();
                binding.messageRecyclerView.smoothScrollToPosition(messages.size() - 1); // вылет после отправки сообщения после перезахода на фрагмент
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Обработка изменения сообщения
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Обработка удаления сообщения
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Обработка перемещения сообщения
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обработка нажатия на кнопку
                String message = binding.messageEditText.getText().toString();
                sendMessage(message);
            }
        });



        // Инициализация ConnectivityManager
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Перенос прогресс бара на передний план
        binding.progressBarChat.bringToFront();

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
                        binding.progressBarChat.setVisibility(View.GONE);
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
                        binding.progressBarChat.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        return root;
    }

    private void sendMessage(String message) {
        // Проверка, что сообщение не пустое
        if (!TextUtils.isEmpty(message) && message.trim().length() != 0) {
            // Отправка сообщения в Firebase
            String messageId = messagesRef.push().getKey();
            
            ChatMessage newMessage = new ChatMessage(message.trim(), username, userId);
            newMessage.setAvatarUrl(avatarUrl);
            messagesRef.child(messageId).setValue(newMessage);

            // Очистка поля ввода сообщения
            binding.messageEditText.setText("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

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
