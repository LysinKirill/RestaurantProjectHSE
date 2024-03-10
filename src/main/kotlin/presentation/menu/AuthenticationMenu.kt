package presentation.menu

import data.entity.AccountEntity
import di.DI
import presentation.menu.interfaces.DisplayStrategy
import presentation.menu.interfaces.RequestOptionStrategy
import presentation.menu.interfaces.ResponsiveMenu
import presentation.menu.options.AuthenticationMenuOption
import kotlin.system.exitProcess

class AuthenticationMenu(
    private val displayStrategy: DisplayStrategy = DefaultDisplayStrategy(AuthenticationMenuOption::class.java),
    private val requestStrategy: RequestOptionStrategy<AuthenticationMenuOption> = ConsoleRequestOptionStrategy(
        AuthenticationMenuOption::class.java
    ),
) : ResponsiveMenu<AccountEntity> {
    private var currentAccount: AccountEntity? = null
    private var isActive = true

    override fun getResponse(): AccountEntity? = currentAccount

    override fun displayMenu() = displayStrategy.display()

    override fun handleInteractions() {
        isActive = true
        do {
            println("Choose one of the following options.")
            displayMenu()
            when (requestStrategy.requestOption()) {
                AuthenticationMenuOption.RegisterVisitor -> registerVisitor()
                AuthenticationMenuOption.RegisterAdmin -> registerAdmin()
                AuthenticationMenuOption.LoginVisitor -> loginVisitor()
                AuthenticationMenuOption.LoginAdmin -> loginAdmin()
                AuthenticationMenuOption.AuthorizeWithCode -> authorizeWithCode()
                AuthenticationMenuOption.Exit -> {
                    println("Exiting...")
                    exitProcess(0)
                }

                null -> {}
            }
        } while (isActive)
    }


    private fun registerAdmin() {
        TODO()
    }

    private fun registerVisitor() {
        TODO()
    }

    private fun loginVisitor() {
        TODO()
    }

    private fun loginAdmin() {
        currentAccount = DI.superuser
        isActive = false
    }

    private fun authorizeWithCode() {
        currentAccount = DI.superuser
        isActive = false
    }
}
