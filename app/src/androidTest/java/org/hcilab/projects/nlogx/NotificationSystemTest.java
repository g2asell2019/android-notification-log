package org.hcilab.projects.nlogx;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.misc.DatabaseHelper;
import org.hcilab.projects.nlogx.service.BroadcastSender;
import org.hcilab.projects.nlogx.service.NotificationHandler;
import org.hcilab.projects.nlogx.service.NotificationListener;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class NotificationSystemTest {

    private Context context;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    @Mock
    private StatusBarNotification mockSbn;

    @Mock
    private Notification mockNotification;


    @Mock
    private BroadcastSender mockBroadcastSender; // Replace LocalBroadcastManager mock

    private NotificationHandler notificationHandler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Set up shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Const.PREF_TEXT, true);
        editor.putBoolean(Const.PREF_ONGOING, false);
        editor.apply();

        // Set up database
        dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.PostedEntry.TABLE_NAME);
        db.execSQL("DELETE FROM " + DatabaseHelper.RemovedEntry.TABLE_NAME);
        db.close();

        // Set up notification mocks
        when(mockSbn.getNotification()).thenReturn(mockNotification);
        when(mockSbn.getPackageName()).thenReturn("org.test.app");
        when(mockSbn.getPostTime()).thenReturn(System.currentTimeMillis());
        when(mockSbn.getId()).thenReturn(1);
        when(mockSbn.getTag()).thenReturn("test_tag");

        // Create a real Bundle for notification extras
        Bundle realExtras = new Bundle();
        realExtras.putCharSequence(Notification.EXTRA_TITLE, "Test notification");
        realExtras.putCharSequence(Notification.EXTRA_TEXT, "This is a test message");

        // Use reflection to set the extras field
        try {
            Field extrasField = Notification.class.getDeclaredField("extras");
            extrasField.setAccessible(true);
            extrasField.set(mockNotification, realExtras);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create notification handler with mock broadcast sender
        notificationHandler = new NotificationHandler(context, mockBroadcastSender);
    }
    @After
    public void tearDown() {
        // Clean up database
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + DatabaseHelper.PostedEntry.TABLE_NAME);
            db.execSQL("DELETE FROM " + DatabaseHelper.RemovedEntry.TABLE_NAME);
            db.close();
            dbHelper.close();
        }
    }


    @Test
    public void testHandlePosted_normalNotification_shouldLogAndBroadcast() throws Exception {
        // Arrange
        when(mockSbn.isOngoing()).thenReturn(false);
        // No need to set mockExtras since real bundle is already set in setup()

        // Act
        notificationHandler.handlePosted(mockSbn);

        // Assert
        // Verify data was saved to database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.PostedEntry.TABLE_NAME,
                new String[]{DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT},
                null, null, null, null, null);

        assertTrue(cursor.moveToFirst());
        String content = cursor.getString(0);
        assertNotNull(content);

        // Verify JSON structure
        JSONObject json = new JSONObject(content);
        assertEquals("org.test.app", json.getString("packageName"));

        cursor.close();
        db.close();
    }

    @Test
    public void testHandlePosted_ongoingNotification_shouldIgnore() {
        // Arrange
        when(mockSbn.isOngoing()).thenReturn(true);

        // Act
        notificationHandler.handlePosted(mockSbn);

        // Assert
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.PostedEntry.TABLE_NAME,
                new String[]{DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT},
                null, null, null, null, null);

        assertEquals(0, cursor.getCount());

        cursor.close();
        db.close();
    }

    @Test
    public void testHandleRemoved_normalNotification_shouldLogAndBroadcast() throws Exception {
        // Arrange
        when(mockSbn.isOngoing()).thenReturn(false);
        // No need to set mockExtras since real bundle is already set in setup()

        // Act
        notificationHandler.handleRemoved(mockSbn, 1); // 1 = REASON_CANCEL

        // Assert
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.RemovedEntry.TABLE_NAME,
                new String[]{DatabaseHelper.RemovedEntry.COLUMN_NAME_CONTENT},
                null, null, null, null, null);

        assertTrue(cursor.moveToFirst());
        String content = cursor.getString(0);
        assertNotNull(content);

        // Verify JSON structure
        JSONObject json = new JSONObject(content);
        assertEquals("org.test.app", json.getString("packageName"));
        assertEquals(1, json.getInt("removeReason"));

        cursor.close();
        db.close();
    }

    @Test
    public void testOngoingNotificationsWithPreferenceEnabled() {
        // Set preference to true for ongoing notifications
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Const.PREF_ONGOING, true);
        editor.apply();

        // Configure notification to be ongoing
        when(mockSbn.isOngoing()).thenReturn(true);

        // Act
        notificationHandler.handlePosted(mockSbn);

        // Assert - verify broadcast was sent, indicating notification was processed
        verify(mockBroadcastSender).sendBroadcast(any(Intent.class));

        // Verify database entry was created
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.PostedEntry.TABLE_NAME,
                new String[]{DatabaseHelper.PostedEntry.COLUMN_NAME_CONTENT},
                null, null, null, null, null);

        assertTrue("Database should contain the notification entry", cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testNotificationListener_lifecycleMethods() {
        // Create mock notification listener
        NotificationListener listener = spy(new NotificationListener());

        // Test onCreate
        listener.onCreate();

        // Test onListenerConnected
        listener.onListenerConnected();

        // Test notification handling
        StatusBarNotification mockSbn = mock(StatusBarNotification.class);
        when(mockSbn.getNotification()).thenReturn(mockNotification);

        listener.onNotificationPosted(mockSbn);
        listener.onNotificationRemoved(mockSbn);

        // Verify handling methods were called
        verify(listener, times(1)).onNotificationPosted(any(StatusBarNotification.class));
        verify(listener, times(1)).onNotificationRemoved(any(StatusBarNotification.class));
    }

    @Test
    public void testBroadcastSent_whenNotificationHandled() {
        // Arrange
        when(mockSbn.isOngoing()).thenReturn(false);
        // No need to set mockExtras since real bundle is already set in setup()

        // Act
        notificationHandler.handlePosted(mockSbn);

        // Assert
        // Verify broadcast was sent
        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(mockBroadcastSender).sendBroadcast(intentCaptor.capture());
        assertEquals(NotificationHandler.BROADCAST, intentCaptor.getValue().getAction());
    }
}