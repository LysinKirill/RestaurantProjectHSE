package data.entity

data class AccountEntity(
    val name: String,
    val hashedPassword: String,
    val accountType: AccountType
)
