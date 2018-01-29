package com.alli5723.android.quickshot.utils

import android.Manifest
import android.app.Activity
import android.content.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.os.Build
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Log
import com.alli5723.android.quickshot.MainActivity
import com.alli5723.android.quickshot.R
import com.alli5723.android.quickshot.R.string.request_permission
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Created by omo_lanke on 28/01/2018.
 */

class Utils{
    fun checkPermission(
            context: Context): Boolean {
        val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("QuickShot needs your permission to use camera and storage", context)

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    context as Activity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                }
                return false
            } else {
                return true
            }

        } else {
            return true
        }
    }

    fun showDialog(msg: String, context: Context) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Permission necessary")
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(context as Activity,
                            arrayOf(Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE),
                            MainActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                })
        val alert = alertBuilder.create()
        alert.show()
    }

    companion object {
        val NOT_UPLOADED = 0
        var UPLOADED = 1
        val FILE_IMAGE = "image"
        var FILE_VIDEO = "video"

        fun showErrorDialog(msg: String, context: Context) {
            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle("Error Occured")
            alertBuilder.setMessage(msg)
            alertBuilder.setPositiveButton(android.R.string.yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
            val alert = alertBuilder.create()
            alert.show()
        }

        fun insertImage(cr: ContentResolver, source: Bitmap?, title: String, description: String): String? {

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, title)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
            values.put(MediaStore.Images.Media.DESCRIPTION, description)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            // Add the date meta data to ensure the image is added at the front of the gallery
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            var url: Uri? = null
            var stringUrl: String? = null    /* value to be returned */

            try {
                url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (source != null) {
                    val imageOut = cr.openOutputStream(url!!)
                    try {
                        source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
                    } finally {
                        imageOut!!.close()
                    }

                    val id = ContentUris.parseId(url)
                    // Wait until MINI_KIND thumbnail is generated.
                    val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
                            MediaStore.Images.Thumbnails.MINI_KIND, null)
                    // This is for backward compatibility.
                    storeThumbnail(cr, miniThumb, id, 50f, 50f, MediaStore.Images.Thumbnails.MICRO_KIND)
                } else {
                    cr.delete(url!!, null, null)
                    url = null
                }
            } catch (e: Exception) {
                if (url != null) {
                    cr.delete(url, null, null)
                    url = null
                }
            }

            if (url != null) {
                stringUrl = url.toString()
            }
            return stringUrl
        }

        fun storeThumbnail(
                cr: ContentResolver,
                source: Bitmap,
                id: Long,
                width: Float,
                height: Float,
                kind: Int): Bitmap? {

            // create the matrix to scale it
            val matrix = Matrix()

            val scaleX = width / source.width
            val scaleY = height / source.height

            matrix.setScale(scaleX, scaleY)

            val thumb = Bitmap.createBitmap(source, 0, 0,
                    source.width,
                    source.height, matrix,
                    true
            )

            val values = ContentValues(4)
            values.put(MediaStore.Images.Thumbnails.KIND, kind)
            values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
            values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
            values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)

            val url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)

            try {
                val thumbOut = cr.openOutputStream(url!!)
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
                thumbOut!!.close()
                return thumb
            } catch (ex: FileNotFoundException) {
                return null
            } catch (ex: IOException) {
                return null
            }

        }

        fun rotate(b: Bitmap, displayRotation: Int): Bitmap {
            var b = b
            var m: Matrix? = null
            var degrees = 0
            val width = b!!.width.toFloat()
            val height = b.height.toFloat()
            if (displayRotation == 0 && width > height) {
                degrees = 90
            } else if (displayRotation == 3 && width > height) {
                //            degrees = 180;
            }
            if (degrees != 0 && b != null) {
                if (m == null) {
                    m = Matrix()
                }
                m.setRotate(degrees.toFloat(),
                        width / 2, height / 2)
                try {
                    val b2 = Bitmap.createBitmap(
                            b, 0, 0, b.width, b.height, m, true)
                    if (b != b2) {
                        b.recycle()
                        b = b2
                    }
                } catch (ex: OutOfMemoryError) {
                    // We have no memory to rotate. Return the original bitmap.
                    Log.e("Utils", "Got oom exception ", ex)
                    return b
                }

            }
            return b
        }
    }
}