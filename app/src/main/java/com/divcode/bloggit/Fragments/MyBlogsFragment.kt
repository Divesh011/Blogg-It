package com.divcode.bloggit.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.divcode.bloggit.Adapters.MyBlogsAdapter
import com.divcode.bloggit.R
import com.divcode.bloggit.Utils.Blog
import com.divcode.bloggit.Utils.CommonUtils

class MyBlogsFragment(): Fragment() {

    private lateinit var myBlogRecyclerView: RecyclerView

    private lateinit var adapter: MyBlogsAdapter
    private lateinit var infoTv: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_blogs, container, false)
        val myBlogs = mutableListOf<Blog>()

        CommonUtils.loadedBlogs.forEach {
            if(it.userId == CommonUtils.currentUser?.uid.toString()){
                myBlogs.add(it)
//                adapter.notifyItemInserted(myBlogs.size-1)
            }
        }

        Log.d("MYBLOGFRAG", myBlogs.toString())

        myBlogRecyclerView = view.findViewById(R.id.my_blogs_rc)
        infoTv = view.findViewById(R.id.NoBlogsTv)

        if (myBlogs.isNotEmpty()){
            infoTv.visibility = View.INVISIBLE
        }

        adapter = MyBlogsAdapter(myBlogs,requireContext())
        Log.d("MYBLOG", myBlogs.toString())
        myBlogRecyclerView.adapter = adapter
        myBlogRecyclerView.layoutManager = LinearLayoutManager(context)
        myBlogRecyclerView.setHasFixedSize(true)



        return view
    }

}