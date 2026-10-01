package kindl.core.id

import io.hypersistence.tsid.TSID

/**
 * 모든 PK를 만드는 TSID 생성기.
 * 노드 비트 10개(최대 1,024대) → 서버당 1ms에 4,096개. 노드 번호는 배포 환경이 TSID_NODE로 주입한다.
 */
object Tsids {
    private const val NODE_BITS = 10

    private val factory: TSID.Factory = TSID.Factory.builder()
        .withNodeBits(NODE_BITS)
        .apply { System.getenv("TSID_NODE")?.takeIf { it.isNotBlank() }?.let { withNode(it.toInt()) } }
        .build()

    fun next(): TSID = factory.generate()

    fun nextString(): String = next().toString()

    fun nextLong(): Long = next().toLong()
}
