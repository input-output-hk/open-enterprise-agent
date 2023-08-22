package io.iohk.atala.agent.walletapi.storage

import com.nimbusds.jose.jwk.JWK
import io.iohk.atala.mercury.model.DidId
import zio.*

/** A simple single-user DID key storage */
trait DIDSecretStorage {

  /** PeerDID related methods. TODO: Refactor to abstract over PrismDID & PeerDID and merge methods */
  def insertKey(did: DidId, keyId: String, keyPair: JWK): Task[Int]

  def getKey(did: DidId, keyId: String): Task[Option[JWK]]

}
