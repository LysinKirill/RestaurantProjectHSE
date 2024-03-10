package presentation.menu

import data.entity.AccountEntity
import presentation.menu.interfaces.DisplayStrategy
import presentation.menu.interfaces.Menu
import presentation.menu.interfaces.RequestOptionStrategy
import presentation.menu.options.OrderMenuOption


class OrderMenu(
    private val userAccount: AccountEntity,
    private val displayStrategy: DisplayStrategy = DefaultDisplayStrategy(OrderMenuOption::class.java),
    private val requestStrategy: RequestOptionStrategy<OrderMenuOption> = ConsoleRequestOptionStrategy(OrderMenuOption::class.java),
) : Menu {
    override fun displayMenu() = displayStrategy.display()

    override fun handleInteractions() {
        var isActive = true
        do {
            println("Choose one of the following options.")
            displayMenu()
            when (requestStrategy.requestOption()) {
                OrderMenuOption.CreateOrder -> {
                    TODO()
                }

                OrderMenuOption.ShowOrders -> TODO()
                OrderMenuOption.AddDishToOrder -> TODO()
                OrderMenuOption.CancelOrder -> TODO()
                OrderMenuOption.PayForOrder -> TODO()
                OrderMenuOption.CloseMenu -> {
                    println("Exiting order menu...")
                    isActive = false
                }

                null -> {}
            }

        } while (isActive)
    }
}