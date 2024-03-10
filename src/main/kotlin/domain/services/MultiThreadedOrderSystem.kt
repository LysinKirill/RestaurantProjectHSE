package domain.services

import data.dao.interfaces.MenuDao
import data.dao.interfaces.OrderDao
import data.entity.AccountEntity
import data.entity.DishEntity
import data.entity.OrderEntity
import data.entity.OrderStatus
import domain.CookingStrategy
import domain.InputManager
import domain.SleepCookingStrategy
import domain.services.interfaces.OrderProcessingSystem
import domain.services.interfaces.OrderScheduler
import domain.services.interfaces.PaymentService
import java.time.LocalDateTime
import kotlin.concurrent.thread
import kotlin.math.max

class MultiThreadedOrderSystem(
    private val menuDao: MenuDao,
    private val orderDao: OrderDao,
    private val paymentService: PaymentService,
    private val inputManager: InputManager,
    private val orderScheduler: OrderScheduler,
    private val maxSimultaneousOrders: Int,
    private val cookingStrategy: CookingStrategy = SleepCookingStrategy(),
) : OrderProcessingSystem {

    private val simultaneousOrdersLock = Any()
    private var currentActiveOrders = 0
    private val orderThreads: MutableMap<Int, Thread> = mutableMapOf()

    init {
        for (order in orderDao.getAllOrders()) {
            when (order.status) {
                OrderStatus.Created -> orderScheduler.scheduleOrder(order)
                OrderStatus.Cooking -> {
                    if (LocalDateTime.now().isAfter(order.finishTime)) {
                        orderDao.updateOrder(order.copy(status = OrderStatus.Ready))
                        println("Order with ID = ${order.id} is ready and can be paid for!")
                        continue
                    }
                    ++currentActiveOrders
                    executeOrder(order)
                }

                OrderStatus.Ready -> continue
                OrderStatus.PaidFor -> continue
            }
        }

        while (currentActiveOrders < maxSimultaneousOrders) {
            val orderToExecute = orderScheduler.getOrder() ?: break
            startNewOrder(orderToExecute)
        }
    }


    override fun getUserOrders(user: AccountEntity): List<OrderEntity> = getUserOrders(user.name)
    override fun showUserOrders(user: AccountEntity) {
        println("Active orders of \"${user.name}\":")
        showOrders(getUserOrders(user.name).sortedBy { it.status })
    }

    override fun createOrder(user: AccountEntity) {
        val dishList = mutableListOf<DishEntity>()
        inputManager.showPrompt("Start adding dishes to your order. When the order is complete enter an empty line.")
        do {
            inputManager.showPrompt("Enter the name of the dish to be added to your order (or an empty line): ")
            val dishName = inputManager.getString()
            if (dishName.isEmpty()) break

            val menuEntryEntity = menuDao.getEntryByDishName(dishName)
            if (menuEntryEntity == null) {
                println("Dish $dishName is not on the menu. Cannot add it to your order.")
                continue
            }
            if (menuEntryEntity.remainingNumber - dishList.count { dish -> dish.name == menuEntryEntity.dish.name } <= 0
            ) {
                println("There is no more \"$dishName\" available. Cannot add it to your order.") // change prompt
                continue
            }
            dishList.add(menuEntryEntity.dish)
            println("Dish \"$dishName\" has been added to your order.")
        } while (true)

        confirmOrder(user, dishList)
    }

    override fun addDishToOrder(user: AccountEntity) {
        val accountOrders = getUserOrders(
            userName = user.name,
            orderStatus = OrderStatus.Cooking
        )
        if (accountOrders.isEmpty()) {
            println("No active orders for user \"${user.name}\" found.")
            return
        }
        println("Active orders of \"${user.name}\":")
        showOrders(accountOrders)

        inputManager.showPrompt("Input the ID of the order you want to add a dish to: ")
        val orderId = inputManager.getInt()

        val order = orderDao.getOrder(orderId)
        if (order == null || order.status != OrderStatus.Cooking) {
            println("Order with ID = $orderId is not being cooked.")
            return
        }

        // Show menu to user

        inputManager.showPrompt("Enter the name of the dish to be added to your order: ")
        val dishName = inputManager.getString()

        val menuEntryEntity = menuDao.getEntryByDishName(dishName)
        if (menuEntryEntity == null) {
            println("Dish $dishName is not on the menu. Cannot add it to your order.")
            return
        }
        if (menuEntryEntity.remainingNumber <= 0) {
            println("There is no more \"$dishName\" available. Cannot add it to your order.") // change prompt
            return
        }

        val updatedOrder = order.copy(
            dishes = order.dishes + menuEntryEntity.dish,
            finishTime = getUpdatedFinishTime(order.finishTime, menuEntryEntity.dish)
        )

        synchronized(simultaneousOrdersLock) {
            orderDao.updateOrder(updatedOrder)
            println("Your order has been updated.")
            orderThreads[orderId]?.interrupt()
            executeOrder(updatedOrder)
        }
    }

    override fun cancelOrder(user: AccountEntity) {
        val userOrders = getUserOrders(
            userName = user.name,
            orderStatus = OrderStatus.Cooking
        )
        if (userOrders.isEmpty()) {
            println("No active orders for account \"${user.name}\" found.")
            return
        }
        println("Active orders of \"${user.name}\" which are being cooked at the moment:")
        showOrders(userOrders)

        inputManager.showPrompt("Input the ID of the order you want to cancel: ")
        val orderId = inputManager.getInt()


        val cookingThread = orderThreads[orderId]
        if (cookingThread == null) {
            println("Order with ID = $orderId is not being processed.")
            return
        }
        cookingThread.interrupt()

        orderThreads.remove(orderId)
        orderDao.removeOrder(orderId)
        println("The order with ID = $orderId has been cancelled.")

        synchronized(simultaneousOrdersLock) {
            currentActiveOrders = max(currentActiveOrders - 1, 0)
        }
        startNewOrder()
    }

    override fun payForOrder(user: AccountEntity) {
        val completedOrders = getUserOrders(user.name, orderStatus = OrderStatus.Ready)
        if (completedOrders.isEmpty()) {
            println("You have no orders that can be paid for.")
            return
        }
        println("List of orders that can be paid for: ")
        showOrders(completedOrders)

        inputManager.showPrompt("Enter the ID of the order you want to pay for: ")
        val orderId = inputManager.getInt()

        if (completedOrders.none { it.id == orderId }) {
            println("Order with ID = $orderId cannot be paid for at the moment.")
            return
        }


        val order = orderDao.getOrder(orderId)
        if (order == null) {
            println("Cannot receive payment at this time.")
            return
        }

        val total = order.dishes.sumOf { it.price }
        paymentService.requestPayment("Your total for this order is %.2f.\nProvide payment: ".format(total))
        val paymentSuccess = paymentService.receivePayment(user, total)

        if (!paymentSuccess) {
            println("Payment failed...")
            return
        }

        orderDao.updateOrder(order.copy(status = OrderStatus.PaidFor))
        println("Payment received.")
    }

    override fun clearOrders() {
        val orderIds = orderThreads.keys.toList()
        for(orderId in orderIds) {
            orderThreads[orderId]?.interrupt()
            orderThreads.remove(orderId)
        }

        currentActiveOrders = 0
    }

    private fun confirmOrder(
        user: AccountEntity,
        dishList: MutableList<DishEntity>
    ) {
        if (dishList.isEmpty()) {
            println("Cannot create an empty order...")
            return
        }

        println("Your order:\n"
                + dishList
            .joinToString(
                prefix = "\t",
                separator = "\n\t",
                postfix = "\n"
            ) { dish -> "${dish.name}\tprice: ${dish.price}" }
        )
        println( "Total: %.2f".format(dishList.sumOf { it.price }))

        do {
            inputManager.showPrompt("Confirm order? [Yes]/[No]")
            val confirmation = inputManager.getString()
            if (confirmation.lowercase() == "no") {
                println("The order has been cancelled.")
                return
            }
            if (confirmation.lowercase() != "yes") {
                println("\"$confirmation\" is not a valid option. Try again.")
                continue
            }

            val finishTime = calculateFinishTime(dishList)
            val response = orderDao.addOrder(user.name, finishTime, dishList)
            if (response == null) {
                println("Could not create the order...")
                return
            }
            println("Order created! Your order ID: ${response.id}")

            // Decrease the amounts of dishes by the amount of dishes in the created order
            for (pair in response.dishes.groupingBy { it.name }.eachCount()) {
                val entry = menuDao.getEntryByDishName(pair.key) ?: continue
                val updatedEntry = entry.copy(remainingNumber = entry.remainingNumber - pair.value)
                menuDao.updateEntry(updatedEntry)
            }

            if (currentActiveOrders < maxSimultaneousOrders) {
                startNewOrder(response)
                return
            }
            orderScheduler.scheduleOrder(response)
            return
        } while (true)
    }

    private fun executeOrder(order: OrderEntity) {
        val cookingThread = thread(start = false) {
            try {
                orderDao.updateOrder(order.copy(status = OrderStatus.Cooking))

                cookingStrategy.cookOrder(order)

                orderDao.updateOrder(order.copy(status = OrderStatus.Ready))
                println("Order with ID = ${order.id} is ready and can be paid for!")
                orderThreads.remove(order.id)
                synchronized(simultaneousOrdersLock) {
                    --currentActiveOrders
                }

                startNewOrder()
            } catch (_: InterruptedException) {
                return@thread
            }
        }

        orderThreads[order.id] = cookingThread
        cookingThread.start()
    }

    private fun calculateFinishTime(dishList: List<DishEntity>): LocalDateTime {
        val finishTime = LocalDateTime.now().plusSeconds(dishList.maxOf { it.cookingTimeInSeconds }.toLong())
        return finishTime
    }

    private fun getUpdatedFinishTime(currentFinishTime: LocalDateTime, newDish: DishEntity): LocalDateTime {
        val dishFinishTime = LocalDateTime.now().plusSeconds(newDish.cookingTimeInSeconds.toLong())
        if (dishFinishTime.isAfter(currentFinishTime))
            return dishFinishTime
        return currentFinishTime
    }

    private fun getUserOrders(userName: String): List<OrderEntity> =
        orderDao.getAllOrders().filter { it.visitorAccountName == userName }

    private fun getUserOrders(userName: String, orderStatus: OrderStatus): List<OrderEntity> =
        orderDao.getAllOrders().filter { it.visitorAccountName == userName && it.status == orderStatus }

    private fun showOrders(orders: List<OrderEntity>) = println(
        orders.joinToString(
            prefix = "\t",
            separator = "\n\t", transform =
            { order ->
                "Order ID: ${order.id}, " +
                        "Status: ${order.status}, " +
                        "Dishes: ${order.dishes.sortedBy { it.name }.joinToString { it.name }}"
            }
        )
    )


    private fun startNewOrder(order: OrderEntity) {
        val updatedOrder = order.copy(finishTime = calculateFinishTime(order.dishes))
        orderDao.updateOrder(updatedOrder)
        synchronized(simultaneousOrdersLock) {
            ++currentActiveOrders
            executeOrder(updatedOrder)
        }
    }

    private fun startNewOrder() = orderScheduler.getOrder()?.let { startNewOrder(it) }
}
