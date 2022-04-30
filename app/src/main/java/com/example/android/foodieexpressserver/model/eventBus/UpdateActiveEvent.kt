package com.example.android.foodieexpressserver.model.eventBus

import com.example.android.foodieexpressserver.model.ShipperModel

class UpdateActiveEvent(var shipperModel: ShipperModel, var active:Boolean) {
}