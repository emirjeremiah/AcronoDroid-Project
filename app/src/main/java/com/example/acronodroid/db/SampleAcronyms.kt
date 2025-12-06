package com.example.acronodroid.db

import com.example.acronodroid.models.Acronym
import java.util.*

// A modest curated list — you can expand to 60-100
object SampleAcronyms {
    fun get(): List<Acronym> {
        val now = System.currentTimeMillis().toString()
        return listOf(
            Acronym(id = "local_api", short = "API", full = "Application Programming Interface",
                explanation = "A set of rules and tools for building software and applications; lets two systems talk.",
                example = "Using Twitter API to fetch tweets.", category = "Web/Programming", isLocalOnly=true),
            Acronym(id = "local_oop", short = "OOP", full = "Object-Oriented Programming",
                explanation = "Programming paradigm using objects/classes to structure code.", example = "Java, C++ uses OOP.", category = "Programming", isLocalOnly=true),
            Acronym(id = "local_json", short = "JSON", full = "JavaScript Object Notation",
                explanation = "Lightweight data-interchange format, easy for humans and machines to parse.", example = "{\"name\":\"Emir\"}", category = "Data", isLocalOnly=true),
            Acronym(id = "local_http", short = "HTTP", full = "HyperText Transfer Protocol",
                explanation = "Protocol used to transfer web pages and data over the internet.", example = "A browser makes HTTP requests to websites.", category = "Networking", isLocalOnly=true),
            Acronym(id = "local_https", short = "HTTPS", full = "HTTP Secure",
                explanation = "HTTP with encryption (TLS/SSL) for secure communication.", example = "Banking websites use HTTPS.", category = "Networking", isLocalOnly=true),
            Acronym(id = "local_dns", short = "DNS", full = "Domain Name System",
                explanation = "Translates domain names to IP addresses.", example = "example.com → 93.184.216.34", category = "Networking", isLocalOnly=true),
            Acronym(id = "local_sql", short = "SQL", full = "Structured Query Language",
                explanation = "Language for managing relational databases.", example = "SELECT * FROM users;", category = "Databases", isLocalOnly=true),
            Acronym(id = "local_tcp", short = "TCP", full = "Transmission Control Protocol",
                explanation = "Reliable, ordered communication between networked systems.", example = "Used by HTTP.", category = "Networking", isLocalOnly=true),
            Acronym(id = "local_udp", short = "UDP", full = "User Datagram Protocol",
                explanation = "Lightweight, connectionless protocol for fast transfers.", example = "Used by DNS and streaming.", category = "Networking", isLocalOnly=true),
            Acronym(id = "local_rest", short = "REST", full = "Representational State Transfer",
                explanation = "Architectural style for designing networked applications (APIs).", example = "RESTful API endpoints.", category = "Web/Programming", isLocalOnly=true)
            // Add more entries to reach 50-100 as needed...
        )
    }
}
