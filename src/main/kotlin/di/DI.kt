package di

import data.entity.AccountEntity
import data.entity.AccountType

object DI {
    const val SUPERUSER_CODE: String = "SuperUser1337"
    val superuser: AccountEntity by lazy {
        AccountEntity("Admin", "_", AccountType.Administrator)
        // password "_" will never match anything because hash can never be a single character long
    }
}