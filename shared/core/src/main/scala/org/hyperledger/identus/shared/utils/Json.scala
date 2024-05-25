package org.hyperledger.identus.shared.utils

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.http.media.MediaType
import com.apicatalog.rdf.Rdf
import io.setl.rdf.normalization.RdfNormalize
import org.erdtman.jcs.JsonCanonicalizer

import java.io.{ByteArrayInputStream, StringWriter}
import java.nio.charset.StandardCharsets
import scala.util.Try

object Json {

  /** Canonicalizes a JSON string to JCS format according to RFC 8785
    *
    * @param jsonStr
    *   JSON string to canonicalize
    * @return
    *   canonicalized JSON string
    */

  def canonicalizeToJcs(jsonStr: String): Either[Throwable, String] = {
    val canonicalizer = Try { new JsonCanonicalizer(jsonStr) }
    canonicalizer.map(_.getEncodedString).toEither
  }

  /** Canonicalizes a JSON-LD string to RDF according to the Universal RDF Dataset Normalization Algorithm 2015
    *
    * @param jsonLdStr
    *   JSON-LD string to canonicalize
    * @return
    *   canonicalized RDF as a byte array
    */
  def canonicalizeJsonLDoRdf(jsonLdStr: String): Either[Throwable, Array[Byte]] = {
    Try {
      val inputStream = new ByteArrayInputStream(jsonLdStr.getBytes)
      val document = JsonDocument.of(inputStream)
      val rdfDataset = JsonLd.toRdf(document).get
      val normalized = RdfNormalize.normalize(rdfDataset)
      val writer = new StringWriter
      val rdfWriter = Rdf.createWriter(MediaType.N_QUADS, writer)
      rdfWriter.write(normalized)
      val bytes = writer.toString.getBytes(StandardCharsets.UTF_8);
      bytes
    }.toEither
  }
}
