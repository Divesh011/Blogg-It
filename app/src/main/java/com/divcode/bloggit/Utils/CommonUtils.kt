package com.divcode.bloggit.Utils

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

//Single Ton Class which Provides Commonly used objects across the application

object CommonUtils {

    //Stores other and current user's profile which are already loaded
    //whenever we need to display user profile then we can check if we already have that in our memory or not. It will save us from unnaccesary Network Calls
    var loadedProfiles: HashMap<String, Bitmap> = hashMapOf()

    // It stores blogs which are already loaded
    var loadedBlogs: MutableList<Blog> = mutableListOf()

    //Holds Instance of current User
    var currentUser = Firebase.auth.currentUser

    var userLikedBlogs: MutableList<String> = mutableListOf()

    //Returns firebase's Firestore object
    fun getDBInstance(): FirebaseFirestore{
        return Firebase.firestore
    }
}