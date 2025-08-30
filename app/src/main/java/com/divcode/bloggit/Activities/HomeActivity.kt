package com.divcode.bloggit.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.divcode.bloggit.Fragments.HomeFragment
import com.divcode.bloggit.Fragments.MyBlogsFragment
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.CommonUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Home Activity which manages fragments, bottom navigation and action bar
class HomeActivity : AppCompatActivity() {
    private lateinit var welcomeTv: TextView
    private lateinit var profileImage: ImageView
    private lateinit var popupMenu: ImageView
    var loadedProfiles: HashMap<String, Bitmap> = hashMapOf()
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var homeFragment: HomeFragment
    private lateinit var myBlogsFragment: MyBlogsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI comp init
        homeFragment = HomeFragment()
        myBlogsFragment = MyBlogsFragment()

        welcomeTv = findViewById(R.id.welcomeTv)
        profileImage = findViewById(R.id.profile_image)
        popupMenu = findViewById(R.id.popupMenu)
        bottomNav = findViewById(R.id.bottom_nav)

        // check if profile is already loaded if yes then use that otherwise make network call and add it to loaded Profile list
        if (CommonUtils.loadedProfiles.contains(CommonUtils.currentUser?.uid.toString())){
            profileImage.setImageBitmap(CommonUtils.loadedProfiles[CommonUtils.currentUser?.uid.toString()])
        }else {
            Glide.with(this).asBitmap().load(CommonUtils.currentUser?.photoUrl.toString())
                .into<CustomTarget<Bitmap>>(object: CustomTarget<Bitmap>(){
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        profileImage.setImageBitmap(resource)
                        CommonUtils.loadedProfiles.put(CommonUtils.currentUser?.photoUrl.toString(), resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }

                })

        }
        bottomNav.selectedItemId = R.id.home_

        // set HomeFragment as default Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()

        setWelcomeText("Welcome, ${CommonUtils.currentUser?.displayName.toString()}!")

        // popMenu
        popupMenu.setOnClickListener {
            val menu = PopupMenu(baseContext, popupMenu)
            menu.inflate(R.menu.popup_menu)
            menu.show()

            menu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.logout ->{
                        Firebase.auth.signOut()
                        Intent(this@HomeActivity, MainActivity::class.java).apply {
                            startActivity(this)
                            finish()
                        }
                    }
                    R.id.filter_options -> {
                        setUpFilterMenu()
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }

        // handle bottom navigation and change Fragments
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_ -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit()
                    setWelcomeText("Welcome, ${CommonUtils.currentUser?.displayName.toString()}!")

                }
                R.id.my_blogs ->{
                    loadedProfiles = CommonUtils.loadedProfiles
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, myBlogsFragment)
                        .commit()
                    setWelcomeText("My Blogs")
                    CommonUtils.loadedProfiles = loadedProfiles
                }
            }

            return@setOnItemSelectedListener true
        }
    }

    // popupMenu for filtering options
    fun setUpFilterMenu(){
        val filterMenu = PopupMenu(this, popupMenu)
        filterMenu.inflate(R.menu.filter_menu)
        filterMenu.show()

        filterMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.tech_blogs ->{
                    homeFragment.filterBlogs("Tech")
                }

                R.id.edu_blogs ->{
                    homeFragment.filterBlogs("Educational")
                }

                R.id.entertainment_blogs ->{
                    homeFragment.filterBlogs("Entertainment")
                }
                R.id.other_blogs ->{
                    homeFragment.filterBlogs("Others")
                }
                R.id.all_blogs -> {
                    homeFragment.filterBlogs("ALLBLOGS")

                }
            }
        return@setOnMenuItemClickListener true
        }

    }
    fun setWelcomeText(text: String){
        welcomeTv.text = text
    }

}