package presentation.menu

import data.entity.AccountEntity
import data.entity.AccountType
import presentation.menu.interfaces.Menu
import presentation.menu.interfaces.ResponsiveMenu

class MenuFactory {
    fun getAuthenticationMenu(): ResponsiveMenu<AccountEntity> {
        return AuthenticationMenu()
    }


    fun getMenuForUser(account: AccountEntity): Menu {
        return when (account.accountType) {
            AccountType.Administrator -> AdminMenu(
                menuModificationMenu = getMenuModificationMenu(),
                statisticsMenu = getStatisticsMenu(),
                userAccount = account
            )

            AccountType.Visitor -> VisitorMenu(
                reviewMenu = getReviewMenu(account),
                orderMenu = getOrderMenu(account),
            )
        }
    }

    private fun getReviewMenu(account: AccountEntity) = ReviewMenu(
        account = account,
    )

    private fun getOrderMenu(account: AccountEntity) = OrderMenu(
        userAccount = account,
    )

    private fun getStatisticsMenu() = StatisticsMenu()
    private fun getMenuModificationMenu() = MenuModificationMenu()
}