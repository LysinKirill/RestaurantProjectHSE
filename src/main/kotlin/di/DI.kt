package di

import data.dao.*
import data.dao.interfaces.*
import data.entity.AccountEntity
import data.entity.AccountType

object DI {
    const val SUPERUSER_CODE: String = "SuperUser1337"

    private const val ACCOUNT_STORAGE_PATH = "src/main/resources/account_storage.json"
    private const val MENU_STORAGE_PATH = "src/main/resources/menu_storage.json"
    private const val ORDER_STORAGE_PATH = "src/main/resources/order_storage.json"
    private const val STATISTICS_STORAGE_PATH = "src/main/resources/statistics_storage.json"
    private const val REVIEW_STORAGE_PATH = "src/main/resources/review_storage.json"


    val superuser: AccountEntity by lazy {
        AccountEntity("Admin", "_", AccountType.Administrator)
        // password "_" will never match anything because hash can never be a single character long
    }


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
}