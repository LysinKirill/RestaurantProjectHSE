package di

import data.dao.*
import data.dao.interfaces.*
import data.entity.AccountEntity
import data.entity.AccountType
import domain.ConsoleInputManager
import domain.InputManager
import domain.controllers.AuthenticationControllerImpl
import domain.controllers.RestaurantMenuControllerImpl
import domain.controllers.ReviewControllerImpl
import domain.controllers.StatisticsControllerImpl
import domain.controllers.interfaces.AuthenticationController
import domain.controllers.interfaces.RestaurantMenuController
import domain.controllers.interfaces.ReviewController
import domain.controllers.interfaces.StatisticsController
import domain.services.HashVerifier
import domain.services.MultiThreadedOrderSystem
import domain.services.PaymentServiceImpl
import domain.services.ThreadSafeQueueOrderScheduler
import domain.services.interfaces.KeyValueVerifier
import domain.services.interfaces.OrderProcessingSystem
import domain.services.interfaces.OrderScheduler
import domain.services.interfaces.PaymentService
import java.security.MessageDigest

object DI {


    private const val ACCOUNT_STORAGE_PATH = "src/main/resources/account_storage.json"
    private const val MENU_STORAGE_PATH = "src/main/resources/menu_storage.json"
    private const val ORDER_STORAGE_PATH = "src/main/resources/order_storage.json"
    private const val STATISTICS_STORAGE_PATH = "src/main/resources/statistics_storage.json"
    private const val REVIEW_STORAGE_PATH = "src/main/resources/review_storage.json"

    private const val SIMULTANEOUS_ORDERS_LIMIT = 5

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

    val menuController: RestaurantMenuController
        get() = RestaurantMenuControllerImpl(menuDao, inputManager)

    val statisticsController: StatisticsController
        get() = StatisticsControllerImpl(statisticsDao, reviewDao, orderDao, inputManager)

    val reviewController: ReviewController
        get() = ReviewControllerImpl(reviewDao, orderDao, inputManager)


    // Services
    private val authenticator: KeyValueVerifier<String, String>
        get() = HashVerifier(accountDao, hashFunction)

    private val inputManager: InputManager
        get() = ConsoleInputManager()

    private val paymentService: PaymentService by lazy {
        PaymentServiceImpl(statisticsDao, inputManager)
    }

    private val orderScheduler: OrderScheduler by lazy {
        ThreadSafeQueueOrderScheduler()
    }

    val orderSystem: OrderProcessingSystem by lazy {
        MultiThreadedOrderSystem(
            menuDao = menuDao,
            orderDao = orderDao,
            paymentService = paymentService,
            inputManager = inputManager,
            orderScheduler = orderScheduler,
            maxSimultaneousOrders = SIMULTANEOUS_ORDERS_LIMIT
        )
    }

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