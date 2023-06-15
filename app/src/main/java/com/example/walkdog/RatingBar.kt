package com.example.walkdog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.walkdog.databinding.ActivityMainBinding
import com.example.walkdog.databinding.ActivityRatingBarBinding
import com.example.walkdog.databinding.ActivityRegulaminBinding
import com.google.firebase.database.*
import kotlin.math.roundToInt

class RatingBar : AppCompatActivity() {

    lateinit var binding: ActivityRatingBarBinding
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val ratingsRef: DatabaseReference = database.reference.child("oceny")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRatingBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ratingBar.rating = 2.5f
        binding.ratingBar.stepSize = 0.5f

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            Toast.makeText(this, "Rating:$rating", Toast.LENGTH_SHORT).show()
        }

        binding.btnZatwierdz.setOnClickListener {
            val intent = intent
            val uid = intent.getStringExtra("uid")
            val ocenianyUzytkownikId = uid.toString()
            val ocena = binding.ratingBar.rating.toDouble()

            ratingsRef.child(ocenianyUzytkownikId).push().setValue(ocena).addOnSuccessListener {
                ratingsRef.child(ocenianyUzytkownikId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var sum = 0.0
                        var count = 0
                        for (childSnapshot in dataSnapshot.children) {
                            val rating = childSnapshot.getValue(Double::class.java)
                            rating?.let {
                                sum += rating
                                count++
                            }
                        }
                        val averageRating = sum/count
                        val zaokraglij = (averageRating * 100.0).roundToInt() / 100.0
                        ratingsRef.child(ocenianyUzytkownikId).child("srednia").setValue(zaokraglij)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }

            onBackPressed()
        }
    }

}