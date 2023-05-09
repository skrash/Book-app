package com.skrash.book.data.network

import java.security.MessageDigest

class EncryptIDAlgorithm {

    companion object{

        fun getHexDigestSha1(id: String): String {
            val sha1 = MessageDigest.getInstance("SHA-256")
            val sha1hash = sha1.digest(id.toByteArray(Charsets.UTF_8))
            val builder = StringBuilder()
            for (b in sha1hash) {
                builder.append(String.format("%02x", b))
            }
            return id + builder
        }

        fun shuffleAlgorithm(inStr: String): String {
            if (inStr.length != 80) throw RuntimeException("incorrect length")
            var result = ""
            val arrayChunk = inStr.chunked(10)
            for (chunk in arrayChunk) {
                val newChunk = charArrayOf(
                    chunk[1], chunk[9], chunk[0], chunk[3], chunk[2], chunk[8], chunk[7], chunk[6], chunk[4], chunk[5]
                )
                result += String(newChunk)
            }
            return result
        }
    }
}