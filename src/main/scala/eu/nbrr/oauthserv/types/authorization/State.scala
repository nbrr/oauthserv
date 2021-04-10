package eu.nbrr.oauthserv.types.authorization

case class State(value: String) {
  override def toString: String = value
}
