package fr.sonique.sqlcipherperformance


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.SliceAction
import androidx.slice.builders.header
import androidx.slice.builders.list
import androidx.slice.builders.row


/**
 * Base class that defines a Slice for the app.
 *
 * Every slice implementation should extend this class and implement getSlice method.
 */
abstract class MySlice(val context: Context, val sliceUri: Uri) {

    /**
     * Main thread handler to execute tasks that requires Android Main Thread
     */
    protected val handler = Handler(Looper.getMainLooper())

    /**
     * @return the specific slice implementation to be used by SliceProvider
     */
    abstract fun getSlice(): Slice

    /**
     * Call refresh to notify the SliceProvider to load again.
     */
    protected fun refresh() {
        context.contentResolver.notifyChange(sliceUri, null)
    }

    /**
     * Utility method to create a SliceAction that launches the main activity.
     */
    protected fun createActivityAction(action: String): SliceAction {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("app://sqlcipherperformance.sonique.fr/open?feature=$action")
        return SliceAction.create(
            PendingIntent.getActivity(context, 0, intent, 0),
            IconCompat.createWithResource(context, R.mipmap.ic_launcher),
            ListBuilder.SMALL_IMAGE,
            "START TEST"
        )
    }

    /**
     * Default Slice when the uri could not be handled.
     */
    class Unknown(context: Context, sliceUri: Uri) : MySlice(context, sliceUri) {

        override fun getSlice(): Slice = list(context, sliceUri, ListBuilder.INFINITY) {
            // Adds a row to the slice
            row {
                // Set the title of the row
                title = "Unknown"
                // Defines the action when slice is clicked.
                primaryAction = createActivityAction("")
            }

            // Mark the slice as error type slice.
            setIsError(true)
        }
    }


    /**
     * Default Slice when the uri is handled.
     *  adb shell am start -a android.intent.action.VIEW -d
    slice-content://fr.sonique.sqlcipherperformance/slice
     *
     *
     */
    class Main(context: Context, sliceUri: Uri) : MySlice(context, sliceUri) {

        override fun getSlice(): Slice = list(context, sliceUri, ListBuilder.INFINITY) {
            header {
                title = "SQL Cipher Performance"
                subtitle = "What action do you want to start?"
                // Defines the primary action when slice is clicked
                primaryAction = createActivityAction("")
            }
            // Adds a row to the slice
            row {
                // Set the title of the row
                title = "Select test"
                // Defines the action when slice is clicked.
                primaryAction = createActivityAction("select")
            }
            // Adds a row to the slice
            row {
                // Set the title of the row
                title = "Insert test"
                // Defines the action when slice is clicked.
                primaryAction = createActivityAction("insert")
            }
        }
    }
}