package domain.services.interfaces

import data.entity.OrderEntity

interface OrderScheduler
{
    fun scheduleOrder(orderEntity: OrderEntity)
    fun getOrder() : OrderEntity?
}