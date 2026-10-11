package proov.core.id

import io.hypersistence.tsid.TSID

object Tsids {
    private const val NODE_BITS = 10

    private val factory: TSID.Factory = TSID.Factory.builder()
        .withNodeBits(NODE_BITS)
        .apply { System.getenv("TSID_NODE")?.takeIf { it.isNotBlank() }?.let { withNode(it.toInt()) } }
        .build()

    fun nextString(): String = factory.generate().toString()

    fun nextLong(): Long = factory.generate().toLong()
}
