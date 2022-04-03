package com.ps.musify.controllers

import com.ps.musify.services.ArtistDetails
import com.ps.musify.services.MusicArtistService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/musify")
class MusicArtistController(val musicArtistService: MusicArtistService) {
	val logger = KotlinLogging.logger {}

	@GetMapping("music-artist/details/{mbid}")
	fun getArtistDetails(@PathVariable mbid: String): ResponseEntity<Any> {
		logger.debug("Before calling external apis for mbid : $mbid")
		val artistData = musicArtistService.getMusicBrainzData(mbid)
		if(artistData != null) {
			val desc = musicArtistService.getWikidataInfo(artistData)
			val coverArtAlbums = musicArtistService.getCoverArtAlbumsOld(artistData)
			val albums = coverArtAlbums.filterNotNull().flatten()
			logger.debug { "Albums data fetched :: $albums" }
			val artistDetails = ArtistDetails(name = artistData.name, mbid = mbid, gender = artistData.gender,
				country = artistData.country, disambiguation = artistData.disambiguation, description = desc, albums = albums)
			logger.debug { "Consolidated Artist data $artistDetails" }
			return ResponseEntity(artistDetails, HttpStatus.OK)
		}
		return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
	}
}
