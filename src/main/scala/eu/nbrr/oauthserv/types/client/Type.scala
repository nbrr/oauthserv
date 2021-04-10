package eu.nbrr.oauthserv.types.client

sealed trait Type

final case class PublicClient() extends Type

final case class ConfidentialClient() extends Type
