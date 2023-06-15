package com.example.walkdog

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walkdog.databinding.ActivityChatListWalkerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ChatListWalker : AppCompatActivity() {
    private lateinit var binding: ActivityChatListWalkerBinding
    private lateinit var userWalkerClassList: ArrayList<UserWalkerClass>
    private lateinit var adapter: UserWalkerAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    var uid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListWalkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mDbRef= FirebaseDatabase.getInstance().getReference()

        userWalkerClassList = ArrayList()
        adapter = UserWalkerAdapter(this, userWalkerClassList, uid)

        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = adapter

        mAuth.currentUser?.let {
            mDbRef.child("walkerUser").child(it.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUserWalkerClass = snapshot.getValue(UserWalkerClass::class.java)
                    val currentUserCity = currentUserWalkerClass?.city

                    mDbRef.child("ownerUser").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            userWalkerClassList.clear()

                            for (postSnapshot in snapshot.children) {
                                val userWalkerClass = postSnapshot.getValue(UserWalkerClass::class.java)
                                uid = userWalkerClass?.uid
                                adapter.uid = uid

                                if (userWalkerClass?.city.toString().toLowerCase() == currentUserCity.toString().toLowerCase()) {
                                    userWalkerClassList.add(userWalkerClass!!)
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Obsłuż błąd odczytu danych
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Obsłuż błąd odczytu danych
                }
            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
            mAuth.signOut()
            val intent = Intent(this@ChatListWalker, MainActivity::class.java)
            finish()
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.tryb){
            val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (currentMode == Configuration.UI_MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            recreate()
        }
        return true
    }
}
class UserWalkerAdapter(val context: Context, val userWalkerClassList: ArrayList<UserWalkerClass>,var uid: String?): RecyclerView.Adapter<UserWalkerAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userWalkerClassList[position]
        holder.textName.text = currentUser.name

        val uid = currentUser.uid.toString()
        val ocenyRef = FirebaseDatabase.getInstance().getReference("oceny").child(uid)
        ocenyRef.child("srednia").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sredniaOcen = snapshot.getValue(Double::class.java)
                if (sredniaOcen != null) {
                    holder.rating.text = sredniaOcen.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val opisRef = FirebaseDatabase.getInstance().getReference("ownerUser").child(uid)
        opisRef.child("opis").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val opis = snapshot.getValue(String::class.java)
                if (opis != null) {
                    holder.opis.text = opis.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val imageRef = FirebaseDatabase.getInstance().getReference("userImage").child(uid)
        imageRef.child("url").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                if (url != null) {
                    Picasso.get().load(url).fit().into(holder.image)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        holder.messageButton.setOnClickListener{
            val intent = Intent(context,ChatActivity::class.java)

            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            context.startActivity(intent)
        }

        holder.ratingButton.setOnClickListener {
            val intent = Intent(context, RatingBar::class.java)
            intent.putExtra("uid", currentUser.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userWalkerClassList.size
    }

    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textName = itemView.findViewById<TextView>(R.id.txtName)
        val ratingButton = itemView.findViewById<ImageButton>(R.id.btnRating)
        val messageButton = itemView.findViewById<ImageButton>(R.id.btnMessage)
        val rating = itemView.findViewById<TextView>(R.id.rating)
        val image = itemView.findViewById<ImageView>(R.id.imageView)
        val opis = itemView.findViewById<TextView>(R.id.txtOpis)
    }

}
