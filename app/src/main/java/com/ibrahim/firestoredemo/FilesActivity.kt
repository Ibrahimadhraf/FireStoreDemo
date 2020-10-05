package com.ibrahim.firestoredemo

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_files.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FilesActivity : AppCompatActivity() {
    val REQUEST_CODE_TIME_PICK = 0
    private var curFile: Uri? = null
    val imageRef = Firebase.storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)
        ivImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"

                startActivityForResult(it, REQUEST_CODE_TIME_PICK)
            }
        }
        btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }
        btnDownloadImage.setOnClickListener {
            downLoadImage("myImage")
        }
        btnDeleteImage.setOnClickListener {
            deleteImage("myImage")
        }
   listFiles()
    }
     private fun listFiles()= CoroutineScope(Dispatchers.IO).launch {
         try {
           val images=imageRef.child("images/").listAll().await()
             val imageUrls= mutableListOf<String>()
             for (image in images.items){
                 val url=image.downloadUrl.await()
                 imageUrls.add(url.toString())
             }
             withContext(Dispatchers.Main) {
                val  imageAdapter=ImageAdapter(imageUrls)
                 rvImages.apply {
                     adapter=imageAdapter
                     layoutManager=LinearLayoutManager(this@FilesActivity)
                 }
             }
         }catch (e:Exception){
             withContext(Dispatchers.Main) {
                 Toast.makeText(this@FilesActivity, e.message, Toast.LENGTH_SHORT).show()
             }
         }
     }
    private fun uploadImageToStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$fileName").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FilesActivity,
                        "Successfully Upload Image",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FilesActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
  private fun downLoadImage(fileName: String)= CoroutineScope(Dispatchers.Main).launch {
      try {

          val maxDownloadSize=5L*1024*1024
          val bytes=imageRef.child("images/$fileName").getBytes(maxDownloadSize).await()
          val bm=BitmapFactory.decodeByteArray(bytes ,0,bytes.size)
          withContext(Dispatchers.Main) {
              ivImage.setImageBitmap(bm)
          }
      } catch (e: Exception) {
          withContext(Dispatchers.Main) {
              Toast.makeText(this@FilesActivity, e.message, Toast.LENGTH_SHORT).show()
          }
      }
  }
    private fun deleteImage(fileName: String)= CoroutineScope(Dispatchers.IO).launch {
        try {

            imageRef.child("images/$fileName").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FilesActivity,"Successfully Delete Image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FilesActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TIME_PICK) {

            data?.data?.let {
                Log.d("Test","$it")
                curFile = it
                ivImage.setImageURI(curFile)
            }
        }
    }

}