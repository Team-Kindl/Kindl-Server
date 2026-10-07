package kindl.core.extension

inline fun <reified E : Enum<E>> String.toEnumIgnoreCaseOrNull(): E? =
    enumValues<E>().firstOrNull { it.name.equals(this, ignoreCase = true) }

inline fun <reified E : Enum<E>> String.toEnumIgnoreCase(): E =
    requireNotNull(toEnumIgnoreCaseOrNull<E>()) { "${E::class.simpleName}에 없는 값: $this" }
