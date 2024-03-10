package presentation.menu

import presentation.menu.interfaces.DisplayStrategy
import presentation.menu.interfaces.Menu
import presentation.menu.interfaces.RequestOptionStrategy
import presentation.menu.options.MenuModificationMenuOption

class MenuModificationMenu(
    private val displayStrategy: DisplayStrategy =
        DefaultDisplayStrategy(MenuModificationMenuOption::class.java),
    private val requestStrategy: RequestOptionStrategy<MenuModificationMenuOption> =
        ConsoleRequestOptionStrategy(MenuModificationMenuOption::class.java),
) : Menu {
    override fun displayMenu() = displayStrategy.display()

    override fun handleInteractions() {
        var isActive = true
        do {
            println("Choose one of the following options.")
            displayMenu()
            when (requestStrategy.requestOption()) {
                MenuModificationMenuOption.AddDishToMenu -> TODO()
                MenuModificationMenuOption.RemoveDishFromMenu -> TODO()
                MenuModificationMenuOption.SetDishCount -> TODO()
                MenuModificationMenuOption.SetDishPrice -> TODO()
                MenuModificationMenuOption.SetDishCookingTime -> TODO()
                MenuModificationMenuOption.GetAllMenuEntries -> TODO()
                MenuModificationMenuOption.CloseMenu -> {
                    println("Exiting menu modification menu...")
                    isActive = false
                }

                null -> {}
            }
        } while (isActive)
    }
}