package com.ps.musify.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.jayway.jsonpath.JsonPath
import com.ps.musify.clients.RestTemplateClient
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.stream.Collectors


@Service
class MusicArtistService(
	@Value("\${external-api.musicbrainz.endpoint-url}")
	private val musicBrainzEndpointUrl: String,
	@Value("\${external-api.wikidata.endpoint-url}")
	private val wikidataEndpointUrl: String,
	@Value("\${external-api.wikipedia.endpoint-url}")
	private val wikipediaEndpointUrl: String,
	@Value("\${external-api.cover-art.endpoint-url}")
	private val coverArtEndpointUrl: String
) {
	val logger = KotlinLogging.logger {}

	@Autowired
	private lateinit var restClient: RestTemplateClient

	fun getRestTemplate(): RestTemplate = restClient.restTemplate(RestTemplateBuilder())

	fun getMusicBrainzData(mbid: String): ArtistData? {
		logger.trace("In getMusicBrainzData :: before calling MusicBrainz api $musicBrainzEndpointUrl")
		val artistData = getRestTemplate().getForEntity(
			musicBrainzEndpointUrl,
			ArtistData::class.java, mapOf("mbid" to mbid)
		)
		logger.debug { "Response from MusicBrainz $artistData" }
		return artistData.body
	}

	fun getWikidataInfo(brianzData: ArtistData): String? {
		logger.trace("In getWikidataInfo :: before calling Wikidata api $wikidataEndpointUrl")
		brianzData.relations.find { r -> r.type.equals("wikidata") }.let { rel ->
			val resourceUrl = rel?.url?.resource.toString()
			logger.debug("Resource url  ... $resourceUrl")
			val linkId = resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1)
			val wikidataJsonStr = getRestTemplate().getForEntity(
				wikidataEndpointUrl,
				String::class.java, mapOf("linkId" to linkId)
			)
			val artistTitlePath = "$['entities'].['$linkId'].['sitelinks'].['enwiki'].['title']]"
			val artistTitle = JsonPath.parse(wikidataJsonStr.body).read(artistTitlePath, String::class.java)
			logger.debug("artistTitle to be requested to get description  ... $artistTitle")
			try {
				val wikipedia = getRestTemplate().getForEntity(
					wikipediaEndpointUrl,
					Wikipedia::class.java, mapOf("title" to artistTitle)
				)
				logger.debug("Response from wikipedia... $wikipedia")
				return wikipedia.body?.extract
			} catch (ex: Exception) {
				logger.error("Exception occurred ${ex.message}")
			}
		}
		return null
	}

	fun getCoverArtAlbumsOld(brianzData: ArtistData): List<List<Album>?> {
		val headers = HttpHeaders()
		headers.accept = listOf(MediaType.APPLICATION_JSON)
		val entity: HttpEntity<String> = HttpEntity(headers)

		return brianzData.releaseGroups.stream().map { rg ->
			val covers = rg.id?.let { getAlbum(entity, it) }
			logger.debug { "Response from CoverArt $covers" }
			covers?.images
		}.collect(Collectors.toList())
	}

	private fun getAlbum(
		entity: HttpEntity<String>,
		id: String
	): CoverArt? {
		try {
			return getRestTemplate().exchange(
				coverArtEndpointUrl,
				HttpMethod.GET,
				entity,
				CoverArt::class.java, mapOf("id" to id)
			).body
		} catch (ex: Exception) {
			logger.error("Exception occurred ${ex.message}")
		}
		return null
	}
}

data class ArtistDetails(
	val mbid: String,
	val name: String,
	val gender: String?,
	val country: String?,
	val disambiguation: String?,
	val description: String?,
	val albums: List<Album>?
)

data class ArtistData(
	val name: String,
	val gender: String?,
	val country: String,
	val disambiguation: String,
	val relations: List<Relations>,
	@JsonProperty("release-groups")
	val releaseGroups: List<ReleaseGroups>,
	)

data class ReleaseGroups(val id: String?)

data class Relations(val type: String?, val url: Url)
data class Url(val resource: String?)

data class Wikipedia(val extract: String?)

data class CoverArt(val images: List<Album>)
data class Album(val id: String?, val title: String?, val image: String?)
