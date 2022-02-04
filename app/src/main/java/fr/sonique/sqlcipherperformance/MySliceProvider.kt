package fr.sonique.sqlcipherperformance



import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.SliceSpec
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.list
import androidx.slice.builders.row


/**
 * SliceProvider implementation that handles the different kind of Uri, creating and
 * holding a MySlice instance implementation depending on the type of URI
 *
 * This SliceProvider is defined inside the AndroidManifest.
 */

@RequiresApi(Build.VERSION_CODES.P)
class MySliceProvider : SliceProvider() {

    companion object {
        /**
         * The Slice authority as defined in the AndroidManifest
         */
        const val SLICE_AUTHORITY = "fr.sonique.sqlcipherperformance"
    }

    /**
     * Keep track of the created slices so when the slice calls "refresh" we don't create it again.
     */
    private val lastSlices = mutableMapOf<Uri, MySlice>()


    override fun onBindSlice(sliceUri: Uri?): Slice? {
        // When a new request is send to the SliceProvider of this app, this method is called
        // with the given slice URI.
        // Here you could directly handle the uri and create a new slice. But in order to make
        // the slice dynamic and better structured, we use the MySlice class.
        // Then we check if the new slice uri is the same as the last created slices (if any).
        // If there was none, we create a new instance of MySlice and return the Slice instance.
        if (sliceUri == null) {
            Log.i("SliceURI", " NULL")
            return null
        }
        Log.i("SliceURI", "${sliceUri.path}")


        return lastSlices.getOrPut(sliceUri) { createNewSlice(sliceUri) }.getSlice()
    }

    /**
     * This method is called when the Provider is first created, use it to initialize your code but keep it mind
     * that you should not do heavy tasks and block the thread.
     */
    override fun onCreateSliceProvider(): Boolean = true

    /**
     * Given the sliceUri create a matching MySlice instance.
     */
    private fun createNewSlice(sliceUri: Uri): MySlice {
        val context = requireNotNull(context) {
            "SliceProvider $this not attached to a context."
        }

        return when (sliceUri.path) {
            DeepLink.SLICE -> MySlice.Main(context, sliceUri)
            else -> MySlice.Unknown(context, sliceUri)
        }
    }
}