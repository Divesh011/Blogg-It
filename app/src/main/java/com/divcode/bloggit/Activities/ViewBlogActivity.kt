package com.divcode.bloggit.Activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.divcode.bloggit.Adapters.CommentsAdapter
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.Comment
import com.divcode.bloggit.Utils.CommonUtils
import com.divcode.bloggit.Utils.FireStoreUtility
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate

// Activity to View The Blog and Handle Comments and Likes
class ViewBlogActivity : AppCompatActivity() {

    private lateinit var blogTitleTv: TextView
    private lateinit var blogContentView: TextView
    private lateinit var currentUserPic: ImageView
    private lateinit var blogUserPic: ImageView
    private lateinit var likeBlogBtn: ImageView
    private lateinit var sendBtn: ImageView
    private lateinit var likesTv: TextView
    private lateinit var viewsTv: TextView
    private lateinit var blogUserName: TextView
    private lateinit var dateTv: TextView
    private lateinit var commentEditText: EditText

    private lateinit var commentsRC: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var likeRef: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_blog)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // holds object of blog to be viewed
        val blog = CommonUtils.loadedBlogs[intent.getIntExtra("CURRENTBLOG", 0)]

        // this ref will be added to current user's liked blogs list and used to determine whether user has liked this blog previously or not
        likeRef = blog.userId + "/" + blog.title

        // UI Components Initialization
        blogTitleTv = findViewById(R.id.ViewBlogTitle)
        blogContentView = findViewById(R.id.ViewBlogContent)
        currentUserPic = findViewById(R.id.CurrentUserProfilePic)
        blogUserPic = findViewById(R.id.ViewBlogProfilePic)
        likesTv = findViewById(R.id.ViewBlogLikesTv)
        viewsTv = findViewById(R.id.ViewBlogViewsTv)
        blogUserName = findViewById(R.id.ViewBlogUserName)
        dateTv = findViewById(R.id.dateTv)
        likeBlogBtn = findViewById(R.id.likePostBtn)

        // if user has already liked the comment before then set the like button Image to already liked image
        if (CommonUtils.userLikedBlogs.contains(likeRef)) {
            likeBlogBtn.setImageResource(R.drawable.liked)
        }

        // UI comps .....
        commentEditText = findViewById(R.id.CommentBox)
        sendBtn = findViewById(R.id.commentSendBtn)

        blogTitleTv.text = blog.title
        blogContentView.text = blog.content
        likesTv.text = blog.likes.toString()
        viewsTv.text = blog.views.toString()

        // Comments list
        val comments: MutableList<Comment> = mutableListOf()
        commentsRC = findViewById(R.id.CommentsRC)
        commentsAdapter = CommentsAdapter(comments, this)
        commentsRC.isNestedScrollingEnabled = true;

        commentsRC.adapter = commentsAdapter
        commentsRC.layoutManager = LinearLayoutManager(this)
        commentsRC.setHasFixedSize(true)


        // Load Profiles of blog's owner and current user
        FireStoreUtility.getProfile(blog.userId) { result ->
            Glide.with(this).load(result).into(blogUserPic)
        }

        FireStoreUtility.getProfile(Firebase.auth.currentUser?.uid.toString()) { result ->
            Glide.with(this).load(result).into(currentUserPic)
        }

        FireStoreUtility.getUserName(blog.userId) { result ->
            blogUserName.text = result
        }

        dateTv.text = blog.date

        // load Comments
        loadComments(blog, comments)

        // comment send button onClick
        sendBtn.setOnClickListener {
            if (commentEditText.text.isNotEmpty()) {
                FireStoreUtility.sendComment(
                    Firebase.auth.currentUser?.uid.toString(), blog.title, Comment(
                        blog.userId, commentEditText.text.toString(),
                        LocalDate.now().toString()
                    )
                ) {
                    loadComments(blog, comments)
                }
            }
        }

        // Handle Liking of the Blog
        likeBlogBtn.setOnClickListener {
            FireStoreUtility.getUserLikedBlogs()

            if (CommonUtils.userLikedBlogs.contains(likeRef)) {
                FireStoreUtility.decreaseLike(blog.userId, blog.title, blog.likes)
                CommonUtils.userLikedBlogs.remove(likeRef)
                FireStoreUtility.updateUsersLikedBlogs()
                likeBlogBtn.setImageResource(R.drawable.unliked)
                CommonUtils.loadedBlogs[CommonUtils.loadedBlogs.indexOf(blog)].likes -= 1
                blog.likes -= 1

            } else {
                FireStoreUtility.increaseLike(blog.userId, blog.title, blog.likes)
                CommonUtils.userLikedBlogs.add(likeRef)
                FireStoreUtility.updateUsersLikedBlogs()
                likeBlogBtn.setImageResource(R.drawable.liked)
                CommonUtils.loadedBlogs[CommonUtils.loadedBlogs.indexOf(blog)].likes += 1
                blog.likes += 1
            }

        }

    }

    // method to load comments of the blog and store them in list
    fun loadComments(blog: Blog, comments: MutableList<Comment>) {
        lifecycleScope.launch {
            async {
                FireStoreUtility.getComments(
                    blog.userId,
                    blog.title
                ) { comment ->
                    if (!comments.contains(comment)) {
                        comments.add(comment)
                        commentsAdapter.notifyItemInserted(comments.size - 1)
                    }
                }
                true
            }.await()
        }
    }
}