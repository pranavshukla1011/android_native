package com.example.sos_app

data class Contact (
    private val id:Int,
    private var name:String,
    private var phoneNo: String,
        ){
    private fun validate(phone: String): String? {
        val case1 = StringBuilder("+91")
        val case2 = StringBuilder("")

        //+91-6388195417
        return if (phone[0] != '+') {
            for (i in phone.indices) {
                if (phone[i] != '-' && phone[i] != ' ') {
                    case1.append(phone[i])
                }
            }
            case1.toString()
        } else {
            for (i in phone.indices) {
                // 0512 - 2511444
                if (phone[i] != '-' || phone[i] != ' ') {
                    case2.append(phone[i])
                }
            }
            case2.toString()
        }
    }

    fun getPhoneNo(): String? {
        return phoneNo
    }

    fun getId(): Int {
        return id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name!!
    }

    fun setPhoneNumber(phoneNo: String?) {
        this.phoneNo = phoneNo!!
    }

}