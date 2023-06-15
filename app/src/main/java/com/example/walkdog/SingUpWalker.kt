package com.example.walkdog

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.walkdog.databinding.ActivitySingUpWalkerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class SingUpWalker : AppCompatActivity() {
    lateinit var binding : ActivitySingUpWalkerBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var storageRef = Firebase.storage
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpWalkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkBox.isChecked = true

        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent(), ActivityResultCallback {
                Picasso.get().load(it).fit().into(binding.imageView)
                if (it != null) {
                    uri = it
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

        binding.btnRejestracja.setOnClickListener {
            val name = binding.etName.text.toString()
            val surname = binding.etSurname.text.toString()
            val email = binding.etMail.text.toString()
            val password = binding.etPassword.text.toString()
            val city = binding.etCity.text.toString()
            val opis = binding.etOpis.text.toString()

            if (binding.etCity.text.isNullOrEmpty() || binding.etPassword.text.isNullOrEmpty() || binding.etName.text.isNullOrEmpty() || binding.etSurname.text.isNullOrEmpty()|| binding.etMail.text.isNullOrEmpty()) {
                Toast.makeText(this@SingUpWalker, "uzupełnij pola", Toast.LENGTH_SHORT).show()
            }
            else if (binding.etPassword.text.toString() != binding.etRepeatPassword.text.toString()) {
                Toast.makeText(this@SingUpWalker, "podane hasła różnią się", Toast.LENGTH_SHORT).show()
            }
            else if (binding.checkBox.isChecked != true){
                Toast.makeText(this@SingUpWalker, "zaakceptuj regulamin", Toast.LENGTH_SHORT).show()
            }
            else {
                singUp(name,surname, email,password,city, opis)
            }
            if (::uri.isInitialized) {
            storageRef.getReference("image").child(System.currentTimeMillis().toString())
                .putFile(uri)
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

        }
    }

    private fun singUp (name:String, surname:String, email:String, password: String, city: String, opis: String){

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, surname, email, city, opis, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SingUpWalker, ChatListWalker::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SingUpWalker, "uzupełnij pola", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(name:String, surname: String, email:String, city:String, opis: String, uid:String){

        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("walkerUser").child(uid).setValue(UserWalkerClass(name,surname,email, city, opis, uid))
    }
}