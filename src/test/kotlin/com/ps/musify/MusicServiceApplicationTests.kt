package com.ps.musify

import com.ps.musify.controllers.MusicArtistController
import com.ps.musify.services.ArtistDetails
import com.ps.musify.services.MusicArtistService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MusicServiceApplicationTests {

	@LocalServerPort
	private val port = 0

	@Autowired
	private lateinit var restTemplate: TestRestTemplate

	@Autowired
	private lateinit var controller: MusicArtistController

	@Autowired
	private lateinit var service: MusicArtistService

	@Test
	fun contextLoads() {
	}

	companion object {
		@JvmStatic
		fun testParams() = listOf(
			Arguments.of("f27ec8db-af05-4f36-916e-3d57f91ecf5e", "Michael Jackson"),
			Arguments.of("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab", "Metallica"),
			Arguments.of("92e634a7-6023-4be8-be15-ebba822f5b34", "Maxïmo Park")
		)
	}

	@DisplayName("should test fetch of consolidated details of an Artist")
	@MethodSource("testParams")
	@ParameterizedTest
	fun `Should return Artist aggregated details for a given mbid`(mbid: String, artistName: String) {
		val response = restTemplate.getForEntity(
			"http://localhost:" + port + "/musify/music-artist/details/" + mbid,
			ArtistDetails::class.java
		)
		assertTrue(response.statusCode.is2xxSuccessful)
		assertNotNull(response.body)
		assertEquals(response.body?.mbid, mbid)
		assertEquals(response.body?.name, artistName)
		assertNotNull(response.body?.description)
		assertNotNull(response.body?.albums)
		assertTrue(response.body?.albums?.size!! > 1)
	}
}