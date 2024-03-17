package network

/**
 * Class used to represent an IPv4 address (32 bits)
 */
class IPv4Addr(override val addr: Seq[Byte]) extends NetworkAddr(addr, 32, ".") {
  /**
   * Convert internal addr representation using human readable format
   *
   * @return Human readable format of IPv4 address in String
   * @example {{{
   *            val addr = IPv4Addr("1.2.4.8")
   *            addr.toString
   *            > "1.2.4.8"
   * }}}
   */
  override def toString = super.toString
}

/**
 * Companion object used to initialize an IPv4 address
 */
object IPv4Addr {
  val MAX_NUM = 255
  val MIN_NUM = 0

  /**
   * Use human readable format to create an IPv4Addr class instance
   *
   * @param v4Addr `String` representation of IPv4 address
   * @example `IPv4Addr("1.2.4.8")`
   * @return IPv4Addr class
   */
  def apply(v4Addr: String): IPv4Addr = {
    val addr = v4Addr
      .split('.')
      .map(_.toInt)
      .map { i =>
        assert(MIN_NUM <= i && i <= MAX_NUM)
        if (i <= Byte.MaxValue.toInt) {
          i.toByte
        } else {
          (-(i - Byte.MaxValue.toInt)).toByte
        }
      }
    new IPv4Addr(addr)
  }
}