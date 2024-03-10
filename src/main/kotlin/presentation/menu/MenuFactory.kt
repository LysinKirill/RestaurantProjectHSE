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
                userAccount = account
            )

            AccountType.Visitor -> VisitorMenu(
            )
        }
    }
}