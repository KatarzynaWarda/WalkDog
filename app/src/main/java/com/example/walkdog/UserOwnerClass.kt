package com.example.walkdog

class UserOwnerClass {
    var name: String? = null
    var email: String? = null
    var city: String? = null
    var opis: String? = null
    var uid: String? = null


    constructor(){

    }

    constructor(name:String?,email:String?,city:String?, opis: String?, uid:String?){
        this.name = name
        this.email = email
        this.city = city
        this.uid = uid
        this.opis = opis
    }
}