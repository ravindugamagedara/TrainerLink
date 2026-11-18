package Util;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.trainerlink.R;

import java.util.Calendar;

public class NotificationHelper {

    private static final String CHANNEL_ID = "trainerlink_booking_channel";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for booking updates");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showBookingNotification(Context context, String title, String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void scheduleSessionReminder(Context context,
                                               String bookingTitle,
                                               String bookingDate,
                                               String bookingTime) {

        try {
            String[] dateParts = bookingDate.split("/");
            String[] timeParts = bookingTime.split(":");

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int year = Integer.parseInt(dateParts[2]);

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar sessionCalendar = Calendar.getInstance();
            sessionCalendar.set(Calendar.YEAR, year);
            sessionCalendar.set(Calendar.MONTH, month);
            sessionCalendar.set(Calendar.DAY_OF_MONTH, day);
            sessionCalendar.set(Calendar.HOUR_OF_DAY, hour);
            sessionCalendar.set(Calendar.MINUTE, minute);
            sessionCalendar.set(Calendar.SECOND, 0);
            sessionCalendar.set(Calendar.MILLISECOND, 0);

            long reminderTime = sessionCalendar.getTimeInMillis() - (60 * 60 * 1000);

            long currentTime = System.currentTimeMillis();

            if (reminderTime <= currentTime) {
                reminderTime = currentTime + (2 * 60 * 1000);
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("title", "Upcoming Training Session");
            intent.putExtra("message", bookingTitle + " starts at " + bookingTime + ".");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) System.currentTimeMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}