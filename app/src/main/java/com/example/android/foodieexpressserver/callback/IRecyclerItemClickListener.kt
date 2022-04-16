package com.example.android.foodieexpressserver.callback

import android.view.View

interface IRecyclerItemClickListener  {
    fun onItemClick(view: View, pos:Int)
}