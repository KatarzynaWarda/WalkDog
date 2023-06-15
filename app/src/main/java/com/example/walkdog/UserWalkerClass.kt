package com.example.walkdog

class UserWalkerClass {
    var name: String? = null
    var surname: String? = null
    var email: String? = null
    var city: String? = null
    var opis: String? = null
    var uid: String? = null

    constructor(){

    }

    constructor(name:String?,surname:String?,email:String?, city: String?, opis: String?, uid:String?){

        this.name = name
        this.surname = surname
        this.email = email
        this.city = city
        this.uid = uid
        this.opis = opis
    }
}