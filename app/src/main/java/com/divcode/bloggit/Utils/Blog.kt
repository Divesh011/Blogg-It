package com.divcode.bloggit.Utils

import java.io.Serializable

// class to hold individual Blog
data class Blog(
    val userId: String,
    val title: String,
    val content: String,
    val category: String = "Tech",
    val date: String,
    var views: Long,
    var likes: Long
) : Serializable
