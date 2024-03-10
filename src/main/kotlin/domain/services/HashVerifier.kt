package domain.services

import data.dao.interfaces.AccountDao
import domain.services.interfaces.KeyValueVerifier

class HashVerifier(
    private val accountDao: AccountDao,
    private val hashFunction: (String) -> String
) : KeyValueVerifier<String, String> {
    override fun verify(key: String, value: String): Boolean {
        val account = accountDao.getAccount(key) ?: return false
        return account.hashedPassword == hashFunction(value)
    }
}