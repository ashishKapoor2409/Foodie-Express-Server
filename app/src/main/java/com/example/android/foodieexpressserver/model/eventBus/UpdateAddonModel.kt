package com.example.android.foodieexpressserver.model.eventBus

import com.example.android.foodieexpressserver.model.AddonModel
import com.example.android.foodieexpressserver.model.SizeModel

class UpdateAddonModel {
    var addonModelList: List<AddonModel>? = null
    constructor()
    constructor(addonModelList: List<AddonModel>?) {
        this.addonModelList = addonModelList
    }
}