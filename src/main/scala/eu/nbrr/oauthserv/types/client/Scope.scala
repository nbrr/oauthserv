package eu.nbrr.oauthserv.types.client

case class Scope(value: Set[String]) {
  override def toString: String = value.mkString(" ")

  def get(): Set[String] = value

  def contains(scopeElt: String): Boolean = value.contains(scopeElt)

  def contains(scope: Scope): Boolean = scope.get().subsetOf(value)
}

object Scope {
  def apply(s: String): Scope = fromString(s)

  def fromString(s: String): Scope = Scope(s.split(" ").toSet)
}
