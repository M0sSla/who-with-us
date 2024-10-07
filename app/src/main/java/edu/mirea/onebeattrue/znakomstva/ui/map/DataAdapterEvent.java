package edu.mirea.onebeattrue.znakomstva.ui.map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.mirea.onebeattrue.znakomstva.MainActivity;
import edu.mirea.onebeattrue.znakomstva.R;
import edu.mirea.onebeattrue.znakomstva.databinding.ItemEventBinding;
import edu.mirea.onebeattrue.znakomstva.ui.auth.Login;

public class DataAdapterEvent extends RecyclerView.Adapter<ViewHolderEvent> {
    private Context context;

    ArrayList<NewEvent> events;
    FirebaseUser user;
    FirebaseAuth auth;

    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference()
            .child("users");

    DatabaseReference eventsRef = FirebaseDatabase.getInstance("https://znakomstva3030-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference()
            .child("events");

    public DataAdapterEvent(Context context, ArrayList<NewEvent> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolderEvent onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolderEvent(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderEvent holder, @SuppressLint("RecyclerView") int position) {
        NewEvent event = events.get(position);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        DatabaseReference interestsRef = usersRef.child(user.getUid()).child("interests");

        // установка цвета мероприятия в зависимости от интересов пользователя
        String eventCategory = event.getEventCategory();

        // Установка иконки мероприятия
        switch (eventCategory) {
            case "music":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_music_event);
                break;
            case "sport":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_sport_event);
                break;
            case "art":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_art_event);
                break;
            case "movies":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_movie_event);
                break;
            case "education":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_education_event);
                break;
            case "social":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_social_event);
                break;
            case "culinary":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_culinary_event);
                break;
            case "technology":
                holder.binding.eventIcon.setImageResource(R.drawable.ic_technology_event);
                break;
            default:
                holder.binding.eventIcon.setImageResource(R.drawable.ic_no_category_event);
                break;
        }

        interestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Получение данных интересов пользователя
                    boolean greenColor = Boolean.TRUE.equals(dataSnapshot.child(eventCategory).getValue(Boolean.class));
                    // Установка цвета заднего фона в зависимости от переменной greenColor
                    if (greenColor) {
                        int colorBrighterPaleGreen = Color.parseColor("#B0E8B2");
                        holder.itemView.setBackgroundColor(colorBrighterPaleGreen);
                    } else {
                        holder.itemView.setBackgroundColor(Color.WHITE);
                    }
                } else {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок чтения из базы данных
            }
        });

        // Установка количества посетителей
        String numOfVisitors = String.valueOf(event.getEventNumberOfVisitors());
        holder.binding.visitors.setText("Number of visitors: " + numOfVisitors);

        holder.binding.eventTitle.setText(event.getEventName());
        holder.binding.eventDescription.setText(event.getEventDescription());
        holder.binding.eventTime.setText(event.getEventTime());
        holder.binding.eventDate.setText(event.getEventDate());
        holder.binding.eventLocation.setText(event.getEventPlace());

        // Установка слушателя для кнопки присоединения к мероприятию
        holder.binding.joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь будет код для сохранения мероприятия в календаре
                saveEventToCalendar(event);
                event.addVisitor(user.getUid());
                eventsRef.child(event.getEventId()).child("eventVisitors").setValue(event.getEventVisitors());
                eventsRef.child(event.getEventId()).child("eventNumberOfVisitors").setValue(event.getEventNumberOfVisitors());
                notifyDataSetChanged();
            }
        });

        // Установка слушателя для кнопки отсоединения от мероприятия
        holder.binding.leaveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event.removeVisitor(user.getUid());
                eventsRef.child(event.getEventId()).child("eventVisitors").setValue(event.getEventVisitors());
                eventsRef.child(event.getEventId()).child("eventNumberOfVisitors").setValue(event.getEventNumberOfVisitors());
                notifyDataSetChanged();
            }
        });

        // Установка слушателя кликов для кнопки удаления
        holder.binding.deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создание диалогового окна для подтверждения удаления
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Подтверждено удаление - выполнение необходимых действий здесь
                        events.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, events.size());

                        // Удаление мероприятия из базы данных
                        eventsRef.child(event.getEventId()).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Успешно удалено
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Обработка ошибки удаления
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // изменение времени
        holder.binding.eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Обработка выбранного времени
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        holder.binding.eventTime.setText(selectedTime);
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        // изменение даты
        holder.binding.eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Обработка выбранной даты
                        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", dayOfMonth, month + 1, year % 100);
                        holder.binding.eventDate.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });

        // Установка слушателя кликов для кнопки редактирования
        if (event.isEditMode()) {
            // Установка стиля кнопки
            holder.binding.editEventButton.setBackgroundColor(Color.BLACK);
            holder.binding.editEventButton.setTextColor(Color.WHITE);
            holder.binding.editEventButton.setText(R.string.edit);

            holder.binding.editEventButton.setStrokeWidth(0);


            holder.binding.editEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // включение возможности редактирования event'a
                    // editButtonInEditMode = false;
                    event.setEditMode(false);
                    holder.binding.eventTitle.setFocusableInTouchMode(true);
                    holder.binding.eventDescription.setFocusableInTouchMode(true);
                    holder.binding.eventTime.setEnabled(true);
                    holder.binding.eventDate.setEnabled(true);
                    holder.binding.eventLocation.setFocusableInTouchMode(true);
                    holder.binding.editEventButton.setText("Save");
                    notifyDataSetChanged();
                }
            });
        } else {
            // Установка стиля кнопки
            holder.binding.editEventButton.setBackgroundColor(Color.WHITE);
            holder.binding.editEventButton.setTextColor(Color.BLACK);
            holder.binding.editEventButton.setText(R.string.save);

            // включение возможности редактирования event'a
            holder.binding.eventTitle.setFocusableInTouchMode(true);
            holder.binding.eventDescription.setFocusableInTouchMode(true);
            holder.binding.eventTime.setEnabled(true);
            holder.binding.eventDate.setEnabled(true);
            holder.binding.eventLocation.setFocusableInTouchMode(true);

            // Перевод dp в px
            int strokeWidthDp = 2;
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int strokeWidthPx = Math.round(strokeWidthDp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

            holder.binding.editEventButton.setStrokeWidth(strokeWidthPx);
            holder.binding.editEventButton.setStrokeColor(ColorStateList.valueOf(Color.BLACK));


            holder.binding.editEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // выключение возможности редактирования event'a
                    // editButtonInEditMode = true;
                    event.setEditMode(true);
                    holder.binding.eventTitle.setFocusable(false);
                    holder.binding.eventDescription.setFocusable(false);
                    holder.binding.eventTime.setEnabled(false);
                    holder.binding.eventDate.setEnabled(false);
                    holder.binding.eventLocation.setFocusable(false);
                    holder.binding.editEventButton.setText("Edit");

                    // редактирование информации в бд и в мероприятии

                    // Проверка, что поле названия мероприятия не пустое
                    if (!(holder.binding.eventTitle.getText().toString().trim().length() == 0)) {
                        eventsRef.child(event.getEventId()).child("eventName").setValue(holder.binding.eventTitle.getText().toString().trim());
                        event.setEventName(holder.binding.eventTitle.getText().toString().trim());
                    }

                    // Проверка, что поле описания мероприятия не пустое
                    if (!(holder.binding.eventDescription.getText().toString().trim().length() == 0)) {
                        eventsRef.child(event.getEventId()).child("eventDescription").setValue(holder.binding.eventDescription.getText().toString().trim());
                        event.setEventDescription(holder.binding.eventDescription.getText().toString().trim());
                    }

                    // Проверка, что поле времени мероприятия не пустое
                    if (!holder.binding.eventTime.getText().toString().equals("Event time")) {
                        eventsRef.child(event.getEventId()).child("eventTime").setValue(holder.binding.eventTime.getText().toString().trim());
                        event.setEventTime(holder.binding.eventTime.getText().toString().trim());
                    }

                    // Проверка, что поле даты мероприятия не пустое
                    if (!holder.binding.eventDate.getText().toString().equals("Event date")) {
                        eventsRef.child(event.getEventId()).child("eventDate").setValue(holder.binding.eventDate.getText().toString().trim());
                        event.setEventDate(holder.binding.eventDate.getText().toString().trim());
                    }

                    // Проверка, что поле места мероприятия не пустое
                    if (!(holder.binding.eventLocation.getText().toString().trim().length() == 0)) {
                        eventsRef.child(event.getEventId()).child("eventPlace").setValue(holder.binding.eventLocation.getText().toString().trim());
                        event.setEventPlace(holder.binding.eventLocation.getText().toString().trim());
                    }

                    notifyDataSetChanged();
                }
            });
        }

        if (event.getEventVisitors().contains(user.getUid())) {
            holder.binding.joinEventButton.setVisibility(View.GONE);
            holder.binding.leaveEventButton.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.joinEventButton.setVisibility(View.VISIBLE);
            holder.binding.leaveEventButton.setVisibility(View.GONE);
        }

        if (user.getUid().equals(event.getEventUser())) {
            holder.binding.editEventButton.setVisibility(View.VISIBLE);
            holder.binding.deleteEventButton.setVisibility(View.VISIBLE);
        }
        else {
            holder.binding.editEventButton.setVisibility(View.GONE);
            holder.binding.deleteEventButton.setVisibility(View.GONE);
        }
    }

    private void saveEventToCalendar(NewEvent event) {
        String eventDateString = event.getEventDate(); // Получаем строку с датой в формате дд/мм/гг
        String eventTimeString = event.getEventTime(); // Получаем строку с временем в формате чч:мм

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Date eventDate = dateFormat.parse(eventDateString); // Преобразуем строку даты в объект Date
            Date eventTime = timeFormat.parse(eventTimeString); // Преобразуем строку времени в объект Date

            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(eventDate); // Устанавливаем дату начала мероприятия
            beginTime.set(Calendar.HOUR_OF_DAY, eventTime.getHours()); // Устанавливаем час начала мероприятия
            beginTime.set(Calendar.MINUTE, eventTime.getMinutes()); // Устанавливаем минуты начала мероприятия

            int reminderMinutes = 24 * 60;

            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis()) // Устанавливаем время начала мероприятия
                    .putExtra(CalendarContract.Events.TITLE, event.getEventName()) // Устанавливаем название мероприятия
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getEventPlace()) // Устанавливаем место проведения мероприятия
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.getEventDescription()) // Устанавливаем описание мероприятия
                    .putExtra(CalendarContract.Reminders.MINUTES, reminderMinutes)
                    .putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);// Устанавливаем напоминание о мероприятии за день до его проведения

            context.startActivity(intent);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}