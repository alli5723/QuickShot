/*
 * Copyright (c) 2017 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.alli5723.android.quickshot

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_preview.*
import java.io.File

class PreviewActivity : AppCompatActivity() {
    private var selectedPhotoPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

//        val launchCamera = intent.getBooleanExtra(MainActivity.INTENT_LAUNCH_CAMERA)
        if(intent.getBooleanExtra(MainActivity.INTENT_LAUNCH_CAMERA, false)){
            takePictureWithCamera()
        }else if (null != intent.getStringExtra(INTENT_IMAGE_URI)){
            Picasso.with(this).load(intent.getStringExtra(INTENT_IMAGE_URI)).fit().into(image_preview);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //setImageViewWithImage()
            Log.e("Preview", "Preview URL is " + selectedPhotoPath)
            Log.e("Preview", "Preview data is " + data?.data)
            Picasso.with(this).load(data?.data).fit().into(image_preview);
        }
    }

    private fun takePictureWithCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

//        val imagePath = File(filesDir, "images")
        val image_name = "QuickShot_" + System.currentTimeMillis() + ".jpg"

        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "QuickShot");
        val newFile = File(mediaStorageDir.path, image_name)

//        val newFile = File(imagePath, image_name)
        if (newFile.exists()){
            newFile.delete()
        }else{
            newFile.parentFile.mkdirs()
        }
////        selectedPhotoPath = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", newFile)
//        selectedPhotoPath =  Uri.parse(newFile.path)
//        captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedPhotoPath)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        } else {
//            val clip = ClipData.newUri(contentResolver, "A photo", selectedPhotoPath)
//            captureIntent.clipData = clip
//            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        }
        startActivityForResult(captureIntent, TAKE_PHOTO_REQUEST_CODE)
    }

    companion object {
        const private val TAKE_PHOTO_REQUEST_CODE = 1
        const private val INTENT_IMAGE_URI = "image_data"
    }
}
