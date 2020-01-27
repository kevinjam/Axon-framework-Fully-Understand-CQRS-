package com.kevinjanvier.giftcard.queries

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class CardSummary(
        @Id
        var id:String? =  null,
        var initialBalance:Int ?=0,
        var remainingBalance:Int?=0
)


data class DataQuery(
        val offset:Int,
        val limit:Int
)


class SizeQuery