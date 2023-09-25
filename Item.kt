package com.cucu.mongodbrealmxml

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Item() : RealmObject {

    @PrimaryKey
    var id: ObjectId = ObjectId()
    var isComplete:Boolean = false
    var summary:String = ""
    var owner_id:String = ""

    constructor(ownerId:String = "") : this(){
        owner_id = ownerId
    }
}