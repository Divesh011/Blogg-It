package com.divcode.bloggit.Activities

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.CommonUtils
import com.divcode.bloggit.Utils.FireStoreUtility
import java.io.Serializable
import java.time.LocalDate

// PublishBlogActivity handles the event of Updating or Publishing the Blog
class PublishBlogActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView

    private lateinit var categoryRadioGroup: RadioGroup
    private lateinit var discardBtn: Button
    private lateinit var postBtn: Button
    private lateinit var blogTitle: EditText
    private lateinit var blogContent: EditText

    private lateinit var introText: TextView

    var checkedRadioCategory: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_publish_blog_acitvity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the purpose whether this activity was invoked for editing or publishing the blog
        val purpose = intent.getStringExtra("PURPOSE")

        //if it was opened for editing get the blog to update, it became necessary to get this blog object even if it was opened to publish new Blog
        val blog = getSerializable(this, "BLOGTOEDIT", Blog::class.java)

        //UI comp init...
        profileImage = findViewById(R.id.profile_image)
        categoryRadioGroup = findViewById(R.id.CategoryRG)
        discardBtn = findViewById(R.id.DiscardBtn)
        postBtn = findViewById(R.id.PostBtn)
        blogTitle = findViewById(R.id.PublishBlogTitle)
        blogContent = findViewById(R.id.PublishBlogContent)
        introText = findViewById(R.id.IntroText)

        // set profile image from already loaded profile images list
        profileImage.setImageBitmap(CommonUtils.loadedProfiles[CommonUtils.currentUser?.uid.toString()])

        if (purpose == "EDITBLOG"){
            blogTitle.setText(blog.title)
            blogContent.setText(blog.content)
            checkedRadioCategory = blog.category
            postBtn.text = "Update"
            introText.text = "Edit Blog"
        }

        // Go back to HomeActivity
        discardBtn.setOnClickListener {
            finish()
        }

        categoryRadioGroup.setOnCheckedChangeListener {group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            checkedRadioCategory = radioButton.text.toString()
        }

        postBtn.setOnClickListener {
            if (purpose == "PUBLISH") publishBlog()
            else{
                updateBlog(blog)
            }
        }
    }

    fun publishBlog(){
        if(blogTitle.text.isNotEmpty() && blogContent.text.isNotEmpty() && checkedRadioCategory != null){
            val newBlog = Blog(CommonUtils.currentUser?.uid.toString(),
                blogTitle.text.toString(),
                blogContent.text.toString(),
                checkedRadioCategory!!,
                LocalDate.now().toString(),
                0L,
                0L
            )
            FireStoreUtility.postBlog(newBlog){
                if(!CommonUtils.loadedBlogs.contains(newBlog)) CommonUtils.loadedBlogs.add(newBlog)
                Toast.makeText(this, "Blog Published!", Toast.LENGTH_SHORT).show()
                finish()
            }

        }else{
            Toast.makeText(this, "One of the field is Empty. Make sure to fill out every Field.", Toast.LENGTH_LONG).show()
        }
    }

    fun updateBlog(blog: Blog, ){
        if(blogTitle.text.isNotEmpty() && blogContent.text.isNotEmpty() && checkedRadioCategory != null){
            if (!(blog.content == blogContent.text.toString() && blog.title == blogTitle.text.toString() && blog.category == checkedRadioCategory)) {
                val newBlog = Blog(
                    CommonUtils.currentUser?.uid.toString(),
                    blogTitle.text.toString(),
                    blogContent.text.toString(),
                    checkedRadioCategory!!,
                    LocalDate.now().toString(),
                    blog.views,
                    blog.likes
                )
                FireStoreUtility.updateBlog(blog, newBlog) {
                    Toast.makeText(this, "Blog Updated!", Toast.LENGTH_SHORT).show()
                    CommonUtils.loadedBlogs.remove(blog)
                    CommonUtils.loadedBlogs.add(newBlog)
                    finish()
                }
            }else{
                Toast.makeText(this, "You have Changed Nothing To Update This Blog.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to get Serialized Objects through Intent
    @Suppress("DEPRECATION")
    fun <T : Serializable?> getSerializable(
        activity: Activity,
        name: String,
        clazz: Class<T>
    ): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

}