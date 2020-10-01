package com.ibrahim.firestoredemo

import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() ,EasyPermissions.PermissionCallbacks{
    val recorder = MediaRecorder()
    private val personCollectionRef = Firebase.firestore.collection("Persons")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

      //  audioManager.isMicrophoneMute = true
        val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isMicrophoneMute = true
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
               requestPermission()
        setContentView(R.layout.activity_main)

        btnUploadData.setOnClickListener {
          val person=getOldPerson()
            savePerson(person)
        }
        btnRetrieveData.setOnClickListener {
            retrieveData()
        }
        btnUpdatePerson.setOnClickListener {
            val oldPerson=getOldPerson()
            Log.d("Test", "${oldPerson}")
            val newPerson=getNewPersonMap()
            Log.d("Test", "${newPerson}")
            updatePerson(oldPerson, newPerson)
        }
        btnDeletePerson.setOnClickListener {
            val person=getOldPerson()
            deletePerson(person)
        }
        btnWatchWrite.setOnClickListener {
            changeName("7myXmgiboOM0zW0aETVT" ,"Ashraf" ,"Ibrahim")
        }
        btnDoTransAction.setOnClickListener {
            updateBirthDate("7myXmgiboOM0zW0aETVT")
        }
       // subscribeToRealTimeTimeUpdates()
    }


    private fun savePerson(persons: Persons) = CoroutineScope(Dispatchers.IO).launch {
        persons?.let {
            try {
                personCollectionRef.add(persons).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully  Save Data", Toast.LENGTH_SHORT)
                        .show()
                }


            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun getOldPerson():Persons{
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val age = etAge.text.toString().toInt()

        return Persons(firstName, lastName, age)
    }
    private fun updatePerson(person: Persons, newPersonMap: Map<String, Any>)= CoroutineScope(
        Dispatchers.IO
    ).launch {
        val personQuery = personCollectionRef
        .whereEqualTo("firstName", person.firstName)
        .whereEqualTo("lastName", person.lastName)
        .whereEqualTo("age", person.age)
        .get()
        .await()
        Log.d("Test", "${personQuery.documents}")
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    Log.d("Test", "${personQuery.documents}")
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }else{
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "No person matches the query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePerson(person: Persons)= CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        Log.d("Test", "${personQuery.documents}")
        if(personQuery.documents.isNotEmpty()) {
            for(document in personQuery) {
                try {
                    //personCollectionRef.document(document.id).update("age", newAge).await()
                    Log.d("Test", "${personQuery.documents}")
                //  personCollectionRef.document(document.id).delete().await()
                    personCollectionRef.document(document.id).update(
                        mapOf(
                            "firstName" to FieldValue.delete()
                        )
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }else{
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "No person matches the query", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeName(personId:String,
                           newFirstName:String,
                           newLastName:String)= CoroutineScope(Dispatchers.IO).launch {
        try {
           Firebase.firestore.runBatch{batch->
           val personRef=personCollectionRef.document(personId)
               batch.update(personRef ,"firstName",newFirstName)
               batch.update(personRef ,"lastName",newLastName)
           }.await()

        }catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = etNewFirstName.text.toString()
        val lastName = etNewLastName.text.toString()
        val age = etNewAge.text.toString()
        val map = mutableMapOf<String, Any>()
        if(firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if(lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if(age.isNotEmpty()) {
            map["age"] = age.toInt()
        }
        return map
    }
    private fun updateBirthDate(personId: String)= CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction {transition->
                val personRef=personCollectionRef.document(personId)
                val person=transition.get(personRef)
                val newAge=person["age"] as Long+1
                transition.update(personRef ,"age" ,newAge)

            }.await()
        }catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
private fun retrieveData()= CoroutineScope(Dispatchers.IO).launch{
  //  val fromAge = etFrom.text.toString().toInt()
  //  val toAge = etTo.text.toString().toInt()
        try {
            val querySnapshot=personCollectionRef

                .get()
                .await()
            Log.d("Test", "${querySnapshot.documents}")
            val sb=StringBuilder()
            for (document in querySnapshot.documents){
                val person=document.toObject<Persons>()
                sb.append("$person\n")
                withContext(Dispatchers.Main){
                    tvPersons.text=sb.toString()
                }
            }

        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
    }



}
    private fun subscribeToRealTimeTimeUpdates(){
        personCollectionRef.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb=StringBuilder()
                for (document in it){
                    val person=document.toObject<Persons>()
                    sb.append("$person\n")
                }
                tvPersons.text=sb.toString()
            }

        }

    }




    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermission()
        }
    }


    private fun requestPermission() {
    if(hasLocationPermission(this)){
        return
    }
        EasyPermissions.requestPermissions(
            this,
            "You need to Accept Audio permission  to use this app",
            REQUEST_Audio_PERMISSION,
            android.Manifest.permission.RECORD_AUDIO,


            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}