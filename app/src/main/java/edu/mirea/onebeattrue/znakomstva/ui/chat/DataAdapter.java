package edu.mirea.onebeattrue.znakomstva.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import edu.mirea.onebeattrue.znakomstva.R;
import edu.mirea.onebeattrue.znakomstva.databinding.ItemMessageBinding;

public class DataAdapter extends RecyclerView.Adapter<ViewHolder> {
    ArrayList<ChatMessage> messages;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference usersRef = database.getReference("users");

    public DataAdapter(Context context, ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        // установка аватарки (при большом количестве сообщений аватарки выставляются рандомно)
        usersRef.child(msg.getMessageUserId()).child("avatarUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String avatarUrl = snapshot.getValue(String.class);
                    if (avatarUrl != null && !avatarUrl.equals(""))
                        Picasso.get().load(avatarUrl).into(holder.binding.avatar);
                    else
                        Picasso.get().load(R.drawable.default_profile_picture).into(holder.binding.avatar);
                }
                else
                    Picasso.get().load(R.drawable.default_profile_picture).into(holder.binding.avatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // установка имени пользователя
        usersRef.child(msg.getMessageUserId()).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.getValue(String.class);
                    holder.binding.userNameTextView.setText(userName);
                }
                else {
                    holder.binding.userNameTextView.setText(msg.getMessageUser());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.messageTextView.setText(msg.getMessageText());
        // Получение экземпляра класса DateFormat
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        // Форматирование времени отправки сообщения в строку
        String msgTimeString = dateFormat.format(new Date(msg.getMessageTime()));
        holder.binding.dateTextView.setText(msgTimeString);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
