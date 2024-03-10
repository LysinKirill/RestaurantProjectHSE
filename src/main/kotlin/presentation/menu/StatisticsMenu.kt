package presentation.menu

import presentation.menu.interfaces.DisplayStrategy
import presentation.menu.interfaces.Menu
import presentation.menu.interfaces.RequestOptionStrategy
import presentation.menu.options.StatisticsMenuOption

class StatisticsMenu(
    private val displayStrategy: DisplayStrategy = DefaultDisplayStrategy(StatisticsMenuOption::class.java),
    private val requestStrategy: RequestOptionStrategy<StatisticsMenuOption> = ConsoleRequestOptionStrategy(
        StatisticsMenuOption::class.java
    ),
) : Menu {
    override fun displayMenu() = displayStrategy.display()

    override fun handleInteractions() {
        var isActive = true
        do {
            println("Choose one of the following options.")
            displayMenu()
            when (requestStrategy.requestOption()) {
                null -> {}
                StatisticsMenuOption.Revenue -> TODO()
                StatisticsMenuOption.PopularDishes -> TODO()
                StatisticsMenuOption.AverageRatingOfDishes -> TODO()
                StatisticsMenuOption.NumberOfOrdersOverPeriod -> TODO()
                StatisticsMenuOption.ShowDishReviews -> TODO()
                StatisticsMenuOption.CloseMenu -> {
                    println("Closing statistics menu...")
                    isActive = false
                }
            }
        } while (isActive)
    }
}