package presentation.menu

import data.entity.AccountEntity
import presentation.menu.interfaces.DisplayStrategy
import presentation.menu.interfaces.Menu
import presentation.menu.interfaces.RequestOptionStrategy
import presentation.menu.options.ReviewMenuOption


class ReviewMenu(
    private val account: AccountEntity,
    private val displayStrategy: DisplayStrategy = DefaultDisplayStrategy(ReviewMenuOption::class.java),
    private val requestStrategy: RequestOptionStrategy<ReviewMenuOption> = ConsoleRequestOptionStrategy(ReviewMenuOption::class.java),
) : Menu {
    override fun displayMenu() = displayStrategy.display()

    override fun handleInteractions() {
        var isActive = true
        do {
            println("Choose one of the following options.")
            displayMenu()
            when (requestStrategy.requestOption()) {
                ReviewMenuOption.ShowReviews -> tryShowReviews()
                ReviewMenuOption.LeaveReview -> leaveReview()
                ReviewMenuOption.UpdateReview -> updateReview()
                ReviewMenuOption.DeleteReview -> deleteReview()
                ReviewMenuOption.CloseMenu -> {
                    println("Closing review menu...")
                    isActive = false
                }

                null -> {}
            }
        } while (isActive)
    }

    private fun updateReview() {
        TODO("Not yet implemented")
    }

    private fun leaveReview() {
        TODO("Not yet implemented")
    }

    private fun tryShowReviews() {
        TODO("Not yet implemented")
    }

    private fun deleteReview() {
        TODO("Not yet implemented")
    }

}
