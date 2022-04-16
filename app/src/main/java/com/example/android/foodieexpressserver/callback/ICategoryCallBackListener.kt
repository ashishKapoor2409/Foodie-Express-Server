package com.example.android.foodieexpressserver.callback

import com.example.android.foodieexpressserver.model.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoriesList:List <CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}