package com.example.messapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.messapp.databinding.ActivitySetUpProfileBinding
import com.example.messapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SetUpProfileActivity : AppCompatActivity() {
    private var binding: ActivitySetUpProfileBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDb: FirebaseDatabase
    private lateinit var mStorage: FirebaseStorage
    private var mSelectedImageUri: Uri? = null
    private lateinit var mProgressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseDatabase.getInstance()
        mStorage = FirebaseStorage.getInstance()

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Updating profile...")

        if (intent.hasExtra("image_profile")) {
            val image = intent.getStringExtra("image_profile")
            val name = intent.getStringExtra("name_profile")
            Glide.with(this).load(image).centerCrop().into(binding?.imageView!!)
            binding?.etName!!.setText(name)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.data != null) {
                Glide.with(this).load(result.data!!.data).centerCrop().into(binding?.imageView!!)
                mSelectedImageUri = result.data!!.data
            }
        }

        binding?.imageView?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

        binding?.btnContinue?.setOnClickListener {
            val name = binding?.etName?.text!!.toString()
            when {
                TextUtils.isEmpty(name.trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                }
                mSelectedImageUri != null -> {
                    mProgressDialog.show()
                    val mRef = mStorage.reference.child("Profiles").child(mAuth.uid.toString())
                    mRef.putFile(mSelectedImageUri!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            mRef.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                val uid = mAuth.uid.toString()
                                val userName = binding?.etName?.text!!.toString()

                                val user = User(uid, userName, imageUrl, true)


                                mDb.reference.child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        mProgressDialog.dismiss()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                            }
                        } else {
                            mProgressDialog.dismiss()
                            Toast.makeText(
                                this,
                                task.exception!!.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else -> {
                    mProgressDialog.show()
                    val uid = mAuth.uid.toString()
                    val userName = binding?.etName?.text!!.toString()

                    val user = User(uid, userName, "No image", true)


                    mDb.reference.child("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            mProgressDialog.dismiss()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}