package di

import data.dao.*
import data.dao.interfaces.*
import data.entity.AccountEntity
import data.entity.AccountType
import domain.ConsoleInputManager
import domain.InputManager
import domain.controllers.AuthenticationControllerImpl
import domain.controllers.interfaces.AuthenticationController
import domain.services.HashVerifier
import domain.services.interfaces.KeyValueVerifier
import java.security.MessageDigest

object DI {


    private const val ACCOUNT_STORAGE_PATH = "src/main/resources/account_storage.json"
    private const val MENU_STORAGE_PATH = "src/main/resources/menu_storage.json"
    private const val ORDER_STORAGE_PATH = "src/main/resources/order_storage.json"
    private const val STATISTICS_STORAGE_PATH = "src/main/resources/statistics_storage.json"
    private const val REVIEW_STORAGE_PATH = "src/main/resources/review_storage.json"



    // Super user
    val superuser: AccountEntity by lazy {
        AccountEntity("Admin", "_", AccountType.Administrator)
        // password "_" will never match anything because hash can never be a single character long
    }

    const val SUPERUSER_CODE: String = "SuperUser1337"


    // Data access objects

    private val accountDao: AccountDao by lazy {
        JsonAccountStorage(ACCOUNT_STORAGE_PATH)
    }

    private val menuDao: MenuDao by lazy {
        JsonMenuStorage(MENU_STORAGE_PATH)
    }

    val orderDao: OrderDao by lazy {
        ThreadSafeJsonOrderStorage(ORDER_STORAGE_PATH)
    }

    private val statisticsDao: RestaurantStatisticsDao by lazy {
        JsonRestaurantStatisticsStorage(STATISTICS_STORAGE_PATH)
    }

    private val reviewDao: ReviewDao by lazy {
        JsonReviewStorage(REVIEW_STORAGE_PATH)
    }

    // Controllers
    val authenticationController: AuthenticationController
        get() = AuthenticationControllerImpl(accountDao, authenticator, inputManager, hashFunction)


    // Services
    private val authenticator: KeyValueVerifier<String, String>
        get() = HashVerifier(accountDao, hashFunction)

    val inputManager: InputManager
        get() = ConsoleInputManager()


    // Auxiliary
    private val hashFunction: (str: String) -> String by lazy {
        fun hashFunc(str: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val hashedBytes = md.digest(str.toByteArray(Charsets.UTF_8))
            val sb = StringBuilder()
            for (b in hashedBytes) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString()
        }
        { str -> hashFunc(str) }
    }
}