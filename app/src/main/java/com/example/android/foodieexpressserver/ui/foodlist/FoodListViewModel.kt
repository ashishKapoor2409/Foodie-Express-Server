package com.example.android.foodieexpressserver.ui.foodlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.foodieexpressserver.common.Common
import com.example.android.foodieexpressserver.model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutableFoodModelListData : MutableLiveData<List<FoodModel>>? = null

    fun getMutableFoodModelListData(): MutableLiveData<List<FoodModel>> {
        if(mutableFoodModelListData == null) {
            mutableFoodModelListData = MutableLiveData()
        }
        mutableFoodModelListData!!.value = Common.categorySelected!!.foods
        return mutableFoodModelListData!!
    }
}