package data.entity

import java.time.LocalDateTime

data class OrderEntity(
    val id: Int,
    val visitorAccountName: String,
    val finishTime: LocalDateTime,
    val dishes: List<DishEntity>,
    val status: OrderStatus,
)