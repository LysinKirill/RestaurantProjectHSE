package domain.services.interfaces

interface KeyValueVerifier<KeyType, ValueType> {
    fun verify(key: KeyType, value: ValueType) : Boolean
}