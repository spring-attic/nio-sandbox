class ThumbnailingHandler {

	def documentDatastore

	def "GET ^/\$"(ctx) {
		ctx.emit("http.redirect", "/uploadform") // <- Redirect to static resource for doing uploads
	}

	def "New Upload"(ctx, meta) {
		// Handle new upload
		switch(meta.contentType) {
			case "image/jpeg":
				ctx.emit("org.myplugins.thumbnail.Request", [
					"uri": meta.uri,
					"constrain-x": 100,
					"constrain-y": 100
				])
				break
			default:
				ctx.emit("Handle Upload", [])
		}
	}

	def "org.myplugins.thumbnail.Complete"(ctx, meta) {
		def newUri = meta.uri.transferTo("documentDatastore://bucket/key")
		documentDatastore.bucket.key = ["thumbnail": newUri]
	}

}

handler = new ThumbnailingHandler()
handler.invokeMethod("New Upload", [["emit": { evt, args -> println "event: $evt, args: $args"}], ["contentType": "image/jpeg", "uri": "/"]])