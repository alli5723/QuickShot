package com.alli5723.android.quickshot

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.CursorLoader
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.alli5723.android.quickshot.adapters.ImageListAdapter

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import android.app.Activity
import android.support.v4.app.ActivityCompat
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import com.alli5723.android.quickshot.utils.Utils
import android.widget.Toast
import android.content.pm.PackageManager
import android.util.Log


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, ImageListAdapter.OnItemClickListener {
    override fun onItemClick(uri: String, position: Int) {
        val intent = Intent(applicationContext, PreviewActivity::class.java)
        intent.putExtra(INTENT_IMAGE_URI, uri)
        intent.setData(Uri.parse(uri))
        startActivity(intent)
    }

    override fun onShareClicked(v: View, position: Int) {
//        TODO("launch share page")
        Log.e("main", "Share")
    }

    private var adapter: ImageListAdapter = ImageListAdapter(null)
    private var cursor: Cursor? = null

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")

        val where = MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE 'QuickShot_%'"

        when (id) {
            ID_GALLERY_LOADER ->
                //                Uri galleryQueryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                return CursorLoader(this,
                        queryUri,
                        PROJECTION,
                        "$where AND ($selection)",
                        null,
                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
            else -> throw RuntimeException("Loader not implemented for " + id)
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        progress.setVisibility(View.GONE)
        cursor = data
        Log.e("DEBUG", "datacount: " + data!!.getCount())
        if (data.getCount() != 0) {
            no_shot.setVisibility(View.GONE)
            adapter?.swapCursor(data)
            progress.setVisibility(View.GONE)
        }else if(data.count == 0){
            no_shot.setVisibility(View.VISIBLE)
            Snackbar.make(progress, "There are no shots created yet using this App.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        adapter.setOnItemClickListener(this)
        image_list.adapter = adapter
        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        image_list.layoutManager = layoutManager

        val utils = Utils()

        if (utils.checkPermission(this)) {
            // do your stuff..
            getSupportLoaderManager().initLoader(ID_GALLERY_LOADER, null, this)
        }

        fab.setOnClickListener { _ -> //view ->
            startActivity(CameraShotPreview(true))
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        // Example of a call to a native method
//        no_shot.text = stringFromJNI()
    }

    fun CameraShotPreview(camera: Boolean): Intent {
        return Intent(this, ShotActivity::class.java).apply {
            putExtra(INTENT_LAUNCH_CAMERA, camera)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("Main", "onResume")
        getSupportLoaderManager().restartLoader(ID_GALLERY_LOADER, null, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSupportLoaderManager().initLoader(ID_GALLERY_LOADER, null, this)
            } else {
                Snackbar.make(progress, "Permissions are needed to use the App.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions,
                    grantResults)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings ->
                launchSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchSettings(): Boolean{
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)
        return true
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        val PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.DATA)
        val INTENT_LAUNCH_CAMERA = "launch_camera"
        val ID_GALLERY_LOADER = 1;
        val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
        const private val INTENT_IMAGE_URI = "image_data"

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
