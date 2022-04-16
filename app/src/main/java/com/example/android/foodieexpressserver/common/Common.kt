package com.example.android.foodieexpressserver.common

import com.example.android.foodieexpressserver.model.CategoryModel
import com.example.android.foodieexpressserver.model.ServerUserModel

object Common {
    val SERVER_REF = "Server"
    var currentServerUser: ServerUserModel? = null
    const val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    var categorySelected: CategoryModel? = null
}