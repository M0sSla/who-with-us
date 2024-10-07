package edu.mirea.onebeattrue.znakomstva.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import edu.mirea.onebeattrue.znakomstva.MainActivity;
import edu.mirea.onebeattrue.znakomstva.databinding.FragmentMapBinding;
import edu.mirea.onebeattrue.znakomstva.ui.auth.Login;
import edu.mirea.onebeattrue.znakomstva.ui.chat.ChatMessage;
import edu.mirea.onebeattrue.znakomstva.ui.chat.DataAdapter;

public class MapFragment extends Fragment {
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isConnected;

    private FragmentMapBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference myRef = database.getReference("events");

    ArrayList<NewEvent> events = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel mapViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DataAdapterEvent dataAdapterEvent = new DataAdapterEvent(getContext(), events);
        binding.eventRecyclerView.setAdapter(dataAdapterEvent);

        // Добавляем слушатель событий
        myRef.addChildEventListener(new ChildEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Получаем нове мероприятие
                NewEvent event = dataSnapshot.getValue(NewEvent.class);
                event.setEditMode(true);
                events.add(event);
                dataAdapterEvent.notifyDataSetChanged();
                // binding.eventRecyclerView.smoothScrollToPosition(events.size()); // вылет с ошибкой после перехода с другого фрагмента
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Обработка изменения мероприятия
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Обработка удаления мероприятия
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Обработка перемещения мероприятия
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });

        binding.addNewEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewEvent();
            }
        });


        // Инициализация ConnectivityManager
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Перенос прогресс бара на передний план
        binding.progressBarMap.bringToFront();

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
                        binding.progressBarMap.setVisibility(View.GONE);
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
                        binding.progressBarMap.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        return root;
    }

    private void addNewEvent() {
        Intent intent = new Intent(getContext(), EventActivity.class);
        startActivity(intent);
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