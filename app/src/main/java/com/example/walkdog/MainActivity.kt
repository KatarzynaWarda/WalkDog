 package com.example.walkdog

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.walkdog.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

 class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
     private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPref = getSharedPreferences("myPrefFile", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val email = sharedPref.getString("EMAIL", null)
        val password = sharedPref.getString("PASSWORD", null)

        if (email != null && password != null) {
            binding.etLogin.setText(email)
            binding.etHaslo.setText(password)
        }


        supportActionBar?.hide()
        mAuth = FirebaseAuth.getInstance()

        binding.btnRejWalker.setOnClickListener {
            val intent = Intent(this, SingUpWalker::class.java)
            startActivity(intent)
        }

        binding.btnRejWl.setOnClickListener {
            val intent = Intent(this, SingUpOwner::class.java)
            startActivity(intent)
        }

        binding.btnZaloguj.setOnClickListener {
            val email = binding.etLogin.text.toString()
            val password = binding.etHaslo.text.toString()
            fun showConfirmationDialog() {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Potwierdzenie")
                builder.setMessage("Czy chcesz zapisać hasło?")

                builder.setPositiveButton("Tak") { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()

                    val email2 = binding.etLogin.text.toString()
                    val password2 = binding.etHaslo.text.toString()
                    editor.putString("EMAIL", email2)
                    editor.putString("PASSWORD", password2)
                    editor.apply()
                    login(email, password)
                }

                builder.setNegativeButton("Nie") { dialog: DialogInterface, _: Int ->
                    editor.remove("EMAIL")
                    editor.remove("PASSWORD")
                    editor.apply()
                    dialog.dismiss()
                    login(email, password)
                }
                val dialog = builder.create()
                dialog.show()
            }
            showConfirmationDialog()
        }
    }

     private fun login(email: String, password: String) {
         mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this) { task ->
                 if (task.isSuccessful) {
                     val currentUser = mAuth.currentUser
                     val userRef = FirebaseDatabase.getInstance().getReference("walkerUser/${currentUser?.uid}")
                     userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                         override fun onDataChange(snapshot: DataSnapshot) {
                             if (snapshot.exists()) {
                                 val intent = Intent(this@MainActivity, ChatListWalker::class.java)
                                 finish()
                                 startActivity(intent)
                             } else {
                                 val intent = Intent(this@MainActivity, ChatListOwner::class.java)
                                 finish()
                                 startActivity(intent)
                             }
                         }
                         override fun onCancelled(error: DatabaseError) {
                             Toast.makeText(this@MainActivity, "Błąd logowania: ${error.message}", Toast.LENGTH_SHORT).show()
                         }
                     })
                 } else {
                     Toast.makeText(this@MainActivity, "Błędny login lub hasło", Toast.LENGTH_SHORT).show()
                 }
             }
    }
}