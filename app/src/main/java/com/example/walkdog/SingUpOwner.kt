package com.example.walkdog

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.walkdog.databinding.ActivitySingUpOwnerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class SingUpOwner : AppCompatActivity() {
    lateinit var binding : ActivitySingUpOwnerBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var storageRef = Firebase.storage
    private lateinit var uriImage: Uri
    private lateinit var uriCertification: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpOwnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                Picasso.get().load(it).fit().into(binding.imageView)
                if (it != null) {
                    uriImage = it
                }
            }
        )
        binding.imageView.setOnClickListener{
            galleryImage.launch("image/*")
        }

        binding.regulamin.setOnClickListener {
            val intent = Intent(this, Regulamin::class.java)
            startActivity(intent)
        }

        val certification = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                if (it != null) {
                    uriCertification = it
                }
            }
        )
        binding.tvSzczepienie.setOnClickListener{
            certification.launch("application/pdf")
        }

        binding.btnRejestracja.setOnClickListener {
            val name = binding.etImie.text.toString()
            val email = binding.etMail.text.toString()
            val password = binding.etHaslo.text.toString()
            val city = binding.etCity.text.toString()
            val opis = binding.etOpis.text.toString()
            if (::uriImage.isInitialized) {
                storageRef.getReference("image").child(System.currentTimeMillis().toString())
                    .putFile(uriImage)
                    .addOnSuccessListener { task ->
                        task.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {
                                val mapImage = mapOf(
                                    "url" to it.toString()
                                )
                                val databaseReference =
                                    FirebaseDatabase.getInstance().getReference("userImage")
                                databaseReference.child(mAuth.currentUser?.uid!!).setValue(mapImage)
                            }
                    }
            }
            if (::uriCertification.isInitialized) {
                storageRef.getReference("certification").child(System.currentTimeMillis().toString())
                    .putFile(uriCertification)
                    .addOnSuccessListener { task ->
                        task.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {
                                val mapImage = mapOf(
                                    "url" to it.toString()
                                )
                                val databaseReference =
                                    FirebaseDatabase.getInstance().getReference("userCertification")
                                databaseReference.child(mAuth.currentUser?.uid!!).setValue(mapImage)
                            }
                    }
            }
            if (binding.etCity.text.isNullOrEmpty() || binding.etHaslo.text.isNullOrEmpty() || binding.etImie.text.isNullOrEmpty() || binding.etMail.text.isNullOrEmpty()) {
                Toast.makeText(this@SingUpOwner, "uzupełnij pola", Toast.LENGTH_SHORT).show()
            }
            else if (binding.etHaslo.text.toString() != binding.etPowtorzHaslo.text.toString()) {
                Toast.makeText(this@SingUpOwner, "podane hasła różnią się", Toast.LENGTH_SHORT).show()
            }
            else if (binding.checkBox.isChecked != true){
                Toast.makeText(this@SingUpOwner, "zaakceptuj regulamin", Toast.LENGTH_SHORT).show()
            }
            else if (!::uriCertification.isInitialized){
                Toast.makeText(this@SingUpOwner, "dodaj zaświadczenie", Toast.LENGTH_SHORT).show()
            }
            else {
                singUp(name,email,password,city, opis)
            }

        }
    }

    private fun singUp (name:String, email:String, password: String, city:String, opis: String){

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, email, city, opis, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SingUpOwner, ChatListOwner::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SingUpOwner, "coś poszło nie tak", Toast.LENGTH_SHORT)
                        .show()
                }

            }
    }

    private fun addUserToDatabase(name:String, email:String, city:String, opis: String, uid:String){

        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("ownerUser").child(uid).setValue(UserOwnerClass(name,email,city,opis, uid))
    }

}