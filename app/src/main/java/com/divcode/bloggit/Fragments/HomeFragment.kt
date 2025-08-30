package com.divcode.bloggit.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.divcode.bloggit.Utils.FireStoreUtility
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.divcode.bloggit.Activities.PublishBlogActivity
import com.divcode.bloggit.Adapters.BlogAdapter
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.CommonUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// Home Fragment responsible for showing list of blogs and handle onClicks, filtering etc.
class HomeFragment() : Fragment() {

    private lateinit var blogRecyclerView: RecyclerView

    private lateinit var adapter: BlogAdapter

    private lateinit var postBlogFAB: FloatingActionButton

    // to keep track the number of blogs before and after the user posts a new blog or deletes old one then update the recycler view accordingly in onStart method
    var blogsCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initializing UI components
        blogRecyclerView = view.findViewById(R.id.blogs_rc)
        postBlogFAB = view.findViewById(R.id.addBtn)

        adapter = BlogAdapter(requireContext(), CommonUtils.loadedBlogs)
        blogRecyclerView.adapter = adapter

        blogRecyclerView.layoutManager = LinearLayoutManager(context)
        blogRecyclerView.setHasFixedSize(true)

        // load Blogs asynchronously and add them to loaded blogs list of Common Util and sort them along by views
        if (CommonUtils.loadedBlogs.isEmpty()) {
            lifecycleScope.launch {
                async {
                    FireStoreUtility.getBlogs { blog ->
                        if (!CommonUtils.loadedBlogs.contains(blog)) {
                            blogsCount++
                            CommonUtils.loadedBlogs.add(blog)
                            CommonUtils.loadedBlogs.sortByDescending { it.views }
                        }
                        adapter.notifyItemInserted(CommonUtils.loadedBlogs.indexOf(blog))
                    }
                    true
                }.await()
            }
        }

        postBlogFAB.setOnClickListener {
            blogsCount = CommonUtils.loadedBlogs.size
            //Start Activity for purpose of Publishing The Blog
            Intent(context, PublishBlogActivity::class.java).apply {
                // Specifies the Purpose, cuz im using the same activity to update or publish new blog so i have specify purpose so that
                // PublishBlogActivity configures itself accordingly
                putExtra("PURPOSE", "PUBLISH")

                //
                putExtra("BLOGTOEDIT", Blog("", "", "", "", "", 0, 0))
                startActivity(this)
            }
        }
        return view
    }

    // function to filter blogs accordingly
    fun filterBlogs(filterBy: String) {
        if (filterBy != "ALLBLOGS") {
            val newBlogs = mutableListOf<Blog>()
            for (blogs in CommonUtils.loadedBlogs) {
                if (blogs.category == filterBy) {
                    newBlogs.add(blogs)
                }
                adapter.blogs = newBlogs
            }
        }else{
            adapter.blogs = CommonUtils.loadedBlogs
        }

        adapter.notifyDataSetChanged()
    }

    // when user navigates back to home fragment after posting, deleting, updating blog then configure the recycler view accordingly before hand
    override fun onStart() {
        if (CommonUtils.loadedBlogs.size > blogsCount) {
            adapter.notifyItemInserted(CommonUtils.loadedBlogs.size - 1)
            blogsCount = CommonUtils.loadedBlogs.size
        }
        super.onStart()
    }

}