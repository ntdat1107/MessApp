package com.example.messapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messapp.databinding.ActivityRegisterBinding
import com.example.messapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private var binding: ActivityRegisterBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mProgressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar!!.title = "Sign up"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setMessage("Signing up...")

        mAuth = FirebaseAuth.getInstance()

        binding?.btnContinue?.setOnClickListener {
            val email = binding?.etEmail!!.text.toString()
            val password = binding?.etPassword!!.text.toString()

            when {
                TextUtils.isEmpty(email.trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(password.trim { it <= ' ' }) -> {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    mProgressDialog.show()
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Sign up successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val user = User(mAuth.currentUser!!.uid)
                                FirebaseDatabase.getInstance().reference
                                    .child("users")
                                    .child(mAuth.currentUser!!.uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        mProgressDialog.dismiss()
                                        mAuth.signOut()
                                        finish()
                                    }
                                    .addOnFailureListener { except ->
                                        mProgressDialog.dismiss()
                                        Toast.makeText(
                                            this,
                                            except.message.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                mProgressDialog.dismiss()

                                Toast.makeText(
                                    this,
                                    it.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }

        binding?.tvSignIn?.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}