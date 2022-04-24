package com.example.android.foodieexpressserver.model.eventBus

import com.example.android.foodieexpressserver.model.SizeModel

class UpdateSizeModel {
    var sizeModelList: List<SizeModel>? = null
    constructor()
    constructor(sizeModelList: List<SizeModel>?) {
        this.sizeModelList = sizeModelList
    }
}