package com.example.homework3.data

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsItemParser {
    companion object {
        val LOG_TAG: String = NewsItemParser::class.java.canonicalName ?: "RssParser"
        val ns: String? = null // Namespace is null as it's not needed for parsing
    }

    // Parses the input stream of an XML RSS feed and returns a list of NewsItems
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    fun parse(inputStream: InputStream): List<NewsItem> {
        return inputStream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            readRss(parser)
        }
    }

    // Reads the RSS feed and returns a list of NewsItems
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readRss(parser: XmlPullParser): List<NewsItem> {
        val entries: MutableList<NewsItem> = ArrayList()
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "channel") {
                entries.addAll(readChannel(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    // Reads the channel tag and returns a list of NewsItems
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readChannel(parser: XmlPullParser): List<NewsItem> {
        val entries: MutableList<NewsItem> = ArrayList()
        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "item") {
                val item = readItem(parser)
                if (item == null)
                    Log.w(LOG_TAG, "Invalid item found. Ignoring it")
                else
                    entries.add(item)
            } else {
                skip(parser)
            }
        }
        return entries
    }

    // Reads an item tag and returns a NewsItem or null if invalid
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readItem(parser: XmlPullParser): NewsItem? {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var uniqueIdentifier: String? = null
        var title: String? = null
        var link: String? = null
        var author: String? = null
        var description: String? = null
        var imgString: String? = null
        var publishedOn: Date? = null
        val keywords: MutableSet<String> = HashSet()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "guid" -> uniqueIdentifier = readBasicTag(parser, "guid")?.trim()
                "title" -> title = readBasicTag(parser, "title")?.trim()
                "category" -> {
                    val keyword = readBasicTag(parser, "category")?.trim()
                    if (keyword != null) keywords.add(keyword)
                }
                "link" -> link = readBasicTag(parser, "link")?.trim()
                "pubDate" -> publishedOn = parseDate(parser)
                "dc:creator" -> author = readBasicTag(parser, "dc:creator")?.trim()
                "description" -> description = readBasicTag(parser, "description")?.trim()
                "media:content" -> {
                    val newImage = readMediaTag(parser)
                    if (newImage != null) imgString = newImage
                }
                else -> skip(parser)
            }
        }
        return if (uniqueIdentifier == null || title == null || publishedOn == null)
            null
        else
            NewsItem(
                uniqueIdentifier = uniqueIdentifier,
                title = title,
                description = description,
                imageUrl = imgString,
                author = author,
                fullArticleLink = link,
                publicationDate = publishedOn,
                keywords = keywords
            )
    }

    // Parses a date string to a Date object
    private fun parseDate(parser: XmlPullParser): Date? {
        val pubDate = readBasicTag(parser, "pubDate") ?: return null
        return try {
            SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(pubDate)
        } catch (e: ParseException) {
            SimpleDateFormat("E, dd MMM yyyy HH:mm:ss 'Z'", Locale.US).parse(pubDate)
        }
    }

    // Reads the content of a basic tag
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readBasicTag(parser: XmlPullParser, tag: String): String? {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return result
    }

    // Reads the content of a media tag and returns the image URL if valid
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readMediaTag(parser: XmlPullParser): String? {
        var url: String? = null
        var isImage = false
        var type: String? = null

        parser.require(XmlPullParser.START_TAG, ns, "media:content")
        for (i in 0 until parser.attributeCount) {
            when (parser.getAttributeName(i)) {
                "medium" -> if (parser.getAttributeValue(i) == "image") isImage = true
                "url" -> url = parser.getAttributeValue(i)
            }
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "media:keywords" -> type = readBasicTag(parser, "media:keywords")
                else -> skip(parser)
            }
        }
        return if (type == "headline" && isImage) url else null
    }

    // Reads the text content of a tag
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String? {
        var result: String? = null
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    // Skips tags that are not needed
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
